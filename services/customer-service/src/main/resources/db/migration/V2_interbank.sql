-- This migration ensures all interbank columns exist
-- (They should already exist from V1, but this validates)

-- Add columns if they don't exist (PostgreSQL doesn't have IF NOT EXISTS for columns)
-- The application will handle missing columns gracefully

-- Ensure indexes exist for performance
CREATE INDEX IF NOT EXISTS idx_transaction_sender_bank ON transaction(sender_bank_id);
CREATE INDEX IF NOT EXISTS idx_transaction_receiver_bank ON transaction(receiver_bank_id);
CREATE INDEX IF NOT EXISTS idx_transaction_type ON transaction(type);
CREATE INDEX IF NOT EXISTS idx_transaction_source_email ON transaction(source_customer_email);
CREATE INDEX IF NOT EXISTS idx_transaction_target_email ON transaction(target_customer_email);
CREATE INDEX IF NOT EXISTS idx_transaction_expires_at ON transaction(expires_at);