# CanBankX API Documentation

Base URL

http://localhost:8080/api/v1

---

# Customer Endpoints

Register customer

POST /customers/register

Body

{
"firstName": "John",
"lastName": "Doe",
"email": "john@bank2.com"
}

Response

{
"id":"uuid",
"firstName":"John",
"lastName":"Doe",
"email":"john@bank2.com",
"kycStatus":"PENDING",
"bankId":2
}

---

Get customer

GET /customers/{customerId}

---

Get customer by email

GET /customers/by-email/{email}

---

# Transaction Endpoints

Initiate transfer

POST /transactions/initiate-transfer

Body

{
"senderAccountId":"uuid",
"senderEmail":"john@bank2.com",
"recipientEmail":"jane@bank1.com",
"receiverBankId":1,
"amount":1000,
"currency":"CAD"
}

Response

{
"id":"txn-uuid",
"externalTransactionId":"TXN-123",
"status":"PROCESSING"
}

---

Get transaction

GET /transactions/{transactionId}

---

Get transaction by external ID

GET /transactions/by-external-id/{externalId}

---

# Settlement Webhook

Called by Central Bank

POST /settlements/notifications

Body

{
"externalTransactionId":"TXN-123",
"centralBankTransactionId":"CB-TXN-999",
"result":"SETTLED",
"settledAt":"timestamp"
}

Response

200 OK

