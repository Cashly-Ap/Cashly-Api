package com.parsun.parsun_api.customers.infrastructure.persistence.repositories;

import com.parsun.parsun_api.customers.domain.entities.Customer;
import com.parsun.parsun_api.customers.domain.repositories.CustomerRepository;
import com.parsun.parsun_api.customers.domain.valueobjects.CustomerId;
import com.parsun.parsun_api.customers.infrastructure.persistence.entities.CustomerEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaCustomerRepository implements CustomerRepository {

    private final SpringDataCustomerRepository springDataRepository;

    public JpaCustomerRepository(SpringDataCustomerRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Customer save(Customer customer) {
        Optional<CustomerEntity> existing = springDataRepository.findById(customer.getId().getValue());

        CustomerEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            entity.updateFromDomain(customer);
        } else {
            entity = CustomerEntity.fromDomain(customer);
        }

        CustomerEntity saved = springDataRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Customer> findById(CustomerId id) {
        return springDataRepository.findById(id.getValue())
            .map(CustomerEntity::toDomain);
    }

    @Override
    public List<Customer> findAll() {
        return springDataRepository.findAll().stream()
            .map(CustomerEntity::toDomain)
            .toList();
    }

    @Override
    public void delete(CustomerId id) {
        springDataRepository.deleteById(id.getValue());
    }

    @Override
    public boolean existsById(CustomerId id) {
        return springDataRepository.existsById(id.getValue());
    }

    @Override
    public long getNextCode() {
        return springDataRepository.findMaxCode().orElse(0L) + 1;
    }
}
