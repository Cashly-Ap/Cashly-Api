-- Give activity notifications an entity label argument so titles can name the
-- affected entity instead of the generic "New activity" wording.
UPDATE `notifications`
SET `message_arguments` = JSON_ARRAY_APPEND(
    `message_arguments`,
    '$',
    CONCAT(
        'i18n:notifications.activity.entity.',
        CASE `reference_type`
            WHEN 'QUOTE' THEN 'quote'
            WHEN 'ORDER' THEN 'order'
            WHEN 'PURCHASE' THEN 'purchase'
            WHEN 'PRODUCT' THEN 'product'
            WHEN 'CATEGORY' THEN 'category'
            WHEN 'PRICE_TYPE' THEN 'price_type'
            WHEN 'WAREHOUSE' THEN 'warehouse'
            WHEN 'INVOICE' THEN 'invoice'
            WHEN 'RETURN' THEN 'return'
            WHEN 'CREDIT_NOTE' THEN 'credit_note'
            WHEN 'DEBIT_NOTE' THEN 'debit_note'
            WHEN 'CASH_REGISTER' THEN 'cash_register'
            WHEN 'RECEIVABLE' THEN 'receivable'
            WHEN 'PAYMENT' THEN 'payment'
            WHEN 'PAYMENT_METHOD' THEN 'payment_method'
            WHEN 'CUSTOMER' THEN 'customer'
            WHEN 'SUPPLIER' THEN 'supplier'
            WHEN 'USER' THEN 'user'
            WHEN 'ROLE' THEN 'role'
            WHEN 'DEPARTMENT' THEN 'department'
            WHEN 'COMPANY' THEN 'company'
        END,
        '.label'
    )
)
WHERE `type` = 'ACTIVITY'
  AND `message_arguments` IS NOT NULL
  AND JSON_LENGTH(`message_arguments`) = 3
  AND `reference_type` IN (
    'QUOTE', 'ORDER', 'PURCHASE', 'PRODUCT', 'CATEGORY', 'PRICE_TYPE',
    'WAREHOUSE', 'INVOICE', 'RETURN', 'CREDIT_NOTE', 'DEBIT_NOTE',
    'CASH_REGISTER', 'RECEIVABLE', 'PAYMENT', 'PAYMENT_METHOD', 'CUSTOMER',
    'SUPPLIER', 'USER', 'ROLE', 'DEPARTMENT', 'COMPANY'
  );
