package com.canbankx.customer.controller;

import org.springframework.web.bind.annotation.*;

@RestController
public class TestController {

    @GetMapping("/test")
    public String test() {
        System.out.println(">>> TEST ENDPOINT HIT");
        return "TEST_OK";
    }
}
