package com.canbankx.customer.infrastructure;

import com.canbankx.customer.domain.Transaction;
import com.canbankx.customer.dto.InboundTransferRequest;
import com.canbankx.customer.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InterBankRetryHandler {

    private final TransactionRepository transactionRepository;
    private final InterBankClient interBankClient;

    @Scheduled(fixedDelay = 30000, initialDelay = 10000) // Every 30 seconds
    public void retryPendingInterbankTransfers() {
        log.debug("Checking for pending interbank transfers to retry...");

        List<Transaction> pendingTransactions = transactionRepository.findByStatusAndType(
                Transaction.TransactionStatus.PENDING,
                Transaction.TransactionType.INTERBANK_SEND
        );

        for (Transaction transaction : pendingTransactions) {
            retryTransfer(transaction);
        }
    }

    private void retryTransfer(Transaction transaction) {
        try {
            log.info("Retrying interbank transfer: {}", transaction.getId());

            InboundTransferRequest request = InboundTransferRequest.builder()
                    .externalTransactionId(transaction.getExternalTransactionId())
                    .senderCustomerEmail(transaction.getSourceCustomerEmail())
                    .recipientEmail(transaction.getTargetCustomerEmail())
                    .amount(transaction.getAmount())
                    .senderBankId(transaction.getSenderBankId())
                    .currency(transaction.getCurrency())
                    .build();

            var response = interBankClient.sendTransferToBank(transaction.getReceiverBankId(), request);

            if ("ACCEPTED".equals(response.getStatus())) {
                transaction.setStatus(Transaction.TransactionStatus.PROCESSING);
                log.info("Retry successful for transfer: {}", transaction.getId());
            } else {
                log.warn("Retry rejected for transfer: {}", transaction.getId());
            }

            transaction.setUpdatedAt(Instant.now());
            transactionRepository.save(transaction);

        } catch (Exception e) {
            log.warn("Retry failed for transfer {}, will retry again later: {}", transaction.getId(), e.getMessage());
        }
    }
}
