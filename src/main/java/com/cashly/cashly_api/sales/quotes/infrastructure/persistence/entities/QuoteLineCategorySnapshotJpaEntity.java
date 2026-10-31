package com.cashly.cashly_api.sales.quotes.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "quote_line_category_snapshots")
public class QuoteLineCategorySnapshotJpaEntity {
    @Id
    @Column(name = "quote_line_id", columnDefinition = "BINARY(16)")
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "quote_line_id", columnDefinition = "BINARY(16)")
    private QuoteLineJpaEntity line;

    @Column(name = "category_id", columnDefinition = "BINARY(16)", nullable = false)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID categoryId;

    protected QuoteLineCategorySnapshotJpaEntity() {
    }

    public QuoteLineCategorySnapshotJpaEntity(QuoteLineJpaEntity line,
            UUID categoryId) {
        this.line = line;
        this.categoryId = categoryId;
    }

    public UUID getId() {
        return id;
    }

    /** Restore the persisted shared key before merging an existing snapshot. New snapshots keep a null id. */
    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCategoryId() {
        return categoryId;
    }
}
