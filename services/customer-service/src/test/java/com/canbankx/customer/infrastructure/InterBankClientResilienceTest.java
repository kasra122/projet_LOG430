package com.canbankx.customer.infrastructure;

import com.canbankx.customer.dto.InboundTransferRequest;
import com.canbankx.customer.dto.InboundTransferResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterBankClientResilienceTest {

    @Mock
    private RestTemplate restTemplate;

    private InterBankClient interBankClient;

    @BeforeEach
    void setUp() {
        interBankClient = new InterBankClient(restTemplate);
        ReflectionTestUtils.setField(interBankClient, "bank1Url", "http://bank1:8091");
        ReflectionTestUtils.setField(interBankClient, "bank3Url", "http://bank3:8093");
    }

    @Test
    void testSuccessfulTransfer() {
        // Arrange
        InboundTransferRequest request = InboundTransferRequest.builder()
                .externalTransactionId("TXN-001")
                .senderCustomerEmail("sender@bank1.com")
                .recipientEmail("recipient@bank2.com")
                .amount(BigDecimal.valueOf(1000))
                .senderBankId(1)
                .currency("CAD")
                .build();

        InboundTransferResponse mockResponse = InboundTransferResponse.builder()
                .externalTransactionId("TXN-001")
                .status("ACCEPTED")
                .processedAt(java.time.Instant.now())
                .build();

        when(restTemplate.postForObject(anyString(), any(), eq(InboundTransferResponse.class)))
                .thenReturn(mockResponse);

        // Act
        InboundTransferResponse response = interBankClient.sendTransferToBank(1, request);

        // Assert
        assertNotNull(response);
        assertEquals("ACCEPTED", response.getStatus());
        verify(restTemplate, times(1)).postForObject(anyString(), any(), eq(InboundTransferResponse.class));
    }

    @Test
    void testCircuitBreakerFallback() {
        // Arrange
        InboundTransferRequest request = InboundTransferRequest.builder()
                .externalTransactionId("TXN-002")
                .senderCustomerEmail("sender@bank1.com")
                .recipientEmail("recipient@bank2.com")
                .amount(BigDecimal.valueOf(500))
                .senderBankId(1)
                .currency("CAD")
                .build();

        when(restTemplate.postForObject(anyString(), any(), eq(InboundTransferResponse.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        // Act & Assert
        assertThrows(InterBankClient.InterBankCommunicationException.class, 
                () -> interBankClient.sendTransferToBank(1, request));
    }

    @Test
    void testTransferFallbackMethod() {
        // Arrange
        InboundTransferRequest request = InboundTransferRequest.builder()
                .externalTransactionId("TXN-003")
                .senderCustomerEmail("sender@bank1.com")
                .recipientEmail("recipient@bank2.com")
                .amount(BigDecimal.valueOf(2000))
                .senderBankId(1)
                .currency("CAD")
                .build();

        Exception ex = new RuntimeException("Bank unavailable");

        // Act
        InboundTransferResponse fallbackResponse = interBankClient.transferFallback(1, request, ex);

        // Assert
        assertNotNull(fallbackResponse);
        assertEquals("PENDING", fallbackResponse.getStatus());
        assertTrue(fallbackResponse.getReason().contains("temporarily unavailable"));
    }

    @Test
    void testInvalidBankId() {
        // Arrange
        InboundTransferRequest request = InboundTransferRequest.builder()
                .externalTransactionId("TXN-004")
                .senderCustomerEmail("sender@bank1.com")
                .recipientEmail("recipient@bank2.com")
                .amount(BigDecimal.valueOf(1000))
                .senderBankId(999)
                .currency("CAD")
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> interBankClient.sendTransferToBank(999, request));
    }
}
