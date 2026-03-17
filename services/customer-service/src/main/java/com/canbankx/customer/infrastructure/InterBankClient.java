package com.canbankx.customer.infrastructure;

import com.canbankx.customer.dto.InboundTransferRequest;
import com.canbankx.customer.dto.InboundTransferResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class InterBankClient {

    private final RestTemplate restTemplate;

    @Value("${bank.interbank-urls.bank-1:http://bank1:8091}")
    private String bank1Url;

    @Value("${bank.interbank-urls.bank-3:http://bank3:8093}")
    private String bank3Url;

    public InboundTransferResponse sendTransferToBank(
            Integer receiverBankId,
            InboundTransferRequest request) {
        try {
            String bankUrl = getBankUrl(receiverBankId);
            String endpoint = bankUrl + "/api/v1/transactions/inbound-transfer";

            log.info("Sending transfer to Bank {} at {}", receiverBankId, endpoint);

            InboundTransferResponse response = restTemplate.postForObject(
                    endpoint,
                    request,
                    InboundTransferResponse.class
            );

            log.info("Received response from Bank {}: {}", receiverBankId, response);
            return response;

        } catch (RestClientException e) {
            log.error("Failed to send transfer to Bank {}", receiverBankId, e);
            throw new InterBankCommunicationException(
                    "Failed to communicate with Bank " + receiverBankId,
                    e
            );
        }
    }

    private String getBankUrl(Integer bankId) {
        return switch (bankId) {
            case 1 -> bank1Url;
            case 3 -> bank3Url;
            default -> throw new IllegalArgumentException("Unknown bank ID: " + bankId);
        };
    }

    public static class InterBankCommunicationException extends RuntimeException {
        public InterBankCommunicationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
