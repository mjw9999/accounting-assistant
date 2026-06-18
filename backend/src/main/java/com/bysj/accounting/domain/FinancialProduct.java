package com.bysj.accounting.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "licaichanpin")
public class FinancialProduct extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "product_code", length = 64)
    public String productCode;

    @Column(nullable = false, length = 100)
    public String name;

    @Column(nullable = false, length = 80)
    public String type;

    @Column(nullable = false, precision = 8, scale = 4)
    public BigDecimal annualRate;

    @Column(length = 64)
    public String publisher;

    @Column(nullable = false, precision = 12, scale = 2)
    public BigDecimal minAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    public BigDecimal maxAmount = BigDecimal.valueOf(100000);

    public Integer termDays = 30;

    @Column(length = 24)
    public String riskLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    public CommonStatus status = CommonStatus.ENABLED;

    @Column(length = 300)
    public String remark;
}
