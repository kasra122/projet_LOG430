package com.canbankx.customer.controller;

import com.canbankx.customer.domain.Statement;
import com.canbankx.customer.service.EODService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/statements")
@RequiredArgsConstructor
public class StatementController {

    private final EODService eodSvc;

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<Statement>> getStatements(
            @PathVariable UUID accountId) {
        List<Statement> stmts = eodSvc.getAccountStatements(accountId);
        return ResponseEntity.ok(stmts);
    }
}
