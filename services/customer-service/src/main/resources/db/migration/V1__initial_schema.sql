-- Create customers table
CREATE TABLE IF NOT EXISTS customers (
  id UUID PRIMARY KEY,
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  kyc_status VARCHAR(50) DEFAULT 'PENDING',
  bank_id INTEGER NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);

-- Create accounts table
CREATE TABLE IF NOT EXISTS accounts (
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

-- Create transactions table
CREATE TABLE IF NOT EXISTS transactions (
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
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);

-- Create audit_logs table
CREATE TABLE IF NOT EXISTS audit_logs (
  id UUID PRIMARY KEY,
  entity_type VARCHAR(255) NOT NULL,
  entity_id VARCHAR(255) NOT NULL,
  action VARCHAR(50) NOT NULL,
  old_value TEXT,
  new_value TEXT,
  created_by VARCHAR(255),
  created_at TIMESTAMP NOT NULL
);

-- Create indexes
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_accounts_customer_id ON accounts(customer_id);
CREATE INDEX idx_transactions_source_account ON transactions(source_account_id);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_external_id ON transactions(external_transaction_id);
