package com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.persistence.queries;

import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.model.StockMovementFilter;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.model.StockMovementGroupBy;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementGroup;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementsResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementsResponse.ProductOption;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementsResponse.Summary;
import com.cashly.cashly_api.inventory.warehouses.stock.application.movements.response.StockMovementsResponse.WarehouseOption;
import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.out.StockMovementQueryGateway;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.MovementType;
import com.cashly.cashly_api.inventory.warehouses.stock.domain.valueobjects.ReferenceType;
import com.cashly.cashly_api.shared.dto.PageResponse;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Repository
public class JdbcStockMovementQueryAdapter
        implements
            StockMovementQueryGateway {

    private static final String FROM_MOVEMENTS = """
            FROM stock_movements sm
            JOIN warehouses w ON w.id = sm.warehouse_id
            JOIN products p ON p.id = sm.product_id
            LEFT JOIN categories c ON c.id = p.category_id
            """;

    private static final String ENTRY_QUANTITY = """
            COALESCE(SUM(CASE WHEN sm.movement_type IN (%s)
              THEN sm.quantity ELSE 0 END), 0) AS entry_quantity
            """.formatted(movementTypesIn(MovementType::isEntry));

    private static final String EXIT_QUANTITY = """
            COALESCE(SUM(CASE WHEN sm.movement_type IN (%s)
              THEN sm.quantity ELSE 0 END), 0) AS exit_quantity
            """.formatted(movementTypesIn(MovementType::isExit));

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcStockMovementQueryAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public StockMovementsResponse search(StockMovementFilter filter) {
        var parameters = new MapSqlParameterSource();
        var where = new StringBuilder(" WHERE 1 = 1 ");
        applyFilters(filter, where, parameters);
        Totals totals = countTotals(where.toString(), parameters);
        parameters.addValue("limit", filter.size()).addValue("offset",
                (long) filter.page() * filter.size());
        var warehouses = findWarehouses();
        var products = findProducts();
        if (filter.groupBy() == StockMovementGroupBy.NONE) {
            var movements = queryMovements(where.toString(), parameters, true);
            return StockMovementsResponse.ungrouped(totals.summary(),
                    warehouses, products,
                    page(movements, totals.summary().movementCount(), filter));
        }
        var groups = queryGroups(filter, where.toString(), parameters);
        var summary = totals.summary();
        long groupCount = switch (filter.groupBy()) {
            case PRODUCT -> summary.productCount();
            case WAREHOUSE -> summary.warehouseCount();
            case MOVEMENT_TYPE -> totals.movementTypeCount();
            case NONE -> summary.movementCount();
        };
        return StockMovementsResponse.grouped(totals.summary(), warehouses,
                products, filter.groupBy(), page(groups, groupCount, filter));
    }

    private void applyFilters(StockMovementFilter filter, StringBuilder where,
            MapSqlParameterSource parameters) {
        if (filter.warehouseId() != null) {
            where.append(" AND sm.warehouse_id = :warehouseId ");
            parameters.addValue("warehouseId", bytes(filter.warehouseId()));
        }
        if (filter.productId() != null) {
            where.append(" AND sm.product_id = :productId ");
            parameters.addValue("productId", bytes(filter.productId()));
        }
        if (filter.movementType() != null) {
            where.append(" AND sm.movement_type = :movementType ");
            parameters.addValue("movementType", filter.movementType().name());
        }
        if (!filter.search().isEmpty()) {
            where.append("""
                    AND (LOWER(p.name) LIKE :search ESCAPE '!'
                      OR LOWER(p.code) LIKE :search ESCAPE '!'
                      OR LOWER(w.name) LIKE :search ESCAPE '!'
                      OR LOWER(sm.reference_number) LIKE :search ESCAPE '!')
                    """);
            parameters.addValue("search", "%" + filter.search()
                    .toLowerCase(Locale.ROOT).replace("!", "!!")
                    .replace("%", "!%").replace("_", "!_") + "%");
        }
    }

    private Totals countTotals(String where,
            MapSqlParameterSource parameters) {
        return jdbc.queryForObject(
                """
                        SELECT COUNT(*) AS movement_count,
                          COUNT(DISTINCT sm.product_id) AS product_count,
                          COUNT(DISTINCT sm.warehouse_id) AS warehouse_count,
                          COUNT(DISTINCT sm.movement_type) AS movement_type_count,
                        """
                        + ENTRY_QUANTITY + "," + EXIT_QUANTITY + FROM_MOVEMENTS
                        + where,
                parameters,
                (row, index) -> new Totals(
                        new Summary(row.getLong("movement_count"),
                                row.getLong("product_count"),
                                row.getLong("warehouse_count"),
                                row.getLong("entry_quantity"),
                                row.getLong("exit_quantity"),
                                row.getLong("entry_quantity")
                                        - row.getLong("exit_quantity")),
                        row.getLong("movement_type_count")));
    }

    private List<WarehouseOption> findWarehouses() {
        return jdbc.query(
                "SELECT id, name FROM warehouses ORDER BY name, id",
                (row, index) -> new WarehouseOption(uuid(row.getBytes("id")),
                        row.getString("name")));
    }

    private List<ProductOption> findProducts() {
        return jdbc.query(
                "SELECT id, name, code FROM products ORDER BY name, code, id",
                (row, index) -> new ProductOption(uuid(row.getBytes("id")),
                        row.getString("name"), row.getString("code")));
    }

    private List<StockMovementGroup> queryGroups(StockMovementFilter filter,
            String where, MapSqlParameterSource parameters) {
        GroupQuery query = groupQuery(filter.groupBy());
        var selectedGroups = jdbc.query(
                "SELECT " + query.columns()
                        + """
                                , COUNT(*) AS movement_count,
                                """
                        + ENTRY_QUANTITY + "," + EXIT_QUANTITY + FROM_MOVEMENTS
                        + where + " GROUP BY " + query.groupColumns()
                        + " ORDER BY " + query.order() + " LIMIT :limit OFFSET :offset",
                parameters,
                (row, index) -> new GroupTotals(
                        query.uuidIdentity()
                                ? uuid(row.getBytes("group_id"))
                                : row.getString("group_id"),
                        row.getString("group_name"),
                        row.getString("product_code"),
                        row.getString("category_name"),
                        row.getString("warehouse_name"),
                        row.getString("movement_type"),
                        row.getLong("movement_count"),
                        row.getLong("entry_quantity"),
                        row.getLong("exit_quantity")));
        if (selectedGroups.isEmpty()) {
            return List.of();
        }
        if (query.uuidIdentity()) {
            parameters.addValue("groupIds", selectedGroups.stream()
                    .map(group -> bytes(UUID.fromString(group.id()))).toList());
        } else {
            parameters.addValue("groupIds", selectedGroups.stream()
                    .map(GroupTotals::id).toList());
        }
        Map<String, List<StockMovementResponse>> groupedItems = queryMovements(
                where + " AND " + query.identity() + " IN (:groupIds) ",
                parameters, false)
                .stream()
                .collect(Collectors.groupingBy(
                        movement -> groupKey(filter.groupBy(), movement)));
        return selectedGroups.stream()
                .map(group -> new StockMovementGroup(group.id(), group.name(),
                        group.movementType() == null
                                ? null
                                : MovementType.valueOf(group.movementType()),
                        group.productCode(), group.categoryName(),
                        group.warehouseName(), group.movementCount(),
                        group.entryQuantity(), group.exitQuantity(),
                        group.entryQuantity() - group.exitQuantity(),
                        List.copyOf(groupedItems.getOrDefault(group.id(),
                                List.of()))))
                .toList();
    }

    private List<StockMovementResponse> queryMovements(String where,
            MapSqlParameterSource parameters, boolean paginate) {
        return jdbc.query(
                """
                        SELECT sm.id, sm.warehouse_id, w.name AS warehouse_name,
                          sm.product_id, p.code AS product_code,
                          p.name AS product_name, c.name AS category_name,
                          sm.movement_type, sm.quantity, sm.previous_stock,
                          sm.new_stock, sm.origin_movement_id,
                          sm.transfer_batch_id, sm.reference_type,
                          sm.reference_id, sm.reference_number, sm.notes,
                          sm.created_at
                        """
                        + FROM_MOVEMENTS + where
                        + " ORDER BY sm.created_at DESC, sm.id DESC"
                        + (paginate ? " LIMIT :limit OFFSET :offset" : ""),
                parameters,
                (row, index) -> new StockMovementResponse(
                        uuid(row.getBytes("id")),
                        uuid(row.getBytes("warehouse_id")),
                        row.getString("warehouse_name"),
                        uuid(row.getBytes("product_id")),
                        row.getString("product_code"),
                        row.getString("product_name"),
                        row.getString("category_name"),
                        MovementType.valueOf(row.getString("movement_type")),
                        row.getInt("quantity"), row.getInt("previous_stock"),
                        row.getInt("new_stock"),
                        uuid(row.getBytes("origin_movement_id")),
                        row.getString("transfer_batch_id"),
                        referenceType(row.getString("reference_type")),
                        uuid(row.getBytes("reference_id")),
                        row.getString("reference_number"),
                        row.getString("notes"),
                        row.getTimestamp("created_at") == null
                                ? null
                                : row.getTimestamp("created_at")
                                        .toLocalDateTime()));
    }

    private static String groupKey(StockMovementGroupBy groupBy,
            StockMovementResponse movement) {
        return switch (groupBy) {
            case PRODUCT -> movement.productId();
            case WAREHOUSE -> movement.warehouseId();
            case MOVEMENT_TYPE -> movement.movementType().name();
            case NONE -> movement.id();
        };
    }

    private static GroupQuery groupQuery(StockMovementGroupBy groupBy) {
        return switch (groupBy) {
            case PRODUCT -> new GroupQuery(
                    "p.id AS group_id, p.name AS group_name, "
                            + "p.code AS product_code, "
                            + "c.name AS category_name, "
                            + "NULL AS warehouse_name, "
                            + "NULL AS movement_type",
                    "p.id, p.name, p.code, c.name", "p.name, p.code, p.id",
                    "p.id", true);
            case WAREHOUSE -> new GroupQuery(
                    "w.id AS group_id, w.name AS group_name, "
                            + "NULL AS product_code, "
                            + "NULL AS category_name, "
                            + "w.name AS warehouse_name, "
                            + "NULL AS movement_type",
                    "w.id, w.name", "w.name, w.id", "w.id", true);
            case MOVEMENT_TYPE -> new GroupQuery(
                    "sm.movement_type AS group_id, "
                            + "sm.movement_type AS group_name, "
                            + "NULL AS product_code, "
                            + "NULL AS category_name, "
                            + "NULL AS warehouse_name, "
                            + "sm.movement_type AS movement_type",
                    "sm.movement_type", "sm.movement_type", "sm.movement_type",
                    false);
            case NONE -> throw new IllegalArgumentException(
                    "Cannot group movements by " + groupBy);
        };
    }

    private static String movementTypesIn(Predicate<MovementType> direction) {
        return Arrays.stream(MovementType.values()).filter(direction)
                .map(type -> "'" + type.name() + "'")
                .collect(Collectors.joining(", "));
    }

    private static ReferenceType referenceType(String value) {
        return value == null ? null : ReferenceType.valueOf(value);
    }

    private static <T> PageResponse<T> page(List<T> content, long count,
            StockMovementFilter filter) {
        int totalPages = (int) ((count + filter.size() - 1) / filter.size());
        return new PageResponse<>(content, count, totalPages, filter.size(),
                filter.page(), filter.page() == 0,
                (long) filter.page() + 1 >= totalPages, content.isEmpty());
    }

    private static byte[] bytes(UUID id) {
        return ByteBuffer.allocate(Long.BYTES * 2)
                .putLong(id.getMostSignificantBits())
                .putLong(id.getLeastSignificantBits()).array();
    }

    private static String uuid(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        var buffer = ByteBuffer.wrap(bytes);
        return new UUID(buffer.getLong(), buffer.getLong()).toString();
    }

    private record Totals(Summary summary, long movementTypeCount) {
    }

    private record GroupTotals(String id, String name, String productCode,
            String categoryName, String warehouseName, String movementType,
            long movementCount, long entryQuantity, long exitQuantity) {
    }

    private record GroupQuery(String columns, String groupColumns, String order,
            String identity, boolean uuidIdentity) {
    }
}
