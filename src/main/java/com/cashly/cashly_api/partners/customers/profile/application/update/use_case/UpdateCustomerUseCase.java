package com.cashly.cashly_api.partners.customers.profile.application.update.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.partners.customers.profile.application.update.request.UpdateCustomerRequest;
import com.cashly.cashly_api.partners.customers.profile.application.dto.response.CustomerDetailResponse;
import com.cashly.cashly_api.partners.customers.profile.application.exceptions.CustomerNotFoundException;
import com.cashly.cashly_api.partners.customers.profile.application.exceptions.DuplicateCustomerRifException;
import com.cashly.cashly_api.partners.customers.profile.application.mappers.CustomerMapper;
import com.cashly.cashly_api.partners.customers.profile.domain.entity.Customer;
import com.cashly.cashly_api.partners.customers.profile.domain.repository.CustomerRepository;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.CreditDays;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.CreditLimit;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.CustomerAlias;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.CustomerId;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.CustomerType;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.EmailAddress;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.LegalName;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.PhoneNumber;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.Rif;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

public class UpdateCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    private final ActivityNotificationRecorder activityNotifications;

    public UpdateCustomerUseCase(CustomerRepository customerRepository,
            CustomerMapper customerMapper,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    @Transactional
    @Audited(module = "partners", entityType = "CUSTOMER", action = "EDIT")
    public CustomerDetailResponse execute(String id,
            UpdateCustomerRequest request) {
        CustomerId customerId = CustomerId.from(id);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found with id: " + id));

        Rif rif = new Rif(request.rif());
        if (customerRepository.existsByRifAndIdNot(rif, customerId)) {
            throw new DuplicateCustomerRifException(rif.getValue());
        }

        customer.updateLegalName(new LegalName(request.legalName()));
        customer.updateType(CustomerType.valueOf(request.type()));
        customer.updateRif(rif);
        customer.updateAlias(new CustomerAlias(request.alias()));
        customer.updateCreditDays(new CreditDays(request.creditDays()));
        customer.updateCreditLimit(new CreditLimit(request.creditLimit()));
        customer.updateNotes(request.notes());
        customer.updatePriceTypeId(request.priceTypeId());

        if (request.active()) {
            customer.activate();
        } else {
            customer.deactivate();
        }

        customer.clearEmails();
        customer.clearPhones();
        customer.clearAddresses();

        request.emails()
                .forEach(e -> customer.addEmail(new EmailAddress(e.email())));
        request.phones()
                .forEach(p -> customer.addPhone(new PhoneNumber(p.phone())));
        request.addresses()
                .forEach(a -> customer.addAddress(a.address(), a.billing(),
                        a.active(), a.state(), a.municipality(),
                        a.postalCode()));

        Customer saved = customerRepository.save(customer);
        activityNotifications.record(ActivityEvent.of("CONTACTS_CUSTOMERS",
                "EDIT", "CUSTOMER", saved.getId().getValue(),
                saved.getLegalName().getValue(), null));
        return customerMapper.toDetailResponse(saved,
                !customerRepository.hasAssociatedRecords(customer.getId()));
    }
}
