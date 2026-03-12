package com.canbankx.customer.controller;

import com.canbankx.customer.domain.Customer;
import com.canbankx.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @PostMapping
    public Customer createCustomer(@Valid @RequestBody Customer customer) {
        return service.createCustomer(customer);
    }

    @GetMapping
    public List<Customer> getCustomers() {
        return service.getAllCustomers();
    }

}