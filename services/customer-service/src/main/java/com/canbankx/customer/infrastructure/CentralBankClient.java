package com.canbankx.customer.infrastructure;

import com.canbankx.customer.dto.CentralBankTransferRequest;
import com.canbankx.customer.dto.CentralBankTransferResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class CentralBankClient {

    private final RestTemplate restTemplate;

    @Value("${bank.central-bank-url:http://central-bank:8090}")
    private String centralBankUrl;

    @Value("${bank.id:2}")
    private Integer bankId;

    /**
     * Sends a transfer request to the Central Bank.
     * The Central Bank will process the transfer and send a settlement notification later.
     *
     * @param request Transfer request containing amount, sender, recipient details
     * @return Response with external transaction ID and initial status
     */
    @Retryable(
            retryFor = {java.net.SocketTimeoutException.class, org.springframework.web.client.ResourceAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 500, multiplier = 2)
    )
    public CentralBankTransferResponse sendTransferRequest(CentralBankTransferRequest request) {
        try {
            String endpoint = centralBankUrl + "/api/v1/transfers";

            log.info("Sending transfer to Central Bank: externalTxnId={}, amount={}, receiver bank={}", 
                    request.getExternalTransactionId(), request.getAmount(), request.getReceiverBankId());

            CentralBankTransferResponse response = restTemplate.postForObject(
                    endpoint,
                    request,
                    CentralBankTransferResponse.class
            );

            log.info("Central Bank accepted transfer: externalTxnId={}, centralBankTxnId={}, status={}", 
                    request.getExternalTransactionId(), response.getCentralBankTransactionId(), response.getStatus());

            return response;

        } catch (RestClientException e) {
            log.error("Failed to communicate with Central Bank for transfer {}", request.getExternalTransactionId(), e);
            throw new CentralBankCommunicationException(
                    "Failed to send transfer to Central Bank",
                    e
            );
        }
    }

    public static class CentralBankCommunicationException extends RuntimeException {
        public CentralBankCommunicationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
