package com.canbankx.customer.infrastructure;

import com.canbankx.customer.domain.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CentralBankClient {

    private final RestTemplate restTemplate;

    @Value("${bank.central-bank-url:http://central-bank:9000}")
    private String centralBankUrl;

    public void registerCustomerWithCentralBank(Customer customer) {
        try {
            String endpoint = centralBankUrl + "/api/v1/customers/register";

            Map<String, Object> request = new HashMap<>();
            request.put("email", customer.getEmail());
            request.put("firstName", customer.getFirstName());
            request.put("lastName", customer.getLastName());
            request.put("bankId", 2);
            request.put("customerId", customer.getId().toString());

            log.info("Registering customer {} with central bank", customer.getEmail());

            restTemplate.postForObject(endpoint, request, Map.class);

            log.info("Customer {} registered with central bank", customer.getEmail());

        } catch (RestClientException e) {
            log.warn("Failed to register customer with central bank, continuing anyway", e);
        }
    }

    public void notifySettlement(String externalTransactionId, String result, String reason) {
        try {
            String endpoint = centralBankUrl + "/api/v1/settlements/notify";

            Map<String, Object> request = new HashMap<>();
            request.put("externalTransactionId", externalTransactionId);
            request.put("result", result);
            request.put("reason", reason);
            request.put("bankId", 2);

            log.info("Notifying central bank of settlement: {} -> {}", externalTransactionId, result);

            restTemplate.postForObject(endpoint, request, Map.class);

        } catch (RestClientException e) {
            log.warn("Failed to notify central bank of settlement", e);
        }
    }
}
