package com.canbankx.customer.controller;

import com.canbankx.customer.dto.SettlementNotificationRequest;
import com.canbankx.customer.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    /**
     * Webhook endpoint called by Central Bank to notify settlement result.
     * Central Bank sends: externalTransactionId, result (SETTLED/REJECTED/EXPIRED), reason
     */
    @PostMapping("/notifications")
    public ResponseEntity<Void> receiveSettlementNotification(
            @RequestBody SettlementNotificationRequest notification) {
        
        try {
            log.info("Received settlement notification from Central Bank: txnId={}", 
                    notification.getExternalTransactionId());
            
            settlementService.processSettlement(notification);
            
            return ResponseEntity.ok().build();
        } catch (SettlementService.SettlementException e) {
            log.error("Settlement processing failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Unexpected error processing settlement notification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
