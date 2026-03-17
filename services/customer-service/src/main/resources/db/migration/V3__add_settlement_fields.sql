-- Add settlement tracking fields
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS central_bank_transaction_id VARCHAR(255);
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS rejection_reason VARCHAR(500);
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS sent_to_central_bank_at TIMESTAMP;
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS settled_at TIMESTAMP;
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP;

-- Create indexes for new fields
CREATE INDEX IF NOT EXISTS idx_transactions_central_bank_id ON transactions(central_bank_transaction_id);
