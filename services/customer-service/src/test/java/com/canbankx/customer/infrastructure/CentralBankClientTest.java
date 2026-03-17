package com.canbankx.customer.infrastructure;

import com.canbankx.customer.dto.CentralBankTransferRequest;
import com.canbankx.customer.dto.CentralBankTransferResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CentralBankClientTest {

    @Mock
    private RestTemplate restTemplate;

    private CentralBankClient centralBankClient;

    @BeforeEach
    void setUp() {
        centralBankClient = new CentralBankClient(restTemplate);
        ReflectionTestUtils.setField(centralBankClient, "centralBankUrl", "http://central-bank:8090");
        ReflectionTestUtils.setField(centralBankClient, "bankId", 2);
    }

    @Test
    void testSuccessfulTransferRequest() {
        // Arrange
        CentralBankTransferRequest request = CentralBankTransferRequest.builder()
                .externalTransactionId("TXN-CB-001")
                .senderEmail("sender@bank2.com")
                .senderBankId(2)
                .recipientEmail("recipient@bank1.com")
                .receiverBankId(1)
                .amount(BigDecimal.valueOf(1000))
                .currency("CAD")
                .requestedAt(Instant.now())
                .idempotencyKey("IDEMP-001")
                .build();

        CentralBankTransferResponse mockResponse = CentralBankTransferResponse.builder()
                .externalTransactionId("TXN-CB-001")
                .centralBankTransactionId("CB-TXN-12345")
                .status("ACCEPTED")
                .processedAt(Instant.now())
                .build();

        when(restTemplate.postForObject(anyString(), any(), eq(CentralBankTransferResponse.class)))
                .thenReturn(mockResponse);

        // Act
        CentralBankTransferResponse response = centralBankClient.sendTransferRequest(request);

        // Assert
        assertNotNull(response);
        assertEquals("ACCEPTED", response.getStatus());
        assertEquals("CB-TXN-12345", response.getCentralBankTransactionId());
        assertEquals("TXN-CB-001", response.getExternalTransactionId());
        verify(restTemplate, times(1)).postForObject(anyString(), any(), eq(CentralBankTransferResponse.class));
    }

    @Test
    void testTransferRejectedByBank() {
        // Arrange
        CentralBankTransferRequest request = CentralBankTransferRequest.builder()
                .externalTransactionId("TXN-CB-002")
                .senderEmail("sender@bank2.com")
                .senderBankId(2)
                .recipientEmail("recipient@bank1.com")
                .receiverBankId(1)
                .amount(BigDecimal.valueOf(500))
                .currency("CAD")
                .requestedAt(Instant.now())
                .build();

        CentralBankTransferResponse mockResponse = CentralBankTransferResponse.builder()
                .externalTransactionId("TXN-CB-002")
                .status("REJECTED")
                .reason("Recipient account not found")
                .processedAt(Instant.now())
                .build();

        when(restTemplate.postForObject(anyString(), any(), eq(CentralBankTransferResponse.class)))
                .thenReturn(mockResponse);

        // Act
        CentralBankTransferResponse response = centralBankClient.sendTransferRequest(request);

        // Assert
        assertNotNull(response);
        assertEquals("REJECTED", response.getStatus());
        assertTrue(response.getReason().contains("Recipient account not found"));
    }

    @Test
    void testConnectionFailureWithRetry() {
        // Arrange
        CentralBankTransferRequest request = CentralBankTransferRequest.builder()
                .externalTransactionId("TXN-CB-003")
                .senderEmail("sender@bank2.com")
                .senderBankId(2)
                .recipientEmail("recipient@bank1.com")
                .receiverBankId(1)
                .amount(BigDecimal.valueOf(2000))
                .currency("CAD")
                .requestedAt(Instant.now())
                .build();

        when(restTemplate.postForObject(anyString(), any(), eq(CentralBankTransferResponse.class)))
                .thenThrow(new ResourceAccessException("Connection timeout"));

        // Act & Assert
        assertThrows(CentralBankClient.CentralBankCommunicationException.class,
                () -> centralBankClient.sendTransferRequest(request));
    }
}
