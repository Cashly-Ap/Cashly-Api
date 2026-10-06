package com.cashly.cashly_api.notifications.application.services;

import com.cashly.cashly_api.auth.user.domain.entities.User;
import com.cashly.cashly_api.auth.user.domain.repositories.UserRepository;
import com.cashly.cashly_api.auth.user.domain.valueobjects.UserRole;
import com.cashly.cashly_api.notifications.domain.entities.Notification;
import com.cashly.cashly_api.notifications.domain.repositories.NotificationRepository;
import com.cashly.cashly_api.notifications.domain.valueobjects.NotificationId;
import com.cashly.cashly_api.notifications.domain.valueobjects.NotificationType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository,
                                UserRepository userRepository,
                                SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public Notification create(UUID userId, NotificationType type, String title, String message,
                                String referenceType, UUID referenceId) {
        Notification notification = Notification.create(userId, null, type, title, message, referenceType, referenceId);
        Notification saved = notificationRepository.save(notification);
        pushToUser(userId, saved);
        return saved;
    }

    @Transactional
    public Notification createForCustomer(UUID customerId, NotificationType type, String title,
                                           String message, String referenceType, UUID referenceId) {
        Notification notification = Notification.create(null, customerId, type, title, message, referenceType, referenceId);
        Notification saved = notificationRepository.save(notification);
        pushToCustomer(customerId, saved);
        return saved;
    }

    private void pushToUser(UUID userId, Notification notification) {
        messagingTemplate.convertAndSend("/topic/notifications/user/" + userId, notification);
    }

    private void pushToCustomer(UUID customerId, Notification notification) {
        messagingTemplate.convertAndSend("/topic/notifications/customer/" + customerId, notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> listByUser(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Notification> listByCustomer(UUID customerId) {
        return notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
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
        Notification notification = notificationRepository.findById(NotificationId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + id));
        if (!userId.equals(notification.getUserId())) {
            throw new IllegalArgumentException("Notification does not belong to the current user");
        }
        notification.markAsRead();
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification markAsReadByCustomer(UUID id, UUID customerId) {
        Notification notification = notificationRepository.findById(NotificationId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + id));
        if (!customerId.equals(notification.getCustomerId())) {
            throw new IllegalArgumentException("Notification does not belong to the current customer");
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
        Notification notification = notificationRepository.findById(NotificationId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + id));
        if (!userId.equals(notification.getUserId())) {
            throw new IllegalArgumentException("Notification does not belong to the current user");
        }
        notificationRepository.deleteById(notification.getId());
    }

    @Transactional
    public void notifyPaymentPendingApproval(UUID adminUserId, UUID paymentId, String customerName, BigDecimal amount) {
        create(adminUserId, NotificationType.PAYMENT_PENDING_APPROVAL,
                "Pago pendiente de verificacion",
                String.format("%s registro un pago por %s", customerName, amount),
                "PAYMENT", paymentId);
    }

    @Transactional
    public void notifyPaymentApproved(UUID customerUserId, UUID paymentId) {
        create(customerUserId, NotificationType.PAYMENT_APPROVED,
                "Pago aprobado",
                "Tu pago ha sido verificado y aprobado.",
                "PAYMENT", paymentId);
    }

    @Transactional
    public void notifyPaymentRejected(UUID customerUserId, UUID paymentId, String reason) {
        create(customerUserId, NotificationType.PAYMENT_REJECTED,
                "Pago rechazado",
                "Tu pago fue rechazado: " + reason,
                "PAYMENT", paymentId);
    }

    @Transactional
    public void notifyAdminsPaymentPending(UUID paymentId, String customerName, BigDecimal amount) {
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            notifyPaymentPendingApproval(user.getId().getValue(), paymentId, customerName, amount);
        }
    }

    @Transactional
    public void notifyAdminsQuoteCreated(UUID quoteId, String customerName) {
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            create(user.getId().getValue(), NotificationType.QUOTE_CREATED, "Presupuesto creado", customerName + " ha creado un nuevo presupuesto.", "QUOTE", quoteId);
        }
    }

    @Transactional
    public void notifyAdminsOrderCreated(UUID orderId, String customerName) {
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            create(user.getId().getValue(), NotificationType.ORDER_CREATED, "Pedido creado", customerName + " ha creado un nuevo pedido.", "ORDER", orderId);
        }
    }

    @Transactional
    public void notifyCustomerPaymentApproved(UUID customerId, UUID paymentId) {
        createForCustomer(customerId, NotificationType.PAYMENT_APPROVED,
                "Pago aprobado",
                "Tu pago ha sido verificado y aprobado.",
                "PAYMENT", paymentId);
    }

    @Transactional
    public void notifyCustomerPaymentRejected(UUID customerId, UUID paymentId, String reason) {
        createForCustomer(customerId, NotificationType.PAYMENT_REJECTED,
                "Pago rechazado",
                "Tu pago fue rechazado: " + reason,
                "PAYMENT", paymentId);
    }

    @Transactional
    public void notifyCustomerQuoteApproved(UUID customerId, UUID quoteId) {
        createForCustomer(customerId, NotificationType.QUOTE_APPROVED,
                "Presupuesto aprobado",
                "Tu presupuesto ha sido aprobado.",
                "QUOTE", quoteId);
    }

    @Transactional
    public void notifyCustomerQuoteRejected(UUID customerId, UUID quoteId, String reason) {
        createForCustomer(customerId, NotificationType.QUOTE_REJECTED,
                "Presupuesto rechazado",
                "Tu presupuesto fue rechazado: " + reason,
                "QUOTE", quoteId);
    }

    @Transactional
    public void notifyCustomerQuoteConvertedToOrder(UUID customerId, UUID quoteId, String orderNumber) {
        createForCustomer(customerId, NotificationType.QUOTE_CONVERTED_TO_ORDER,
                "Presupuesto convertido a pedido",
                "Tu presupuesto ha sido convertido al pedido #" + orderNumber + ".",
                "QUOTE", quoteId);
    }

    @Transactional
    public void notifyCustomerQuoteConvertedToInvoice(UUID customerId, UUID quoteId, String invoiceNumber) {
        createForCustomer(customerId, NotificationType.QUOTE_CONVERTED_TO_INVOICE,
                "Presupuesto convertido a factura",
                "Tu presupuesto ha sido convertido a la factura #" + invoiceNumber + ".",
                "QUOTE", quoteId);
    }

    @Transactional
    public void notifyCustomerOrderApproved(UUID customerId, UUID orderId) {
        createForCustomer(customerId, NotificationType.ORDER_APPROVED,
                "Pedido aprobado",
                "Tu pedido ha sido aprobado.",
                "ORDER", orderId);
    }

    @Transactional
    public void notifyCustomerOrderRejected(UUID customerId, UUID orderId, String reason) {
        createForCustomer(customerId, NotificationType.ORDER_REJECTED,
                "Pedido rechazado",
                "Tu pedido fue rechazado: " + reason,
                "ORDER", orderId);
    }

    @Transactional
    public void notifyCustomerOrderConvertedToInvoice(UUID customerId, UUID orderId, String invoiceNumber) {
        createForCustomer(customerId, NotificationType.ORDER_CONVERTED_TO_INVOICE,
                "Pedido convertido a factura",
                "Tu pedido ha sido convertido a la factura #" + invoiceNumber + ".",
                "ORDER", orderId);
    }

    @Transactional
    public void notifyCustomerInvoiceCreated(UUID customerId, UUID invoiceId, String invoiceNumber) {
        createForCustomer(customerId, NotificationType.INVOICE_CREATED,
                "Factura creada", "Se ha creado la factura #" + invoiceNumber + ".", "INVOICE", invoiceId);
    }

    @Transactional
    public void notifyCustomerInvoiceConfirmed(UUID customerId, UUID invoiceId, String invoiceNumber) {
        createForCustomer(customerId, NotificationType.INVOICE_CONFIRMED,
                "Factura confirmada", "Tu factura #" + invoiceNumber + " ha sido confirmada.", "INVOICE", invoiceId);
    }

    @Transactional
    public void notifyCustomerInvoiceCancelled(UUID customerId, UUID invoiceId, String invoiceNumber, String reason) {
        createForCustomer(customerId, NotificationType.INVOICE_CANCELLED,
                "Factura cancelada", "Tu factura #" + invoiceNumber + " fue cancelada: " + reason,
                "INVOICE", invoiceId);
    }
}
