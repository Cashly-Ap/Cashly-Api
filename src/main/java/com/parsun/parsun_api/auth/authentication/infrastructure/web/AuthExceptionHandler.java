package com.parsun.parsun_api.auth.authentication.infrastructure.web;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.parsun.parsun_api.auth.authentication.application.exceptions.InvalidCredentialsException;
import com.parsun.parsun_api.auth.token.application.exceptions.InvalidTokenException;
import com.parsun.parsun_api.auth.token.application.exceptions.TokenExpiredException;
import com.parsun.parsun_api.auth.user.application.exceptions.DuplicateEmailException;
import com.parsun.parsun_api.auth.user.application.exceptions.UserNotFoundException;
import com.parsun.parsun_api.categories.application.exceptions.CategoryHasActiveProductsException;
import com.parsun.parsun_api.categories.application.exceptions.CategoryNotFoundException;
import com.parsun.parsun_api.categories.application.exceptions.DuplicateCategoryNameException;
import com.parsun.parsun_api.orders.domain.exceptions.OrderCannotBeDeletedException;
import com.parsun.parsun_api.orders.domain.exceptions.OrderNotFoundException;
import com.parsun.parsun_api.products.application.exceptions.CompositeProductCannotBeComponentException;
import com.parsun.parsun_api.products.application.exceptions.DuplicateProductCodeException;
import com.parsun.parsun_api.products.application.exceptions.DuplicateProductComponentException;
import com.parsun.parsun_api.products.application.exceptions.ProductNotFoundException;
import com.parsun.parsun_api.customers.application.exceptions.CustomerNotFoundException;
import com.parsun.parsun_api.customers.application.exceptions.CustomerWarehouseAccessAlreadyExistsException;
import com.parsun.parsun_api.customers.application.exceptions.CustomerWarehouseAccessNotFoundException;
import com.parsun.parsun_api.price_types.application.exceptions.DuplicatePriceTypeNameException;
import com.parsun.parsun_api.price_types.application.exceptions.PriceTypeInUseException;
import com.parsun.parsun_api.price_types.application.exceptions.PriceTypeNotFoundException;
import com.parsun.parsun_api.warehouses.application.exceptions.DuplicateWarehouseCodeException;
import com.parsun.parsun_api.warehouses.application.exceptions.InsufficientStockException;
import com.parsun.parsun_api.warehouses.application.exceptions.ProductNotInWarehouseException;
import com.parsun.parsun_api.warehouses.application.exceptions.WarehouseNotActiveException;
import com.parsun.parsun_api.warehouses.application.exceptions.WarehouseNotFoundException;
import com.parsun.parsun_api.quotes.domain.exceptions.QuoteNotFoundException;
import com.parsun.parsun_api.quotes.domain.exceptions.QuoteStateException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@ResponseBody
public class AuthExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEmail(DuplicateEmailException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.CONFLICT,
            "Email already registered",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDetails);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.NOT_FOUND,
            "User not found",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDetails);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.UNAUTHORIZED,
            "Invalid credentials",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDetails);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidToken(InvalidTokenException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.UNAUTHORIZED,
            "Invalid token",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDetails);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<Map<String, Object>> handleTokenExpired(TokenExpiredException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.UNAUTHORIZED,
            "Token expired",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDetails);
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCustomerNotFound(CustomerNotFoundException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.NOT_FOUND,
            "Customer not found",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDetails);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleOrderNotFound(OrderNotFoundException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.NOT_FOUND,
            "Order not found",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDetails);
    }

    @ExceptionHandler(OrderCannotBeDeletedException.class)
    public ResponseEntity<Map<String, Object>> handleOrderCannotBeDeleted(OrderCannotBeDeletedException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "Order cannot be deleted",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorDetails);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCategoryNotFound(CategoryNotFoundException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.NOT_FOUND,
            "Category not found",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDetails);
    }

    @ExceptionHandler(DuplicateCategoryNameException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateCategoryName(DuplicateCategoryNameException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.CONFLICT,
            "Duplicate category name",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDetails);
    }

    @ExceptionHandler(CategoryHasActiveProductsException.class)
    public ResponseEntity<Map<String, Object>> handleCategoryHasActiveProducts(CategoryHasActiveProductsException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.CONFLICT,
            "Category has active products",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDetails);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleProductNotFound(ProductNotFoundException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.NOT_FOUND,
            "Product not found",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDetails);
    }

    @ExceptionHandler(DuplicateProductCodeException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateProductCode(DuplicateProductCodeException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.CONFLICT,
            "Duplicate product code",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDetails);
    }

    @ExceptionHandler(CompositeProductCannotBeComponentException.class)
    public ResponseEntity<Map<String, Object>> handleCompositeProductCannotBeComponent(
            CompositeProductCannotBeComponentException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.CONFLICT,
            "Invalid component",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDetails);
    }

    @ExceptionHandler(DuplicateProductComponentException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateProductComponent(DuplicateProductComponentException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.CONFLICT,
            "Duplicate component",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDetails);
    }

    @ExceptionHandler(PriceTypeNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePriceTypeNotFound(PriceTypeNotFoundException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.NOT_FOUND,
            "Price type not found",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDetails);
    }

    @ExceptionHandler(DuplicatePriceTypeNameException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicatePriceTypeName(DuplicatePriceTypeNameException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.CONFLICT,
            "Duplicate price type name",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDetails);
    }

    @ExceptionHandler(PriceTypeInUseException.class)
    public ResponseEntity<Map<String, Object>> handlePriceTypeInUse(PriceTypeInUseException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.CONFLICT,
            "Price type in use",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDetails);
    }

    @ExceptionHandler(WarehouseNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleWarehouseNotFound(WarehouseNotFoundException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.NOT_FOUND,
            "Warehouse not found",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDetails);
    }

    @ExceptionHandler(DuplicateWarehouseCodeException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateWarehouseCode(DuplicateWarehouseCodeException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.CONFLICT,
            "Duplicate warehouse code",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDetails);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientStock(InsufficientStockException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.CONFLICT,
            "Insufficient stock",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDetails);
    }

    @ExceptionHandler(ProductNotInWarehouseException.class)
    public ResponseEntity<Map<String, Object>> handleProductNotInWarehouse(ProductNotInWarehouseException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.NOT_FOUND,
            "Product not in warehouse",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDetails);
    }

    @ExceptionHandler(WarehouseNotActiveException.class)
    public ResponseEntity<Map<String, Object>> handleWarehouseNotActive(WarehouseNotActiveException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "Warehouse not active",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorDetails);
    }

    @ExceptionHandler(CustomerWarehouseAccessAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleCustomerWarehouseAccessAlreadyExists(
            CustomerWarehouseAccessAlreadyExistsException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.CONFLICT,
            "Access already granted",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDetails);
    }

    @ExceptionHandler(CustomerWarehouseAccessNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCustomerWarehouseAccessNotFound(
            CustomerWarehouseAccessNotFoundException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.NOT_FOUND,
            "Warehouse access not found",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDetails);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.CONFLICT,
            "Invalid operation",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDetails);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Invalid request",
            ex.getMessage()
        );
        return ResponseEntity.badRequest().body(errorDetails);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));

        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Validation failed",
            errors
        );
        return ResponseEntity.badRequest().body(errorDetails);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException ex) {
        String message = "Invalid request body";
        Throwable cause = ex.getMostSpecificCause();
        if (cause != null) {
            message = cause.getMessage();
        }
        
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Invalid value",
            message
        );
        return ResponseEntity.badRequest().body(errorDetails);
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidFormat(InvalidFormatException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Invalid value",
            ex.getMessage()
        );
        return ResponseEntity.badRequest().body(errorDetails);
    }

    @ExceptionHandler(QuoteNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleQuoteNotFound(QuoteNotFoundException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.NOT_FOUND,
            "Quote not found",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDetails);
    }

    @ExceptionHandler(QuoteStateException.class)
    public ResponseEntity<Map<String, Object>> handleQuoteState(QuoteStateException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "Invalid quote state",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorDetails);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.FORBIDDEN,
            "Access denied",
            "You do not have permission to access this resource"
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorDetails);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        ex.printStackTrace();
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal server error",
            "An unexpected error occurred"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetails);
    }

    private Map<String, Object> createErrorResponse(HttpStatus status, String error, String message) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now().toString());
        errorDetails.put("status", status.value());
        errorDetails.put("error", error);
        errorDetails.put("message", message);
        return errorDetails;
    }
}
