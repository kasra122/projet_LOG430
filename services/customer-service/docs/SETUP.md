# Setup Guide

## Requirements

Java 21

PostgreSQL 14+

Gradle 8+

Docker (optional)

---

# Clone Repository

git clone https://github.com/kasra122/projet_LOG430.git

cd projet_LOG430

---

# Database Setup

Create database

createdb canbankx_customer

Create user

createuser canbankx_user -P

Grant privileges

psql -d canbankx_customer -c "GRANT ALL PRIVILEGES ON DATABASE canbankx_customer TO canbankx_user;"

---

# Configure Application

Edit:

src/main/resources/application.properties

Example:

spring.datasource.url=jdbc:postgresql://localhost:5432/canbankx_customer
spring.datasource.username=canbankx_user
spring.datasource.password=password

bank.id=2
bank.central-bank-url=http://central-bank:8090

---

# Run Application

./gradlew bootRun

Server runs on

http://localhost:8080

---

# Health Check

curl http://localhost:8080/actuator/health

---

# Run Tests

./gradlew test

---

# Useful Gradle Commands

./gradlew build
./gradlew clean
./gradlew bootRun
./gradlew test
./gradlew dependencies

