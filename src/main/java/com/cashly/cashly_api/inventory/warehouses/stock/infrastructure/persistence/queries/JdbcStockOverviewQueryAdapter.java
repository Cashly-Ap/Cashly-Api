package com.cashly.cashly_api.inventory.warehouses.stock.infrastructure.persistence.queries;

import com.cashly.cashly_api.inventory.warehouses.stock.application.dto.response.StockOverviewItem;
import com.cashly.cashly_api.inventory.warehouses.stock.application.dto.response.StockOverviewGroup;
import com.cashly.cashly_api.inventory.warehouses.stock.application.dto.response.StockOverviewResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.dto.response.StockOverviewResponse.Option;
import com.cashly.cashly_api.inventory.warehouses.stock.application.dto.response.StockOverviewResponse.Summary;
import com.cashly.cashly_api.inventory.warehouses.stock.application.ports.out.StockOverviewQueryGateway;
import com.cashly.cashly_api.inventory.warehouses.stock.application.queries.model.StockOverviewFilter;
import com.cashly.cashly_api.shared.dto.PageResponse;
import com.cashly.cashly_api.inventory.warehouses.stock.application.queries.model.StockOverviewGroupBy;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.UUID;

@Repository
public class JdbcStockOverviewQueryAdapter
        implements
            StockOverviewQueryGateway {
    private static final String FROM_STOCK = """
            FROM warehouse_stock ws
            JOIN warehouses w ON w.id = ws.warehouse_id
            JOIN products p ON p.id = ws.product_id
            LEFT JOIN categories c ON c.id = p.category_id
            """;
    private final NamedParameterJdbcTemplate jdbc;

    public JdbcStockOverviewQueryAdapter(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public StockOverviewResponse search(StockOverviewFilter filter) {
        var parameters = new MapSqlParameterSource();
        var where = new StringBuilder(" WHERE 1 = 1 ");
        if (filter.warehouseId() != null) {
            where.append(" AND ws.warehouse_id = :warehouseId ");
            parameters.addValue("warehouseId", bytes(filter.warehouseId()));
        }
        if (filter.categoryId() != null) {
            where.append(" AND p.category_id = :categoryId ");
            parameters.addValue("categoryId", bytes(filter.categoryId()));
        }
        if (!filter.search().isEmpty()) {
            where.append("""
                    AND (LOWER(p.name) LIKE :search ESCAPE '!'
                      OR LOWER(p.code) LIKE :search ESCAPE '!'
                      OR LOWER(w.name) LIKE :search ESCAPE '!')
                    """);
            parameters.addValue("search",
                    "%" + filter.search().toLowerCase(Locale.ROOT)
                            .replace("!", "!!").replace("%", "!%")
                            .replace("_", "!_") + "%");
        }
        var totals = jdbc.queryForObject(
                """
                        SELECT COUNT(*) AS row_count, COUNT(DISTINCT p.id) AS product_count,
                          COUNT(DISTINCT w.id) AS warehouse_count,
                          COALESCE(SUM(ws.quantity), 0) AS total_quantity,
                          COALESCE(SUM(ws.reserved), 0) AS reserved_quantity,
                          COALESCE(SUM(ws.quantity - ws.reserved), 0) AS available_quantity
                        """
                        + FROM_STOCK + where,
                parameters,
                (row, index) -> new Totals(row.getLong("row_count"),
                        new Summary(row.getLong("product_count"),
                                row.getLong("warehouse_count"),
                                row.getLong("total_quantity"),
                                row.getLong("reserved_quantity"),
                                row.getLong("available_quantity"))));
        parameters.addValue("limit", filter.size()).addValue("offset",
                (long) filter.page() * filter.size());
        PageResponse<StockOverviewItem> stock = null;
        PageResponse<StockOverviewGroup> groups = null;
        if (filter.groupBy() == StockOverviewGroupBy.NONE) {
            var items = queryItems(where.toString(), parameters, true);
            stock = page(items, totals.rowCount(), filter);
        } else {
            groups = queryGroups(filter, where.toString(), parameters,
                    totals.summary());
        }
        var warehouses = jdbc.query(
                "SELECT id, name FROM warehouses ORDER BY name, id",
                (row, index) -> new Option(uuid(row.getBytes("id")),
                        row.getString("name")));
        var categories = jdbc.query(
                "SELECT id, name FROM categories ORDER BY name, id",
                (row, index) -> new Option(uuid(row.getBytes("id")),
                        row.getString("name")));
        return new StockOverviewResponse(stock, totals.summary(), warehouses,
                categories, filter.groupBy(), groups);
    }

    private PageResponse<StockOverviewGroup> queryGroups(
            StockOverviewFilter filter, String where,
            MapSqlParameterSource parameters, Summary summary) {
        boolean byProduct = filter.groupBy() == StockOverviewGroupBy.PRODUCT;
        String identity = byProduct ? "p.id" : "w.id";
        String columns = byProduct
                ? "p.id AS group_id, p.name AS group_name, p.code AS product_code, c.name AS category_name"
                : "w.id AS group_id, w.name AS group_name, NULL AS product_code, NULL AS category_name";
        String groupColumns = byProduct
                ? "p.id, p.name, p.code, c.name"
                : "w.id, w.name";
        String order = byProduct ? "p.name, p.code, p.id" : "w.name, w.id";
        var selectedGroups = jdbc.query(
                "SELECT " + columns
                        + """
                                , SUM(ws.quantity) AS quantity, SUM(ws.reserved) AS reserved,
                                  SUM(ws.quantity - ws.reserved) AS available,
                                  COUNT(DISTINCT p.id) AS product_count, COUNT(DISTINCT w.id) AS warehouse_count
                                """
                        + FROM_STOCK + where + " GROUP BY " + groupColumns
                        + " ORDER BY " + order + " LIMIT :limit OFFSET :offset",
                parameters,
                (row, index) -> new GroupTotals(uuid(row.getBytes("group_id")),
                        row.getString("group_name"),
                        row.getString("product_code"),
                        row.getString("category_name"), row.getLong("quantity"),
                        row.getLong("reserved"), row.getLong("available"),
                        row.getLong("product_count"),
                        row.getLong("warehouse_count")));
        Map<String, List<StockOverviewItem>> groupedItems;
        if (selectedGroups.isEmpty()) {
            groupedItems = Map.of();
        } else {
            parameters.addValue("groupIds", selectedGroups.stream()
                    .map(group -> bytes(UUID.fromString(group.id()))).toList());
            groupedItems = queryItems(
                    where + " AND " + identity + " IN (:groupIds) ", parameters,
                    false)
                    .stream()
                    .collect(Collectors.groupingBy(item -> byProduct
                            ? item.productId()
                            : item.warehouseId()));
        }
        var groups = selectedGroups.stream()
                .map(group -> new StockOverviewGroup(group.id(), group.name(),
                        group.productCode(), group.categoryName(), group
                                .quantity(),
                        group.reserved(), group.available(),
                        group.productCount(), group.warehouseCount(),
                        List.copyOf(groupedItems.getOrDefault(group.id(),
                                List.of()))))
                .toList();
        return page(groups,
                byProduct ? summary.productCount() : summary.warehouseCount(),
                filter);
    }

    private List<StockOverviewItem> queryItems(String where,
            MapSqlParameterSource parameters, boolean paginate) {
        return jdbc.query(
                """
                        SELECT ws.id, w.id AS warehouse_id, w.name AS warehouse_name,
                          p.id AS product_id, p.code AS product_code, p.name AS product_name,
                          c.id AS category_id, c.name AS category_name, p.composite,
                          ws.quantity, ws.reserved, (ws.quantity - ws.reserved) AS available
                        """
                        + FROM_STOCK + where
                        + " ORDER BY w.name, p.name, p.code, ws.id"
                        + (paginate ? " LIMIT :limit OFFSET :offset" : ""),
                parameters,
                (row, index) -> new StockOverviewItem(uuid(row.getBytes("id")),
                        uuid(row.getBytes("warehouse_id")),
                        row.getString("warehouse_name"),
                        uuid(row.getBytes("product_id")),
                        row.getString("product_code"),
                        row.getString("product_name"),
                        uuid(row.getBytes("category_id")),
                        row.getString("category_name"),
                        row.getBoolean("composite"), row.getInt("quantity"),
                        row.getInt("reserved"), row.getInt("available")));
    }

    private static <T> PageResponse<T> page(List<T> content, long count,
            StockOverviewFilter filter) {
        int totalPages = (int) ((count + filter.size() - 1) / filter.size());
        return new PageResponse<>(content, count, totalPages, filter.size(),
                filter.page(), filter.page() == 0,
                (long) filter.page() + 1 >= totalPages, content.isEmpty());
    }

    private record GroupTotals(String id, String name, String productCode,
            String categoryName, long quantity, long reserved, long available,
            long productCount, long warehouseCount) {
    }

    private static byte[] bytes(UUID id) {
        return ByteBuffer.allocate(Long.BYTES * 2)
                .putLong(id.getMostSignificantBits())
                .putLong(id.getLeastSignificantBits()).array();
    }

    private static String uuid(byte[] bytes) {
        if (bytes == null)
            return null;
        var buffer = ByteBuffer.wrap(bytes);
        return new UUID(buffer.getLong(), buffer.getLong()).toString();
    }

    private record Totals(long rowCount, Summary summary) {
    }
}
