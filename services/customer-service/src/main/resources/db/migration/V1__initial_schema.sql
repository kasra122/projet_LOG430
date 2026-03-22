-- Customers
CREATE TABLE customers (
  id UUID PRIMARY KEY,
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  kyc_status VARCHAR(50) DEFAULT 'PENDING',
  bank_id INTEGER NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);

-- Accounts
CREATE TABLE accounts (
  id UUID PRIMARY KEY,
  customer_id UUID NOT NULL REFERENCES customers(id),
  account_number VARCHAR(255) UNIQUE NOT NULL,
  account_type VARCHAR(50) NOT NULL,
  balance DECIMAL(19,2) DEFAULT 0.00,
  currency VARCHAR(3) DEFAULT 'CAD',
  status VARCHAR(50) DEFAULT 'ACTIVE',
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);

-- Transactions
CREATE TABLE transactions (
  id UUID PRIMARY KEY,
  external_transaction_id VARCHAR(255) UNIQUE,
  source_account_id UUID NOT NULL REFERENCES accounts(id),
  target_account_id UUID REFERENCES accounts(id),
  source_customer_email VARCHAR(255),
  target_customer_email VARCHAR(255),
  sender_bank_id INTEGER NOT NULL,
  receiver_bank_id INTEGER NOT NULL,
  amount DECIMAL(19,2) NOT NULL,
  currency VARCHAR(3) DEFAULT 'CAD',
  type VARCHAR(50) NOT NULL,
  status VARCHAR(50) DEFAULT 'PENDING',
  idempotency_key VARCHAR(255) UNIQUE,
  central_bank_reference VARCHAR(255),
  settlement_status VARCHAR(50),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);

-- Audit Logs
CREATE TABLE audit_logs (
  id UUID PRIMARY KEY,
  entity_type VARCHAR(255) NOT NULL,
  entity_id VARCHAR(255) NOT NULL,
  action VARCHAR(50) NOT NULL,
  details TEXT,
  created_by VARCHAR(255),
  created_at TIMESTAMP NOT NULL
);

-- Banks
CREATE TABLE banks (
  id SERIAL PRIMARY KEY,
  bank_name VARCHAR(255) NOT NULL,
  bank_code VARCHAR(50) UNIQUE,
  created_at TIMESTAMP NOT NULL
);

-- OTP Tokens (MFA)
CREATE TABLE otp_tokens (
  id UUID PRIMARY KEY,
  customer_id UUID NOT NULL REFERENCES customers(id),
  token VARCHAR(255) NOT NULL UNIQUE,
  otp_code VARCHAR(6) NOT NULL,
  status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
  delivery_method VARCHAR(50) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  verified_at TIMESTAMP,
  attempt_count INTEGER NOT NULL DEFAULT 0
);

-- Sessions
CREATE TABLE sessions (
  id UUID PRIMARY KEY,
  customer_id UUID NOT NULL REFERENCES customers(id),
  token VARCHAR(255) UNIQUE NOT NULL,
  mfa_verified BOOLEAN DEFAULT false,
  status VARCHAR(50) DEFAULT 'ACTIVE',
  created_at TIMESTAMP NOT NULL,
  expires_at TIMESTAMP NOT NULL
);

-- Device Registrations
CREATE TABLE device_registrations (
  id UUID PRIMARY KEY,
  customer_id UUID NOT NULL REFERENCES customers(id),
  device_id VARCHAR(255) NOT NULL,
  device_name VARCHAR(255),
  status VARCHAR(50) DEFAULT 'PENDING',
  is_trusted BOOLEAN DEFAULT false,
  created_at TIMESTAMP NOT NULL
);

-- AML Rules
CREATE TABLE aml_rules (
  id UUID PRIMARY KEY,
  rule_name VARCHAR(255) NOT NULL,
  rule_type VARCHAR(50) NOT NULL,
  threshold DECIMAL(19,2),
  active BOOLEAN DEFAULT true,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);

-- Suspicious Activity Reports
CREATE TABLE suspicious_activity_reports (
  id UUID PRIMARY KEY,
  customer_id UUID NOT NULL REFERENCES customers(id),
  description TEXT,
  reported_to_fintrac BOOLEAN DEFAULT false,
  created_at TIMESTAMP NOT NULL
);

-- Indexes
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_accounts_customer_id ON accounts(customer_id);
CREATE INDEX idx_transactions_source_account ON transactions(source_account_id);
CREATE INDEX idx_transactions_target_account ON transactions(target_account_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
