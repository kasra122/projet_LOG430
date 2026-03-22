package com.canbankx.customer.service;

import com.canbankx.customer.domain.Account;
import com.canbankx.customer.domain.Transaction;
import com.canbankx.customer.domain.Bank;
import com.canbankx.customer.dto.InterbankTransferRequest;
import com.canbankx.customer.dto.InterbankTransferResponse;
import com.canbankx.customer.repository.AccountRepository;
import com.canbankx.customer.repository.TransactionRepository;
import com.canbankx.customer.repository.BankRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterbankTransactionService {

    private final TransactionRepository txnRepo;
    private final AccountRepository acctRepo;
    private final BankRepository bkRepo;
    private final AccountService acctSvc;

    @Transactional
    public InterbankTransferResponse initiateInterbankTransfer(InterbankTransferRequest req) {
        log.info("Initiating interbank transfer from: {} to {}", 
                req.getSourceAccountId(), req.getRecipientEmail());

        Account srcAcct = acctRepo.findById(req.getSourceAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        Bank recipientBank = bkRepo.findById(req.getRecipientBankId())
                .orElseThrow(() -> new RuntimeException("Bank not found"));

        if (srcAcct.getBalance().compareTo(req.getAmount()) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        acctSvc.deductBalance(srcAcct, req.getAmount());

        Transaction t = Transaction.builder()
                .id(UUID.randomUUID())
                .sourceAccountId(req.getSourceAccountId())
                .targetCustomerEmail(req.getRecipientEmail())
                .receiverBankId(req.getRecipientBankId())
                .amount(req.getAmount())
                .currency(req.getCurrency() != null ? req.getCurrency() : "CAD")
                .type(Transaction.TransactionType.TRANSFER)
                .status(Transaction.TransactionStatus.PENDING)
                .idempotencyKey(req.getIdempotencyKey())
                .createdAt(Instant.now())
                .build();

        txnRepo.save(t);

        return InterbankTransferResponse.builder()
                .id(t.getId())
                .status("INITIATED")
                .amount(req.getAmount())
                .recipientEmail(req.getRecipientEmail())
                .recipientBankId(req.getRecipientBankId())
                .createdAt(t.getCreatedAt())
                .build();
    }

    @Transactional
    public void handleTransferFailure(UUID transactionId) {
        Transaction txn = txnRepo.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        Account srcAcct = acctRepo.findById(txn.getSourceAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        acctSvc.refundBalance(srcAcct, txn.getAmount());
        txn.setStatus(Transaction.TransactionStatus.PENDING);
        txnRepo.save(txn);

        log.info("Transfer completed for transaction: {}", transactionId);
    }
}
