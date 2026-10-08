CREATE TABLE invoice_order_links (
    invoice_id BINARY(16) NOT NULL,
    order_id BINARY(16) NOT NULL,
    PRIMARY KEY (invoice_id, order_id),
    CONSTRAINT fk_invoice_order_links_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE,
    CONSTRAINT fk_invoice_order_links_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE TABLE invoice_quote_links (
    invoice_id BINARY(16) NOT NULL,
    quote_id BINARY(16) NOT NULL,
    PRIMARY KEY (invoice_id, quote_id),
    CONSTRAINT fk_invoice_quote_links_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE,
    CONSTRAINT fk_invoice_quote_links_quote FOREIGN KEY (quote_id) REFERENCES quotes(id)
);
