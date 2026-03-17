package com.canbankx.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class HealthController {

    /**
     * Health check endpoint for service monitoring
     * Returns detailed status information about the application
     *
     * @return Health status with service details
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();

        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "CanBankX Customer Service");
        response.put("version", "0.0.1-SNAPSHOT");
        response.put("environment", System.getProperty("spring.profiles.active", "default"));

        return ResponseEntity.ok(response);
    }

    /**
     * Liveness probe endpoint for Kubernetes/container orchestration
     * Indicates if the application is running
     *
     * @return Liveness status
     */
    @GetMapping("/live")
    public ResponseEntity<Map<String, String>> live() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "CanBankX Customer Service");
        return ResponseEntity.ok(response);
    }

    /**
     * Readiness probe endpoint for Kubernetes/container orchestration
     * Indicates if the application is ready to accept traffic
     *
     * @return Readiness status
     */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, String>> ready() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "CanBankX Customer Service");
        return ResponseEntity.ok(response);
    }

    /**
     * Detailed health information endpoint
     * Returns comprehensive application metrics
     *
     * @return Detailed health information
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailed() {
        Map<String, Object> response = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();

        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "CanBankX Customer Service");
        response.put("version", "0.0.1-SNAPSHOT");
        response.put("java-version", System.getProperty("java.version"));

        Map<String, Object> memory = new HashMap<>();
        memory.put("total-mb", runtime.totalMemory() / 1024 / 1024);
        memory.put("free-mb", runtime.freeMemory() / 1024 / 1024);
        memory.put("max-mb", runtime.maxMemory() / 1024 / 1024);
        response.put("memory", memory);

        return ResponseEntity.ok(response);
    }
}