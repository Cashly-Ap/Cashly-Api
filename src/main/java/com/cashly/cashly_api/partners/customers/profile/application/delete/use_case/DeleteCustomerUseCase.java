package com.cashly.cashly_api.partners.customers.profile.application.delete.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.partners.customers.profile.application.delete.services.CustomerHasAssociatedRecordsException;
import com.cashly.cashly_api.partners.customers.profile.application.exceptions.CustomerNotFoundException;
import com.cashly.cashly_api.partners.customers.profile.domain.repository.CustomerRepository;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.CustomerId;
import org.springframework.transaction.annotation.Transactional;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

public class DeleteCustomerUseCase {

    private final CustomerRepository customerRepository;

    private final ActivityNotificationRecorder activityNotifications;

    public DeleteCustomerUseCase(CustomerRepository customerRepository,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.customerRepository = customerRepository;
    }

    @Transactional
    @Audited(module = "partners", entityType = "CUSTOMER", action = "DELETE")
    public void execute(String id) {
        CustomerId customerId = CustomerId.from(id);
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException(
                    "Customer not found with id: " + id);
        }
        if (customerRepository.hasAssociatedRecords(customerId)) {
            throw new CustomerHasAssociatedRecordsException(id);
        }
        activityNotifications.record(ActivityEvent.of("CONTACTS_CUSTOMERS",
                "DELETE", "CUSTOMER", customerId.getValue(),
                customerId.toString(), null));
        customerRepository.delete(customerId);
    }
}
