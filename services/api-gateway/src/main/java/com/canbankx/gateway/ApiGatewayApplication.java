package com.canbankx.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("customer-route", r -> r
                        .path("/api/v1/customers/**")
                        .uri("http://localhost:8080"))
                .route("transaction-route", r -> r
                        .path("/api/v1/transactions/**")
                        .uri("http://localhost:8080"))
                .build();
    }
}
