package com.canbankx.customer.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

import java.time.Duration;

@Slf4j
@Configuration
@EnableRetry
public class ResilienceConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50.0f)
                .slowCallRateThreshold(50.0f)
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .recordExceptions(java.net.SocketTimeoutException.class,
                        org.springframework.web.client.ResourceAccessException.class,
                        org.springframework.web.client.HttpServerErrorException.class)
                .ignoreExceptions(IllegalArgumentException.class)
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(circuitBreakerConfig);
        registry.getEventPublisher()
                .onEntryAdded(event -> log.info("CircuitBreaker registered: {}", event.getAddedEntry().getName()));

        return registry;
    }
}
