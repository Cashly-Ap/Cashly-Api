-- Migrate existing quote numbers to include PRE prefix
-- From: 2026-0001 -> To: PRE-2026-0001
UPDATE quotes SET quote_number = CONCAT('PRE-', quote_number) WHERE quote_number NOT LIKE 'PRE-%';
