alter table notifications
    add column title_key varchar(150) null,
    add column message_key varchar(150) null,
    add column message_arguments json null;

update notifications
set title_key = concat('notifications.', lower(type), '.title'),
    message_key = concat('notifications.', lower(type), '.message'),
    message_arguments = case
        when type = 'PAYMENT_PENDING_APPROVAL' then json_array(substring_index(message, ' registro un pago por ', 1), substring_index(message, ' registro un pago por ', -1))
        when type = 'PAYMENT_REJECTED' then json_array(substring(message, char_length('Tu pago fue rechazado: ') + 1))
        when type = 'QUOTE_CREATED' then json_array(substring(message, 1, char_length(message) - char_length(' ha creado un nuevo presupuesto.')))
        when type = 'QUOTE_REJECTED' then json_array(substring(message, char_length('Tu presupuesto fue rechazado: ') + 1))
        when type = 'QUOTE_CONVERTED_TO_ORDER' then json_array(trim(trailing '.' from substring_index(message, '#', -1)))
        when type = 'QUOTE_CONVERTED_TO_INVOICE' then json_array(trim(trailing '.' from substring_index(message, '#', -1)))
        when type = 'ORDER_CREATED' then json_array(substring(message, 1, char_length(message) - char_length(' ha creado un nuevo pedido.')))
        when type = 'ORDER_REJECTED' then json_array(substring(message, char_length('Tu pedido fue rechazado: ') + 1))
        when type = 'ORDER_CONVERTED_TO_INVOICE' then json_array(trim(trailing '.' from substring_index(message, '#', -1)))
        when type = 'INVOICE_CREATED' then json_array(trim(trailing '.' from substring_index(message, '#', -1)))
        when type = 'INVOICE_CONFIRMED' then json_array(substring_index(substring(message, char_length('Tu factura #') + 1), ' ha sido confirmada.', 1))
        when type = 'INVOICE_CANCELLED' then json_array(substring_index(substring(message, char_length('Tu factura #') + 1), ' fue cancelada: ', 1), substring_index(message, ' fue cancelada: ', -1))
        else json_array()
    end
where title in ('Pago pendiente de verificacion', 'Pago aprobado', 'Pago rechazado', 'Presupuesto creado',
    'Presupuesto aprobado', 'Presupuesto rechazado', 'Presupuesto convertido a pedido', 'Presupuesto convertido a factura',
    'Pedido creado', 'Pedido aprobado', 'Pedido rechazado', 'Pedido convertido a factura',
    'Factura creada', 'Factura confirmada', 'Factura cancelada');
