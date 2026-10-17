package com.cashly.cashly_api.infrastructure.config;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.cashly.cashly_api.auth.authentication.application.exceptions.InvalidCredentialsException;
import com.cashly.cashly_api.auth.token.application.exceptions.InvalidTokenException;
import com.cashly.cashly_api.auth.token.application.exceptions.TokenExpiredException;
import com.cashly.cashly_api.auth.user.application.exceptions.DuplicateEmailException;
import com.cashly.cashly_api.auth.user.application.exceptions.UserHasAssociatedRecordsException;
import com.cashly.cashly_api.auth.user.application.exceptions.UserNotFoundException;
import com.cashly.cashly_api.catalog.categories.application.exceptions.CategoryHasActiveProductsException;
import com.cashly.cashly_api.customers.profile.application.delete.services.CustomerHasAssociatedRecordsException;
import com.cashly.cashly_api.catalog.categories.application.exceptions.CategoryNotFoundException;
import com.cashly.cashly_api.catalog.categories.application.exceptions.DuplicateCategoryNameException;
import com.cashly.cashly_api.customers.profile.application.exceptions.CustomerNotFoundException;
import com.cashly.cashly_api.customers.profile.application.exceptions.DuplicateCustomerRifException;
import com.cashly.cashly_api.customers.warehouse_access.application.exceptions.CustomerWarehouseAccessAlreadyExistsException;
import com.cashly.cashly_api.customers.warehouse_access.application.exceptions.CustomerWarehouseAccessNotFoundException;
import com.cashly.cashly_api.finance.billing.invoices.domain.exceptions.InvoiceNotFoundException;
import com.cashly.cashly_api.finance.billing.invoices.domain.exceptions.InvoiceNotReturnableException;
import com.cashly.cashly_api.finance.billing.invoices.domain.exceptions.ReturnQuantityExceedsException;
import com.cashly.cashly_api.orders.domain.exceptions.OrderCannotBeDeletedException;
import com.cashly.cashly_api.orders.domain.exceptions.OrderNotFoundException;
import com.cashly.cashly_api.orders.domain.exceptions.OrderNotPendingException;
import com.cashly.cashly_api.catalog.products.application.exceptions.CompositeProductCannotBeComponentException;
import com.cashly.cashly_api.catalog.products.application.exceptions.DuplicateProductCodeException;
import com.cashly.cashly_api.catalog.products.application.exceptions.DuplicateProductComponentException;
import com.cashly.cashly_api.catalog.products.application.exceptions.ProductHasAssociatedRecordsException;
import com.cashly.cashly_api.catalog.products.application.exceptions.ProductHasStockException;
import com.cashly.cashly_api.catalog.products.application.exceptions.ProductNotFoundException;
import com.cashly.cashly_api.suppliers.application.exceptions.SupplierHasAssociatedRecordsException;
import com.cashly.cashly_api.catalog.price_types.application.exceptions.DuplicatePriceTypeNameException;
import com.cashly.cashly_api.catalog.price_types.application.exceptions.PriceTypeInUseException;
import com.cashly.cashly_api.catalog.price_types.application.exceptions.PriceTypeNotFoundException;
import com.cashly.cashly_api.quotes.domain.exceptions.InvalidQuoteAllocationException;
import com.cashly.cashly_api.quotes.domain.exceptions.InvalidQuoteLineException;
import com.cashly.cashly_api.quotes.domain.exceptions.QuoteNotFoundException;
import com.cashly.cashly_api.quotes.domain.exceptions.QuoteStateException;
import com.cashly.cashly_api.customers.credit.application.exceptions.CreditLimitExceededException;
import com.cashly.cashly_api.inventory.warehouses.management.application.create.services.DuplicateWarehouseCodeException;
import com.cashly.cashly_api.inventory.warehouses.stock.application.exceptions.InsufficientStockException;
import com.cashly.cashly_api.inventory.warehouses.stock.application.exceptions.ProductNotInWarehouseException;
import com.cashly.cashly_api.inventory.warehouses.management.application.exceptions.WarehouseNotActiveException;
import com.cashly.cashly_api.inventory.warehouses.management.application.delete.services.WarehouseHasActiveStockException;
import com.cashly.cashly_api.inventory.warehouses.management.application.exceptions.WarehouseNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private Map<String, Object> createErrorResponse(HttpStatus status, String error, String message) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now().toString());
        errorDetails.put("status", status.value());
        errorDetails.put("error", error);
        errorDetails.put("message", message);
        return errorDetails;
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEmail(DuplicateEmailException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createErrorResponse(HttpStatus.CONFLICT, "Email already registered", ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse(HttpStatus.NOT_FOUND, "User not found", ex.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(createErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid credentials", ex.getMessage()));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidToken(InvalidTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(createErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid token", ex.getMessage()));
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<Map<String, Object>> handleTokenExpired(TokenExpiredException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(createErrorResponse(HttpStatus.UNAUTHORIZED, "Token expired", ex.getMessage()));
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCustomerNotFound(CustomerNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse(HttpStatus.NOT_FOUND, "Customer not found", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateCustomerRifException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateCustomerRif(DuplicateCustomerRifException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createErrorResponse(HttpStatus.CONFLICT, "Duplicate customer RIF", ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String causeMessage = ex.getMostSpecificCause().getMessage();
        if (causeMessage != null && causeMessage.contains("uk_customers_rif")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse(
                            HttpStatus.CONFLICT,
                            "Duplicate customer RIF",
                            "A customer with that RIF is already registered"
                    ));
        }

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createErrorResponse(
                        HttpStatus.CONFLICT,
                        "Data integrity violation",
                        "The operation conflicts with existing data"
                ));
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleOrderNotFound(OrderNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse(HttpStatus.NOT_FOUND, "Order not found", ex.getMessage()));
    }

    @ExceptionHandler(OrderCannotBeDeletedException.class)
    public ResponseEntity<Map<String, Object>> handleOrderCannotBeDeleted(OrderCannotBeDeletedException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(createErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, "Order cannot be deleted", ex.getMessage()));
    }

    @ExceptionHandler(OrderNotPendingException.class)
    public ResponseEntity<Map<String, Object>> handleOrderNotPending(OrderNotPendingException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(createErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid order state", ex.getMessage()));
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCategoryNotFound(CategoryNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse(HttpStatus.NOT_FOUND, "Category not found", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateCategoryNameException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateCategoryName(DuplicateCategoryNameException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createErrorResponse(HttpStatus.CONFLICT, "Duplicate category name", ex.getMessage()));
    }

    @ExceptionHandler(CategoryHasActiveProductsException.class)
    public ResponseEntity<Map<String, Object>> handleCategoryHasActiveProducts(CategoryHasActiveProductsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createErrorResponse(HttpStatus.CONFLICT, "Category has active products", ex.getMessage()));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleProductNotFound(ProductNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse(HttpStatus.NOT_FOUND, "Product not found", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateProductCodeException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateProductCode(DuplicateProductCodeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createErrorResponse(HttpStatus.CONFLICT, "Duplicate product code", ex.getMessage()));
    }

    @ExceptionHandler(CompositeProductCannotBeComponentException.class)
    public ResponseEntity<Map<String, Object>> handleCompositeProductCannotBeComponent(
            CompositeProductCannotBeComponentException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createErrorResponse(HttpStatus.CONFLICT, "Invalid component", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateProductComponentException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateProductComponent(DuplicateProductComponentException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createErrorResponse(HttpStatus.CONFLICT, "Duplicate component", ex.getMessage()));
    }

    @ExceptionHandler(ProductHasAssociatedRecordsException.class)
    public ResponseEntity<Map<String, Object>> handleProductHasAssociatedRecords(ProductHasAssociatedRecordsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createErrorResponse(HttpStatus.CONFLICT, "Product has associated records", ex.getMessage()));
    }

    @ExceptionHandler(UserHasAssociatedRecordsException.class)
    public ResponseEntity<Map<String, Object>> handleUserHasAssociatedRecords(UserHasAssociatedRecordsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createErrorResponse(HttpStatus.CONFLICT, "User has associated records", ex.getMessage()));
    }

    @ExceptionHandler(SupplierHasAssociatedRecordsException.class)
    public ResponseEntity<Map<String, Object>> handleSupplierHasAssociatedRecords(SupplierHasAssociatedRecordsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createErrorResponse(HttpStatus.CONFLICT, "Supplier has associated records", ex.getMessage()));
    }

    @ExceptionHandler(ProductHasStockException.class)
    public ResponseEntity<Map<String, Object>> handleProductHasStock(ProductHasStockException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createErrorResponse(HttpStatus.CONFLICT, "Product has stock", ex.getMessage()));
    }

    @ExceptionHandler(PriceTypeNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePriceTypeNotFound(PriceTypeNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse(HttpStatus.NOT_FOUND, "Price type not found", ex.getMessage()));
    }

    @ExceptionHandler(DuplicatePriceTypeNameException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicatePriceTypeName(DuplicatePriceTypeNameException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createErrorResponse(HttpStatus.CONFLICT, "Duplicate price type name", ex.getMessage()));
    }

    @ExceptionHandler(PriceTypeInUseException.class)
    public ResponseEntity<Map<String, Object>> handlePriceTypeInUse(PriceTypeInUseException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createErrorResponse(HttpStatus.CONFLICT, "Price type in use", ex.getMessage()));
    }

    @ExceptionHandler(WarehouseHasActiveStockException.class)
    public ResponseEntity<Map<String, Object>> handleWarehouseHasActiveStock(WarehouseHasActiveStockException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createErrorResponse(HttpStatus.CONFLICT, "Warehouse has active stock", ex.getMessage()));
    }

    @ExceptionHandler(WarehouseNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleWarehouseNotFound(WarehouseNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse(HttpStatus.NOT_FOUND, "Warehouse not found", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateWarehouseCodeException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateWarehouseCode(DuplicateWarehouseCodeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createErrorResponse(HttpStatus.CONFLICT, "Duplicate warehouse code", ex.getMessage()));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientStock(InsufficientStockException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createErrorResponse(HttpStatus.CONFLICT, "Insufficient stock", ex.getMessage()));
    }

    @ExceptionHandler(ProductNotInWarehouseException.class)
    public ResponseEntity<Map<String, Object>> handleProductNotInWarehouse(ProductNotInWarehouseException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse(HttpStatus.NOT_FOUND, "Product not in warehouse", ex.getMessage()));
    }

    @ExceptionHandler(WarehouseNotActiveException.class)
    public ResponseEntity<Map<String, Object>> handleWarehouseNotActive(WarehouseNotActiveException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(createErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, "Warehouse not active", ex.getMessage()));
    }

    @ExceptionHandler(CustomerHasAssociatedRecordsException.class)
    public ResponseEntity<Map<String, Object>> handleCustomerHasAssociatedRecords(CustomerHasAssociatedRecordsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createErrorResponse(HttpStatus.CONFLICT, "Customer has associated records", ex.getMessage()));
    }

    @ExceptionHandler(CustomerWarehouseAccessAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleCustomerWarehouseAccessAlreadyExists(
            CustomerWarehouseAccessAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createErrorResponse(HttpStatus.CONFLICT, "Access already granted", ex.getMessage()));
    }

    @ExceptionHandler(CustomerWarehouseAccessNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCustomerWarehouseAccessNotFound(
            CustomerWarehouseAccessNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse(HttpStatus.NOT_FOUND, "Warehouse access not found", ex.getMessage()));
    }

    @ExceptionHandler(QuoteNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleQuoteNotFound(QuoteNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse(HttpStatus.NOT_FOUND, "Quote not found", ex.getMessage()));
    }

    @ExceptionHandler(QuoteStateException.class)
    public ResponseEntity<Map<String, Object>> handleQuoteState(QuoteStateException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(createErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid quote state", ex.getMessage()));
    }

    @ExceptionHandler(InvalidQuoteLineException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidQuoteLine(InvalidQuoteLineException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid quote line", ex.getMessage()));
    }

    @ExceptionHandler(InvalidQuoteAllocationException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidQuoteAllocation(InvalidQuoteAllocationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid allocation", ex.getMessage()));
    }

    @ExceptionHandler(InvoiceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleInvoiceNotFound(InvoiceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse(HttpStatus.NOT_FOUND, "Invoice not found", ex.getMessage()));
    }

    @ExceptionHandler(InvoiceNotReturnableException.class)
    public ResponseEntity<Map<String, Object>> handleInvoiceNotReturnable(InvoiceNotReturnableException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(createErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, "Invoice not returnable", ex.getMessage()));
    }

    @ExceptionHandler(ReturnQuantityExceedsException.class)
    public ResponseEntity<Map<String, Object>> handleReturnQuantityExceeds(ReturnQuantityExceedsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse(HttpStatus.BAD_REQUEST, "Return quantity exceeds allowed", ex.getMessage()));
    }

    @ExceptionHandler(CreditLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleCreditLimitExceeded(CreditLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createErrorResponse(HttpStatus.CONFLICT, "Credit limit exceeded", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createErrorResponse(HttpStatus.CONFLICT, "Invalid operation", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid request", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest()
                .body(createErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed", errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String message = "Invalid request body";
        Throwable cause = ex.getMostSpecificCause();
        if (cause != null) {
            message = cause.getMessage();
        }
        return ResponseEntity.badRequest()
                .body(createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid value", message));
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidFormat(InvalidFormatException ex) {
        return ResponseEntity.badRequest()
                .body(createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid value", ex.getMessage()));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAuthorizationDenied(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(createErrorResponse(HttpStatus.FORBIDDEN, "Access denied",
                        "You do not have permission to access this resource"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String name = ex.getName();
        String value = ex.getValue() != null ? ex.getValue().toString() : "null";
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        log.warn("Type mismatch for parameter '{}': value '{}' cannot be converted to {}", name, value, requiredType);
        return ResponseEntity.badRequest()
                .body(createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid parameter",
                        "Invalid value '" + value + "' for parameter '" + name
                                + "'. Expected a valid " + requiredType + "."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error",
                        ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred"));
    }
}
