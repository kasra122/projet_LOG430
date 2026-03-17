package com.canbankx.customer.controller;

import com.canbankx.customer.config.SecurityConfig;
import com.canbankx.customer.config.TestDataBuilder;
import com.canbankx.customer.domain.Customer;
import com.canbankx.customer.dto.AuthRegisterRequest;
import com.canbankx.customer.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    @Test
    void testRegister_Success() throws Exception {
        AuthRegisterRequest request = TestDataBuilder.buildRegisterRequest("newuser@bank2.com");
        Customer newCustomer = TestDataBuilder.buildCustomer("newuser@bank2.com", Customer.KycStatus.PENDING);
        newCustomer.setId(UUID.randomUUID());

        when(customerService.registerCustomer(any(), any(), any()))
            .thenReturn(newCustomer);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("newuser@bank2.com"));
    }

    @Test
    void testRegister_DuplicateEmail() throws Exception {
        AuthRegisterRequest request = TestDataBuilder.buildRegisterRequest("existing@bank2.com");

        when(customerService.registerCustomer(any(), any(), any()))
            .thenThrow(new CustomerService.CustomerAlreadyExistsException("Customer already exists"));

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }

    @Test
    void testHealth_Success() throws Exception {
        mockMvc.perform(get("/api/v1/auth/health")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string("Bank 2 (Kasra) is healthy"));
    }
}
