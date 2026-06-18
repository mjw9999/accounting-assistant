package com.bysj.accounting.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "touzi_jilu")
public class InvestmentRecord extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public Long productId;

    @Column(name = "product_code", length = 64)
    public String productCode;

    @Column(nullable = false, length = 100)
    public String productName;

    @Column(nullable = false, length = 80)
    public String productType;

    @Column(nullable = false, precision = 8, scale = 4)
    public BigDecimal annualRate;

    @Column(nullable = false, precision = 12, scale = 2)
    public BigDecimal amount;

    @Column(nullable = false)
    public Long investorId;

    @Column(nullable = false, length = 64)
    public String investorName;

    @Column(nullable = false)
    public LocalDate startDate;

    @Column(nullable = false)
    public LocalDate expectedRedeemDate;

    public Integer actualDays = 0;

    @Column(nullable = false, precision = 12, scale = 2)
    public BigDecimal expectedIncome = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    public BigDecimal actualIncome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    public InvestmentStatus status = InvestmentStatus.HOLDING;
}
