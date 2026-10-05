package com.crafthinge.common.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Consistent error bodies across all services (problem+json).
 * Bean-validation errors are handled by the parent class.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    ProblemDetail handleApi(ApiException e) {
        return ProblemDetail.forStatusAndDetail(e.status(), e.getMessage());
    }

    /** A downstream service answered with an error (e.g. 404 from food-service). */
    @ExceptionHandler(RestClientResponseException.class)
    ProblemDetail handleDownstream(RestClientResponseException e) {
        log.warn("Downstream call failed: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
        HttpStatus status = e.getStatusCode().is4xxClientError()
            ? HttpStatus.valueOf(e.getStatusCode().value()) : HttpStatus.BAD_GATEWAY;
        return ProblemDetail.forStatusAndDetail(status, "A dependent service returned " + e.getStatusCode().value());
    }

    /** A downstream service is unreachable or timed out. */
    @ExceptionHandler(ResourceAccessException.class)
    ProblemDetail handleUnreachable(ResourceAccessException e) {
        log.error("Downstream service unreachable", e);
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, "A dependent service is unavailable. Try again shortly.");
    }
}
