package com.crafthinge.common.events;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * AZURE mode (crafthinge.events.mode=servicebus): sends to the "order-events" topic.
 * Authentication: DefaultAzureCredential -> AKS Workload Identity. No connection string exists.
 * The sender is created lazily, so services that never publish open no connection.
 */
@Component
@ConditionalOnProperty(name = "crafthinge.events.mode", havingValue = "servicebus")
public class ServiceBusEventPublisher implements EventPublisher, DisposableBean {

    private final String namespace;
    private final String topic;
    private final ObjectMapper mapper;
    private volatile ServiceBusSenderClient sender;

    public ServiceBusEventPublisher(@Value("${crafthinge.events.servicebus.namespace}") String namespace,
                                    @Value("${crafthinge.events.servicebus.topic:order-events}") String topic,
                                    ObjectMapper mapper) {
        this.namespace = namespace;
        this.topic = topic;
        this.mapper = mapper;
    }

    @Override
    public void publish(OrderEvent event) {
        try {
            ServiceBusMessage message = new ServiceBusMessage(mapper.writeValueAsString(event))
                .setMessageId(event.eventId().toString())
                .setSubject(event.type())
                .setContentType("application/json");
            sender().sendMessage(message);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialise event", e);
        }
    }

    private ServiceBusSenderClient sender() {
        if (sender == null) {
            synchronized (this) {
                if (sender == null) {
                    sender = new ServiceBusClientBuilder()
                        .fullyQualifiedNamespace(namespace)
                        .credential(new DefaultAzureCredentialBuilder().build())
                        .sender()
                        .topicName(topic)
                        .buildClient();
                }
            }
        }
        return sender;
    }

    @Override
    public void destroy() {
        if (sender != null) {
            sender.close();
        }
    }
}
