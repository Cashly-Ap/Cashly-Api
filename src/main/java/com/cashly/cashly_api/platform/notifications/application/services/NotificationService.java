package com.cashly.cashly_api.platform.notifications.application.services;

import com.cashly.cashly_api.platform.notifications.application.ports.out.NotificationRecipientGateway;
import com.cashly.cashly_api.platform.notifications.application.ports.out.NotificationDeliveryGateway;
import com.cashly.cashly_api.platform.notifications.domain.entity.Notification;
import com.cashly.cashly_api.platform.notifications.domain.repository.NotificationRepository;
import com.cashly.cashly_api.platform.notifications.domain.valueobjects.NotificationId;
import com.cashly.cashly_api.platform.notifications.domain.valueobjects.NotificationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationRecipientGateway recipientGateway;
    private final NotificationDeliveryGateway deliveryGateway;

    public NotificationService(NotificationRepository notificationRepository,
            NotificationRecipientGateway recipientGateway,
            NotificationDeliveryGateway deliveryGateway) {
        this.notificationRepository = notificationRepository;
        this.recipientGateway = recipientGateway;
        this.deliveryGateway = deliveryGateway;
    }

    @Transactional
    public Notification create(UUID userId, NotificationType type, String title,
            String message, String referenceType, UUID referenceId) {
        Notification notification = Notification.create(userId, null, type,
                title, message, referenceType, referenceId);
        Notification saved = notificationRepository.save(notification);
        deliveryGateway.deliver(saved);
        return saved;
    }

    @Transactional
    public Notification createForCustomer(UUID customerId,
            NotificationType type, String title, String message,
            String referenceType, UUID referenceId) {
        Notification notification = Notification.create(null, customerId, type,
                title, message, referenceType, referenceId);
        Notification saved = notificationRepository.save(notification);
        deliveryGateway.deliver(saved);
        return saved;
    }

    private Notification createLocalized(UUID userId, UUID customerId,
            NotificationType type, String referenceType, UUID referenceId,
            String... arguments) {
        String key = "notifications."
                + type.name().toLowerCase(java.util.Locale.ROOT);
        var notification = Notification
                .create(userId, customerId, type, key + ".title",
                        key + ".message", referenceType, referenceId)
                .withTranslation(key + ".title", key + ".message",
                        java.util.Arrays.asList(arguments));
        var saved = notificationRepository.save(notification);
        deliveryGateway.deliver(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Notification> listByUser(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Notification> listByCustomer(UUID customerId) {
        return notificationRepository
                .findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Transactional(readOnly = true)
    public long countUnread(UUID userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Transactional(readOnly = true)
    public long countUnreadByCustomer(UUID customerId) {
        return notificationRepository.countUnreadByCustomerId(customerId);
    }

    @Transactional
    public Notification markAsRead(UUID id, UUID userId) {
        Notification notification = notificationRepository
                .findById(NotificationId.of(id))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Notification not found: " + id));
        if (!userId.equals(notification.getUserId())) {
            throw new IllegalArgumentException(
                    "Notification does not belong to the current user");
        }
        notification.markAsRead();
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification markAsReadByCustomer(UUID id, UUID customerId) {
        Notification notification = notificationRepository
                .findById(NotificationId.of(id))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Notification not found: " + id));
        if (!customerId.equals(notification.getCustomerId())) {
            throw new IllegalArgumentException(
                    "Notification does not belong to the current customer");
        }
        notification.markAsRead();
        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Transactional
    public void delete(UUID id, UUID userId) {
        Notification notification = notificationRepository
                .findById(NotificationId.of(id))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Notification not found: " + id));
        if (!userId.equals(notification.getUserId())) {
            throw new IllegalArgumentException(
                    "Notification does not belong to the current user");
        }
        notificationRepository.deleteById(notification.getId());
    }

    @Transactional
    public void notifyPaymentPendingApproval(UUID adminUserId, UUID paymentId,
            String customerName, BigDecimal amount) {
        createLocalized(adminUserId, null,
                NotificationType.PAYMENT_PENDING_APPROVAL, "PAYMENT", paymentId,
                customerName, amount.toPlainString());
    }
    @Transactional
    public void notifyPaymentApproved(UUID userId, UUID paymentId) {
        createLocalized(userId, null, NotificationType.PAYMENT_APPROVED,
                "PAYMENT", paymentId);
    }
    @Transactional
    public void notifyPaymentRejected(UUID userId, UUID paymentId,
            String reason) {
        createLocalized(userId, null, NotificationType.PAYMENT_REJECTED,
                "PAYMENT", paymentId, reason);
    }
    @Transactional
    public void notifyAdminsPaymentPending(UUID paymentId, String customerName,
            BigDecimal amount) {
        for (var recipient : recipientGateway.findAllUserIds())
            notifyPaymentPendingApproval(recipient, paymentId, customerName,
                    amount);
    }
    @Transactional
    public void notifyAdminsQuoteCreated(UUID quoteId, String customerName) {
        for (var recipient : recipientGateway.findAllUserIds())
            createLocalized(recipient, null, NotificationType.QUOTE_CREATED,
                    "QUOTE", quoteId, customerName);
    }
    @Transactional
    public void notifyAdminsOrderCreated(UUID orderId, String customerName) {
        for (var recipient : recipientGateway.findAllUserIds())
            createLocalized(recipient, null, NotificationType.ORDER_CREATED,
                    "ORDER", orderId, customerName);
    }
    @Transactional
    public void notifyCustomerPaymentApproved(UUID customerId,
            UUID referenceId) {
        createLocalized(null, customerId, NotificationType.PAYMENT_APPROVED,
                "PAYMENT", referenceId);
    }
    @Transactional
    public void notifyCustomerPaymentRejected(UUID customerId, UUID referenceId,
            String reason) {
        createLocalized(null, customerId, NotificationType.PAYMENT_REJECTED,
                "PAYMENT", referenceId, reason);
    }
    @Transactional
    public void notifyCustomerQuoteApproved(UUID customerId, UUID referenceId) {
        createLocalized(null, customerId, NotificationType.QUOTE_APPROVED,
                "QUOTE", referenceId);
    }
    @Transactional
    public void notifyCustomerQuoteRejected(UUID customerId, UUID referenceId,
            String reason) {
        createLocalized(null, customerId, NotificationType.QUOTE_REJECTED,
                "QUOTE", referenceId, reason);
    }
    @Transactional
    public void notifyCustomerQuoteConvertedToOrder(UUID customerId,
            UUID referenceId, String orderNumber) {
        createLocalized(null, customerId,
                NotificationType.QUOTE_CONVERTED_TO_ORDER, "QUOTE", referenceId,
                orderNumber);
    }
    @Transactional
    public void notifyCustomerQuoteConvertedToInvoice(UUID customerId,
            UUID referenceId, String invoiceNumber) {
        createLocalized(null, customerId,
                NotificationType.QUOTE_CONVERTED_TO_INVOICE, "QUOTE",
                referenceId, invoiceNumber);
    }
    @Transactional
    public void notifyCustomerOrderApproved(UUID customerId, UUID referenceId) {
        createLocalized(null, customerId, NotificationType.ORDER_APPROVED,
                "ORDER", referenceId);
    }
    @Transactional
    public void notifyCustomerOrderRejected(UUID customerId, UUID referenceId,
            String reason) {
        createLocalized(null, customerId, NotificationType.ORDER_REJECTED,
                "ORDER", referenceId, reason);
    }
    @Transactional
    public void notifyCustomerOrderConvertedToInvoice(UUID customerId,
            UUID referenceId, String invoiceNumber) {
        createLocalized(null, customerId,
                NotificationType.ORDER_CONVERTED_TO_INVOICE, "ORDER",
                referenceId, invoiceNumber);
    }
    @Transactional
    public void notifyCustomerInvoiceCreated(UUID customerId, UUID referenceId,
            String invoiceNumber) {
        createLocalized(null, customerId, NotificationType.INVOICE_CREATED,
                "INVOICE", referenceId, invoiceNumber);
    }
    @Transactional
    public void notifyCustomerInvoiceConfirmed(UUID customerId,
            UUID referenceId, String invoiceNumber) {
        createLocalized(null, customerId, NotificationType.INVOICE_CONFIRMED,
                "INVOICE", referenceId, invoiceNumber);
    }
    @Transactional
    public void notifyCustomerInvoiceCancelled(UUID customerId,
            UUID referenceId, String invoiceNumber, String reason) {
        createLocalized(null, customerId, NotificationType.INVOICE_CANCELLED,
                "INVOICE", referenceId, invoiceNumber, reason);
    }
}
