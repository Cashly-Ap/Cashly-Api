package com.cashly.cashly_api.partners.customers.profile.application.register.use_case;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityEvent;

import com.cashly.cashly_api.platform.notifications.application.ports.in.ActivityNotificationRecorder;

import com.cashly.cashly_api.partners.customers.profile.application.register.request.CreateCustomerRequest;
import com.cashly.cashly_api.partners.customers.profile.application.register.response.CustomerCreateResponse;
import com.cashly.cashly_api.partners.customers.profile.application.exceptions.DuplicateCustomerRifException;
import com.cashly.cashly_api.partners.customers.profile.application.mappers.CustomerMapper;
import com.cashly.cashly_api.partners.customers.profile.domain.entity.Customer;
import com.cashly.cashly_api.partners.customers.warehouse_access.domain.entity.CustomerWarehouseAccess;
import com.cashly.cashly_api.partners.customers.profile.domain.repository.CustomerRepository;
import com.cashly.cashly_api.partners.customers.warehouse_access.domain.repository.CustomerWarehouseAccessRepository;
import com.cashly.cashly_api.partners.customers.profile.domain.services.CustomerCodeGenerator;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.CreditDays;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.CreditLimit;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.CustomerAlias;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.CustomerCode;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.CustomerType;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.EmailAddress;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.LegalName;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.PhoneNumber;
import com.cashly.cashly_api.partners.customers.profile.domain.valueobjects.Rif;
import com.cashly.cashly_api.partners.customers.warehouse_access.application.ports.out.WarehouseDirectoryGateway;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import com.cashly.cashly_api.platform.audit.application.ports.in.Audited;

public class RegisterCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerCodeGenerator customerCodeGenerator;
    private final CustomerMapper customerMapper;
    private final CustomerWarehouseAccessRepository warehouseAccessRepository;
    private final WarehouseDirectoryGateway warehouseDirectory;

    private final ActivityNotificationRecorder activityNotifications;

    public RegisterCustomerUseCase(CustomerRepository customerRepository,
            CustomerCodeGenerator customerCodeGenerator,
            CustomerMapper customerMapper,
            CustomerWarehouseAccessRepository warehouseAccessRepository,
            WarehouseDirectoryGateway warehouseDirectory,
            ActivityNotificationRecorder activityNotifications) {
        this.activityNotifications = activityNotifications != null
                ? activityNotifications
                : ActivityNotificationRecorder.NONE;
        this.customerRepository = customerRepository;
        this.customerCodeGenerator = customerCodeGenerator;
        this.customerMapper = customerMapper;
        this.warehouseAccessRepository = warehouseAccessRepository;
        this.warehouseDirectory = warehouseDirectory;
    }

    @Transactional
    @Audited(module = "partners", entityType = "CUSTOMER", action = "CREATE")
    public CustomerCreateResponse execute(CreateCustomerRequest request,
            UUID grantedBy) {
        Rif rif = new Rif(request.rif());
        if (customerRepository.existsByRif(rif)) {
            throw new DuplicateCustomerRifException(rif.getValue());
        }

        CustomerCode code = customerCodeGenerator.generateNext();

        Customer customer = Customer.create(code,
                new LegalName(request.legalName()),
                CustomerType.valueOf(request.type()), rif,
                new CustomerAlias(request.alias()),
                new CreditDays(request.creditDays()),
                new CreditLimit(request.creditLimit()));

        if (!request.active()) {
            customer.deactivate();
        }

        customer.updateNotes(request.notes());
        customer.updatePriceTypeId(request.priceTypeId());

        request.emails()
                .forEach(e -> customer.addEmail(new EmailAddress(e.email())));
        request.phones()
                .forEach(p -> customer.addPhone(new PhoneNumber(p.phone())));
        request.addresses()
                .forEach(a -> customer.addAddress(a.address(), a.billing(),
                        a.active(), a.state(), a.municipality(),
                        a.postalCode()));

        Customer saved = customerRepository.save(customer);
        grantWarehouseAccess(saved.getId().getValue(), request.warehouseIds(),
                grantedBy);
        activityNotifications.record(ActivityEvent.of("CONTACTS_CUSTOMERS",
                "CREATE", "CUSTOMER", saved.getId().getValue(),
                saved.getLegalName().getValue(), grantedBy));
        return customerMapper.toCreateResponse(saved);
    }

    private void grantWarehouseAccess(UUID customerId,
            java.util.List<UUID> warehouseIds, UUID grantedBy) {
        for (UUID warehouseId : warehouseIds) {
            warehouseDirectory.requireById(warehouseId);
            warehouseAccessRepository.save(CustomerWarehouseAccess
                    .create(customerId, warehouseId, grantedBy));
        }
    }
}
