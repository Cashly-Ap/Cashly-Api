-- Normalize legacy notification types to values accepted by NotificationType.
--
-- Earlier seed data stored generic PAYMENT and QUOTE types, which are not part
-- of the enum and made the inbox endpoint fail while decoding the row.
UPDATE `notifications` SET `type` = 'PAYMENT_APPROVED' WHERE `type` = 'PAYMENT';
UPDATE `notifications` SET `type` = 'QUOTE_CREATED' WHERE `type` = 'QUOTE';
