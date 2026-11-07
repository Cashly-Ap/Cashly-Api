-- Translate the remaining seeded notifications and point the payment seed at the
-- invoice it actually references.
UPDATE `notifications`
SET `title_key` = 'notifications.payment_received.title',
    `message_key` = 'notifications.payment_received.message',
    `message_arguments` = JSON_ARRAY(),
    `reference_type` = 'INVOICE'
WHERE `title` = 'Payment received'
  AND `type` = 'PAYMENT_APPROVED';

UPDATE `notifications`
SET `title_key` = 'notifications.quote_created.title',
    `message_key` = 'notifications.quote_created.message',
    `message_arguments` = JSON_ARRAY('operator')
WHERE `title` = 'Quote created'
  AND `type` = 'QUOTE_CREATED';
