package com.canbankx.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    /**
     * Configure API Gateway routes
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Customer Service Routes
                .route("customer-service", r -> r
                        .path("/api/v1/customers/**")
                        .uri("http://localhost:8081"))
                
                // Transaction Service Routes
                .route("transaction-service", r -> r
                        .path("/api/v1/transactions/**")
                        .uri("http://localhost:8081"))
                
                // Settlement Webhook
                .route("settlement-service", r -> r
                        .path("/api/v1/settlements/**")
                        .uri("http://localhost:8081"))
                
                // Health check passthrough
                .route("health-check", r -> r
                        .path("/actuator/**")
                        .uri("http://localhost:8081"))
                
                .build();
    }

    /**
     * Configure CORS headers
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(Arrays.asList("*"));
        corsConfig.setMaxAge(3600L);
        corsConfig.addAllowedMethod("*");
        corsConfig.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
