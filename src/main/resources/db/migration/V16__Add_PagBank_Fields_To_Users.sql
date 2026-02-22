-- Add optional PagBank fields to users table for "remember me" functionality
ALTER TABLE users ADD COLUMN pagbank_email VARCHAR(255);
ALTER TABLE users ADD COLUMN pagbank_cpf VARCHAR(14);
