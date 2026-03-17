# CanBankX Customer Service Architecture

## Overview

CanBankX Customer Service is a **Spring Boot microservice** responsible for:

- Customer management
- Account ownership
- Inter-bank transfers
- Integration with a simulated Central Bank

Stack:

- Java 21
- Spring Boot 3
- PostgreSQL
- Gradle
- Flyway
- Docker

---

# System Architecture

Client Applications
        │
        ▼
API Gateway (Phase 2)
        │
        ▼
┌────────────────────────────────┐
│        Customer Service        │
│                                │
│  CustomerService               │
│  TransactionService            │
│  SettlementService             │
│  CentralBankClient             │
└───────────────┬────────────────┘
                │
        ┌───────┴────────┐
        ▼                ▼
  PostgreSQL        Central Bank
  Database          External API

---

# Components

## CustomerService

Responsible for customer lifecycle.

Methods:

- registerCustomer()
- getCustomerById()
- getCustomerByEmail()

---

## TransactionService

Manages transfer lifecycle.

Flow:

1 Validate account
2 Verify balance
3 Deduct funds
4 Create transaction record
5 Send transfer to Central Bank
6 Update status

---

## SettlementService

Handles Central Bank settlement callbacks.

Results handled:

- SETTLED
- REJECTED
- EXPIRED

Rejected or expired transfers trigger refunds.

---

## CentralBankClient

HTTP client responsible for communication with the Central Bank.

Features:

- retry (3 attempts)
- exponential backoff
- circuit breaker
- idempotency support

---

# Data Models

## Customer

id (UUID)

firstName  
lastName  
email (unique)

kycStatus

bankId

createdAt  
updatedAt

---

## Account

id (UUID)

customerId  
accountNumber

accountType (CHEQUING / SAVINGS)

balance

currency

status (ACTIVE / FROZEN / CLOSED)

---

## Transaction

id (UUID)

externalTransactionId  
centralBankTransactionId

sourceAccountId  
targetAccountId

senderBankId  
receiverBankId

amount  
currency

type

status

idempotencyKey

sentToCentralBankAt  
settledAt

---

# Transaction Status Flow

User initiates transfer

PENDING  
↓

PROCESSING  
↓

SETTLED ✓

or

REJECTED → REFUNDED

or

EXPIRED → REFUNDED

---

# Error Handling

## Central Bank Unavailable

Retry 3 times  
If still failing → refund sender.

---

## Transfer Rejected

Transaction marked REJECTED  
Funds refunded automatically.

---

## Expired Transfers

Central bank timeout (>24h)

Transaction marked EXPIRED  
Funds refunded.

---

# Performance Targets

Latency P95 ≤ 500ms

Throughput ≥ 600 ops/sec

Availability ≥ 95%

