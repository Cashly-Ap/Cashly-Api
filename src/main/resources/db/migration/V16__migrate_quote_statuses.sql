-- Migrate legacy quote statuses to simplified enum
-- SENT -> DRAFT (still pending action)
-- ACCEPTED -> DRAFT (accepted but not yet converted)
-- REJECTED -> CANCELLED (rejected = cancelled)
UPDATE quotes SET status = 'DRAFT' WHERE status IN ('SENT', 'ACCEPTED');
UPDATE quotes SET status = 'CANCELLED' WHERE status = 'REJECTED';
