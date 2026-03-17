package com.canbankx.customer.controller;

import com.canbankx.customer.config.SecurityConfig;
import com.canbankx.customer.config.TestDataBuilder;
import com.canbankx.customer.domain.Customer;
import com.canbankx.customer.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@Import(SecurityConfig.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Test
    void testGetCustomer_Found() throws Exception {
        UUID customerId = UUID.randomUUID();
        Customer customer = TestDataBuilder.buildCustomer("john@bank2.com", Customer.KycStatus.VERIFIED);
        customer.setId(customerId);

        when(customerService.getCustomerById(customerId))
            .thenReturn(Optional.of(customer));

        mockMvc.perform(get("/api/v1/customers/" + customerId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("john@bank2.com"));
    }

    @Test
    void testGetCustomer_NotFound() throws Exception {
        UUID customerId = UUID.randomUUID();

        when(customerService.getCustomerById(customerId))
            .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/customers/" + customerId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void testGetCustomerByEmail_Found() throws Exception {
        String email = "john@bank2.com";
        Customer customer = TestDataBuilder.buildCustomer(email, Customer.KycStatus.VERIFIED);

        when(customerService.getCustomerByEmail(email))
            .thenReturn(Optional.of(customer));

        mockMvc.perform(get("/api/v1/customers/email/" + email)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void testGetCustomerByEmail_NotFound() throws Exception {
        String email = "unknown@bank2.com";

        when(customerService.getCustomerByEmail(email))
            .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/customers/email/" + email)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}
