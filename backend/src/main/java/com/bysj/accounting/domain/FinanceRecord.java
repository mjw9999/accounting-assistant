package com.bysj.accounting.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "caiwu")
public class FinanceRecord extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    public RecordType type;

    public Long categoryId;

    @Column(nullable = false, length = 80)
    public String categoryName;

    @Column(nullable = false, precision = 12, scale = 2)
    public BigDecimal amount;

    @Column(length = 300)
    public String remark;

    @Column(nullable = false)
    public Long userId;

    @Column(nullable = false, length = 64)
    public String createdBy;

    @Column(length = 32)
    public String recordSource; // MANUAL, AUTO_REDEMPTION

    @Column(nullable = false)
    public LocalDate recordDate;
}
