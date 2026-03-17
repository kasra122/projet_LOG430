# CanBankX Customer Service

Spring Boot microservice responsible for customer management and inter-bank transfers.

---

# Quick Start

Create database

createdb canbankx_customer

Run service

./gradlew bootRun

Health check

curl http://localhost:8080/actuator/health

---

# API

POST /api/v1/customers/register

GET /api/v1/customers/{customerId}

POST /api/v1/transactions/initiate-transfer

GET /api/v1/transactions/{transactionId}

POST /api/v1/settlements/notifications

---

# Tech Stack

Java 21

Spring Boot 3

PostgreSQL

Gradle

JUnit

Flyway

Docker

---

# Project Structure

src/main/java/com/canbankx/customer

domain
service
controller
repository
infrastructure
config

docs/

ARCHITECTURE.md  
API.md  
SETUP.md

---

# Performance Targets

Latency P95 ≤ 500 ms

Throughput ≥ 600 ops/sec

Availability ≥ 95 %

