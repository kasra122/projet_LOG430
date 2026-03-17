-- Create Customer Table
CREATE TABLE customer (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    kyc_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    bank_id INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create Account Table
CREATE TABLE account (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customer(id),
    account_number VARCHAR(20) UNIQUE NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'CAD',
    balance DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create Transaction Table
CREATE TABLE transaction (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_account_id UUID REFERENCES account(id),
    target_account_id UUID REFERENCES account(id),
    source_customer_email VARCHAR(255),
    target_customer_email VARCHAR(255),
    sender_bank_id INTEGER,
    receiver_bank_id INTEGER,
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'CAD',
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    idempotency_key VARCHAR(255) UNIQUE,
    external_transaction_id VARCHAR(255) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    settled_at TIMESTAMP,
    expires_at TIMESTAMP
);

-- Create AuditLog Table
CREATE TABLE audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL REFERENCES transaction(id),
    action VARCHAR(255) NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create Indexes
CREATE INDEX idx_customer_email ON customer(email);
CREATE INDEX idx_account_customer_id ON account(customer_id);
CREATE INDEX idx_transaction_source_account ON transaction(source_account_id);
CREATE INDEX idx_transaction_target_account ON transaction(target_account_id);
CREATE INDEX idx_transaction_status ON transaction(status);
CREATE INDEX idx_transaction_idempotency_key ON transaction(idempotency_key);
CREATE INDEX idx_transaction_external_id ON transaction(external_transaction_id);
CREATE INDEX idx_transaction_created_at ON transaction(created_at);
CREATE INDEX idx_audit_log_transaction_id ON audit_log(transaction_id);