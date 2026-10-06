package com.cashly.cashly_api.products.infrastructure.persistence.repositories;

import com.cashly.cashly_api.price_types.infrastructure.persistence.entities.PriceTypeEntity;
import com.cashly.cashly_api.price_types.infrastructure.persistence.repositories.SpringDataPriceTypeRepository;
import com.cashly.cashly_api.products.domain.entities.Product;
import com.cashly.cashly_api.products.domain.repositories.ProductRepository;
import com.cashly.cashly_api.products.domain.valueobjects.ProductPrice;
import com.cashly.cashly_api.products.infrastructure.persistence.entities.ProductCodeSequenceEntity;
import com.cashly.cashly_api.products.infrastructure.persistence.entities.ProductEntity;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JpaProductRepository implements ProductRepository {

    private static final Set<String> ALLOWED_ORDER_BY = Set.of("name", "code", "createdAt", "updatedAt");
    private static final String CODE_PREFIX = "PRD-";
    private static final String CODE_FORMAT = CODE_PREFIX + "%05d";

    private final SpringDataProductRepository springDataRepository;
    private final SpringDataProductCodeSequenceRepository codeSequenceRepository;
    private final SpringDataPriceTypeRepository springDataPriceTypeRepository;

    public JpaProductRepository(SpringDataProductRepository springDataRepository,
                                 SpringDataProductCodeSequenceRepository codeSequenceRepository,
                                 SpringDataPriceTypeRepository springDataPriceTypeRepository) {
        this.springDataRepository = springDataRepository;
        this.codeSequenceRepository = codeSequenceRepository;
        this.springDataPriceTypeRepository = springDataPriceTypeRepository;
    }

    @Override
    public Product save(Product product) {
        if (product.getId() != null) {
            return springDataRepository.findWithComponentsById(product.getId())
                .map(entity -> {
                    entity.getComponents().clear();
                    entity.getProductPrices().clear();
                    springDataRepository.saveAndFlush(entity);
                    Map<UUID, PriceTypeEntity> priceTypeMap = resolvePriceTypeMap(product.getProductPrices());
                    entity.updateFromDomain(product, priceTypeMap);
                    return springDataRepository.save(entity).toDomain();
                })
                .orElseGet(() -> {
                    ProductEntity entity = ProductEntity.fromDomain(product);
                    entity.setId(UUID.randomUUID());
                    Map<UUID, PriceTypeEntity> priceTypeMap = resolvePriceTypeMap(product.getProductPrices());
                    entity.applyPrices(product.getProductPrices(), priceTypeMap);
                    return springDataRepository.save(entity).toDomain();
                });
        }
        ProductEntity entity = ProductEntity.fromDomain(product);
        entity.setId(UUID.randomUUID());
        Map<UUID, PriceTypeEntity> priceTypeMap = resolvePriceTypeMap(product.getProductPrices());
        entity.applyPrices(product.getProductPrices(), priceTypeMap);
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
    public PageResponse<Product> findAllForOrder(String search, UUID categoryId, Boolean composite,
                                                  Boolean withStock, Set<UUID> allowedWarehouseIds,
                                                  int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        Page<ProductEntity> result = springDataRepository.findAllForOrder(
                search, categoryId, composite, withStock, allowedWarehouseIds, pageable);

        Page<Product> mapped = result.map(ProductEntity::toDomain);
        return PageResponse.from(mapped);
    }

    @Override
    public void delete(UUID id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public void deleteComponentReferencesByProductId(UUID productId) {
        springDataRepository.deleteByComponentProductId(productId);
    }

    @Override
    public Set<UUID> findIdsWithReferences(Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }
        return springDataRepository.findProductIdsWithReferences(ids);
    }

    @Override
    public String generateNextCode() {
        ProductCodeSequenceEntity sequence = codeSequenceRepository.save(new ProductCodeSequenceEntity());
        return String.format(CODE_FORMAT, sequence.getId());
    }

    private Map<UUID, PriceTypeEntity> resolvePriceTypeMap(List<ProductPrice> prices) {
        if (prices == null || prices.isEmpty()) {
            return Map.of();
        }
        List<UUID> priceTypeIds = prices.stream()
            .map(ProductPrice::getPriceTypeId)
            .distinct()
            .toList();
        return springDataPriceTypeRepository.findAllById(priceTypeIds).stream()
            .collect(Collectors.toMap(PriceTypeEntity::getId, Function.identity()));
    }
}
