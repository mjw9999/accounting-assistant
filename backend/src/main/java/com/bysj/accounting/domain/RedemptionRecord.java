package com.bysj.accounting.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "shuhui_jilu")
public class RedemptionRecord extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public Long investmentId;

    @Column(nullable = false)
    public Long productId;

    @Column(nullable = false, length = 100)
    public String productName;

    @Column(nullable = false, precision = 12, scale = 2)
    public BigDecimal amount;

    @Column(nullable = false)
    public Integer actualDays;

    @Column(nullable = false, precision = 12, scale = 2)
    public BigDecimal income;

    @Column(nullable = false)
    public Long redeemerId;

    @Column(nullable = false, length = 64)
    public String redeemerName;

    @Column(nullable = false)
    public LocalDate redeemDate;

    @Column(length = 300)
    public String remark;
}
