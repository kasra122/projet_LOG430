-- Add Central Bank integration fields to transactions table
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS central_bank_transaction_id VARCHAR(255);
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS rejection_reason VARCHAR(500);
ALTER TABLE transactions ADD COLUMN IF NOT EXISTS sent_to_central_bank_at TIMESTAMP;

-- Add index for central bank transaction ID lookups
CREATE INDEX IF NOT EXISTS idx_central_bank_id ON transactions(central_bank_transaction_id);
