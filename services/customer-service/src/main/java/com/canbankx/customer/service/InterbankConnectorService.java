package com.canbankx.customer.service;

import com.canbankx.customer.domain.Bank;
import com.canbankx.customer.domain.Transaction;
import com.canbankx.customer.repository.BankRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterbankConnectorService {

    private final BankRepository bankRepository;
    private final RestTemplate restTemplate;

    public void sendTransferToBank(Transaction transaction, Bank recipientBank) {
        try {
            log.info("Sending transfer to bank {}: transaction {}", recipientBank.getNm(), transaction.getId());

            Map<String, Object> payload = buildPayload(transaction);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + recipientBank.getK());
            headers.set("X-Idempotency-Key", transaction.getIdempotencyKey());
            headers.set("X-Request-ID", transaction.getExternalTransactionId());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            String url = recipientBank.getAUrl() + "/api/v1/interbank/receive";
            
            restTemplate.postForObject(url, request, Map.class);

            log.info("Transfer sent successfully to bank {}", recipientBank.getNm());

        } catch (Exception e) {
            log.error("Failed to send transfer to bank {}: {}", recipientBank.getNm(), e.getMessage());
            throw new RuntimeException("Inter-bank transfer failed: " + e.getMessage());
        }
    }

    public Optional<Bank> getBankById(Integer bankId) {
        return bankRepository.findById(bankId);
    }

    public Optional<Bank> getActiveBankByName(String name) {
        return bankRepository.findByNm(name)
                .filter(bank -> "ACTIVE".equals(bank.getSt()));
    }

    public void syncAllBanks() {
        List<Bank> allBanks = bankRepository.findAll();
        for (Bank b : allBanks) {
            if ("ACTIVE".equals(b.getSt())) {
                String healthUrl = b.getAUrl() + "/api/v1/interbank/health";
                try {
                    restTemplate.getForObject(healthUrl, String.class);
                } catch (Exception ex) {
                    log.error("Bank {} is down", b.getNm());
                }
            }
        }
    }

    private Map<String, Object> buildPayload(Transaction transaction) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("transactionId", transaction.getExternalTransactionId());
        payload.put("senderBankId", transaction.getSenderBankId());
        payload.put("recipientEmail", transaction.getTargetCustomerEmail());
        payload.put("amount", transaction.getAmount());
        payload.put("currency", transaction.getCurrency());
        payload.put("idempotencyKey", transaction.getIdempotencyKey());
        payload.put("description", "Interbank Transfer");
        
        Bank tmpBnk = bankRepository.findById(transaction.getSenderBankId()).orElse(null);
        if (tmpBnk != null) {
            payload.put("senderBankName", tmpBnk.getNm());
        }
        
        Bank tmpBnk2 = bankRepository.findById(transaction.getReceiverBankId()).orElse(null);
        if (tmpBnk2 != null) {
            payload.put("receiverBankName", tmpBnk2.getNm());
        }
        
        return payload;
    }
}
