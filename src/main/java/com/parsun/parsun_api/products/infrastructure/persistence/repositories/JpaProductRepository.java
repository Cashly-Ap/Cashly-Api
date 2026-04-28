package com.parsun.parsun_api.products.infrastructure.persistence.repositories;

import com.parsun.parsun_api.products.domain.entities.Product;
import com.parsun.parsun_api.products.domain.repositories.ProductRepository;
import com.parsun.parsun_api.products.infrastructure.persistence.entities.ProductCodeSequenceEntity;
import com.parsun.parsun_api.products.infrastructure.persistence.entities.ProductEntity;
import com.parsun.parsun_api.shared.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class JpaProductRepository implements ProductRepository {

    private static final Set<String> ALLOWED_ORDER_BY = Set.of("name", "code", "createdAt", "updatedAt");
    private static final String CODE_PREFIX = "PRD-";
    private static final String CODE_FORMAT = CODE_PREFIX + "%05d";

    private final SpringDataProductRepository springDataRepository;
    private final SpringDataProductCodeSequenceRepository codeSequenceRepository;

    public JpaProductRepository(SpringDataProductRepository springDataRepository,
                                 SpringDataProductCodeSequenceRepository codeSequenceRepository) {
        this.springDataRepository = springDataRepository;
        this.codeSequenceRepository = codeSequenceRepository;
    }

    @Override
    public Product save(Product product) {
        if (product.getId() != null) {
            return springDataRepository.findWithComponentsById(product.getId())
                .map(entity -> {
                    entity.getComponents().clear();
                    springDataRepository.saveAndFlush(entity);
                    entity.updateFromDomain(product);
                    return springDataRepository.save(entity).toDomain();
                })
                .orElseGet(() -> springDataRepository.save(ProductEntity.fromDomain(product)).toDomain());
        }
        ProductEntity entity = ProductEntity.fromDomain(product);
        entity.setId(UUID.randomUUID());
        return springDataRepository.save(entity).toDomain();
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return springDataRepository.findWithComponentsById(id)
            .map(ProductEntity::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return springDataRepository.existsByCode(code);
    }

    @Override
    public boolean existsByCodeAndIdNot(String code, UUID id) {
        return springDataRepository.existsByCodeAndIdNot(code, id);
    }

    @Override
    public boolean existsByCategoryId(UUID categoryId) {
        return springDataRepository.existsByCategoryId(categoryId);
    }

    @Override
    public PageResponse<Product> findAllWithFilters(String search, UUID categoryId, Boolean active,
                                                     Boolean composite, int page, int size,
                                                     String orderBy, String orderDir) {
        String field = ALLOWED_ORDER_BY.contains(orderBy) ? orderBy : "name";
        Sort sort = "desc".equalsIgnoreCase(orderDir) ? Sort.by(field).descending() : Sort.by(field).ascending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        Page<ProductEntity> result = springDataRepository.findAllWithFilters(
            search, categoryId, active, composite, pageable);

        Page<Product> mapped = result.map(ProductEntity::toDomain);
        return PageResponse.from(mapped);
    }

    @Override
    public void delete(UUID id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public String generateNextCode() {
        ProductCodeSequenceEntity sequence = codeSequenceRepository.save(new ProductCodeSequenceEntity());
        return String.format(CODE_FORMAT, sequence.getId());
    }
}
