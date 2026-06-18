package com.bysj.accounting.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "fenxiang")
public class Share extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public Long investmentId;

    @Column(nullable = false)
    public Long productId;

    @Column(nullable = false, length = 100)
    public String productName;

    @Column(nullable = false, length = 64)
    public String investorName;

    @Column(nullable = false, precision = 12, scale = 2)
    public BigDecimal amount;

    @Column(nullable = false, precision = 12, scale = 2)
    public BigDecimal income;

    @Column(nullable = false, length = 120)
    public String title;

    @Column(nullable = false, length = 1000)
    public String content;

    @Column(nullable = false)
    public Long userId;

    @Column(nullable = false, length = 64)
    public String userName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    public CommonStatus status = CommonStatus.ENABLED;
}
