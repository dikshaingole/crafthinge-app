package com.crafthinge.common.events;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * AZURE mode: each consuming service reads from its own subscription on the
 * "order-events" topic. If the handler throws, the message is abandoned and
 * retried; after max delivery count (10, set in Terraform) it moves to the
 * dead-letter queue for investigation.
 */
@Component
@ConditionalOnProperty(name = "crafthinge.events.mode", havingValue = "servicebus")
public class ServiceBusEventListener implements DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(ServiceBusEventListener.class);

    private final ObjectProvider<OrderEventHandler> handlerProvider;
    private final ObjectMapper mapper;
    private final String namespace;
    private final String topic;
    private final String subscription;
    private ServiceBusProcessorClient processor;

    public ServiceBusEventListener(ObjectProvider<OrderEventHandler> handlerProvider, ObjectMapper mapper,
                                   @Value("${crafthinge.events.servicebus.namespace}") String namespace,
                                   @Value("${crafthinge.events.servicebus.topic:order-events}") String topic,
                                   @Value("${crafthinge.events.servicebus.subscription:${spring.application.name}}") String subscription) {
        this.handlerProvider = handlerProvider;
        this.mapper = mapper;
        this.namespace = namespace;
        this.topic = topic;
        this.subscription = subscription;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        OrderEventHandler handler = handlerProvider.getIfAvailable();
        if (handler == null) {
            return; // this service publishes only
        }
        processor = new ServiceBusClientBuilder()
            .fullyQualifiedNamespace(namespace)
            .credential(new DefaultAzureCredentialBuilder().build())
            .processor()
            .topicName(topic)
            .subscriptionName(subscription)
            .processMessage(ctx -> {
                try {
                    handler.handle(mapper.readValue(ctx.getMessage().getBody().toString(), OrderEvent.class));
                } catch (java.io.IOException e) {
                    throw new IllegalStateException("Unreadable event " + ctx.getMessage().getMessageId(), e);
                }
            })
            .processError(err -> log.error("Service Bus error on {}/{}", topic, subscription, err.getException()))
            .buildProcessorClient();
        processor.start();
        log.info("Listening to Service Bus {}/{}", topic, subscription);
    }

    @Override
    public void destroy() {
        if (processor != null) {
            processor.close();
        }
    }
}
