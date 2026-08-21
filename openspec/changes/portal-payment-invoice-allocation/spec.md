# Spec: Portal Payment Invoice Allocation

## Requirement: Portal single-invoice payments record their target invoice structurally

### Scenario: Customer pays one invoice from the portal
- **Given** an authenticated portal customer with an unpaid invoice `INV-1`
- **When** they submit a payment for `INV-1` via `POST /api/portal/invoices/{invoiceId}/pay`
- **Then** a `Payment` is created with `status = PENDING`, `target_invoice_id = INV-1.id`, and `notes` containing no `"INVOICE_ID:"` marker

## Requirement: Portal debt payments leave target invoice unset

### Scenario: Customer pays total accumulated debt from the portal
- **Given** an authenticated portal customer with multiple unpaid invoices
- **When** they submit `POST /api/portal/debt/pay`
- **Then** a `Payment` is created with `status = PENDING` and `target_invoice_id = NULL`

### Scenario: Customer pays partial accumulated debt from the portal
- **Given** an authenticated portal customer with multiple unpaid invoices
- **When** they submit `POST /api/portal/debt/pay-partial` with an amount less than total debt
- **Then** a `Payment` is created with `status = PENDING` and `target_invoice_id = NULL`

## Requirement: Approving a targeted payment allocates exactly to that invoice

### Scenario: Staff approves a payment that targets a specific invoice
- **Given** a `PENDING` `Payment` with `target_invoice_id = INV-1.id` and `amount = 500`
- **When** staff approves it via `PUT /api/payments/{id}/approve`
- **Then** a `PaymentAllocation` of `500` is created for `INV-1`
- **And** `INV-1`'s status is recalculated
- **And** the `Payment`'s `appliedAmount` becomes `500`

## Requirement: Approving an untargeted payment spreads across unpaid invoices oldest-first

### Scenario: Staff approves a debt payment that exactly covers total outstanding debt
- **Given** a `PENDING` `Payment` with `target_invoice_id = NULL` for a customer with unpaid invoices `INV-1` (older, balance `300`) and `INV-2` (newer, balance `200`), `amount = 500`
- **When** staff approves it
- **Then** `PaymentAllocation`s are created: `300` for `INV-1`, `200` for `INV-2`
- **And** both invoices' statuses are recalculated
- **And** the `Payment`'s `appliedAmount` becomes `500`

### Scenario: Staff approves a debt payment smaller than total outstanding debt
- **Given** a `PENDING` `Payment` with `target_invoice_id = NULL` for a customer with unpaid invoices `INV-1` (older, balance `300`) and `INV-2` (newer, balance `200`), `amount = 250`
- **When** staff approves it
- **Then** a single `PaymentAllocation` of `250` is created for `INV-1` only
- **And** `INV-1`'s status is recalculated
- **And** `INV-2` is untouched

### Scenario: Staff approves a debt payment larger than total outstanding debt
- **Given** a `PENDING` `Payment` with `target_invoice_id = NULL` for a customer with unpaid invoices totaling `500` in balance, `amount = 700`
- **When** staff approves it
- **Then** `PaymentAllocation`s are created totaling exactly `500` across the unpaid invoices
- **And** the `Payment`'s `appliedAmount` becomes `500` (not `700`)

### Scenario: Staff approves a debt payment for a customer with no unpaid invoices
- **Given** a `PENDING` `Payment` with `target_invoice_id = NULL` for a customer with zero unpaid invoices
- **When** staff approves it
- **Then** no `PaymentAllocation` is created
- **And** the `Payment`'s `appliedAmount` remains `0`
- **And** approval still succeeds (payment status becomes `APPLIED`)

## Requirement: Voucher files are persisted structurally, not embedded in notes

### Scenario: Customer registers a payment with a voucher file
- **Given** an authenticated portal customer
- **When** they submit `POST /api/portal/payments/register` with a file part named `justificante`
- **Then** the created `Payment.voucher_path` is set to the stored file path
- **And** `Payment.notes` contains no `"Justificante:"` text

### Scenario: Customer pays an invoice with an attached voucher file
- **Given** an authenticated portal customer
- **When** they submit `POST /api/portal/invoices/{invoiceId}/pay` as multipart with a `justificante` file part
- **Then** the created `Payment.voucher_path` is set to the stored file path

### Scenario: Customer pays an invoice with no voucher file
- **Given** an authenticated portal customer
- **When** they submit `POST /api/portal/invoices/{invoiceId}/pay` as multipart with no file part
- **Then** the created `Payment.voucher_path` is `NULL`

## Requirement: Payment response DTOs expose the voucher URL

### Scenario: A payment with a voucher is fetched
- **Given** a `Payment` with `voucher_path = "payments/justificantes/abc.pdf"`
- **When** it's returned in `PaymentResponse` or `PaymentListResponse`
- **Then** `voucherUrl = "/api/files/payments/justificantes/abc.pdf"`

### Scenario: A payment without a voucher is fetched
- **Given** a `Payment` with `voucher_path = NULL`
- **When** it's returned in `PaymentResponse` or `PaymentListResponse`
- **Then** `voucherUrl = NULL`

## Requirement: Portal customers can check for an applicable pending payment

### Scenario: Pending payment exists for a specific invoice
- **Given** a customer has a `PENDING` `Payment` with `target_invoice_id = INV-1.id`
- **When** the portal calls `GET /api/portal/payments/pending-check?invoiceId={INV-1.id}`
- **Then** that payment is included in the response

### Scenario: Pending general-debt payment applies to any invoice query
- **Given** a customer has a `PENDING` `Payment` with `target_invoice_id = NULL`
- **When** the portal calls `GET /api/portal/payments/pending-check?invoiceId={any invoice of that customer}`
- **Then** that payment is included in the response

### Scenario: Checking for general debt only
- **Given** a customer has one `PENDING` `Payment` targeting a specific invoice and one `PENDING` `Payment` with `target_invoice_id = NULL`
- **When** the portal calls `GET /api/portal/payments/pending-check` (no `invoiceId`)
- **Then** only the `target_invoice_id = NULL` payment is included
