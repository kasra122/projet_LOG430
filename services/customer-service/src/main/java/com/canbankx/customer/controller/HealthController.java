package com.canbankx.customer.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    public String health() {
        System.out.println(">>> HEALTH HIT - NO LOGIC");
        return "OK";
    }
}
