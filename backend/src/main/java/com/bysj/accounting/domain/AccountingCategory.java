package com.bysj.accounting.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "jizhang_fenlei")
public class AccountingCategory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 80)
    public String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    public RecordType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    public CommonStatus status = CommonStatus.ENABLED;

    @Column(length = 200)
    public String remark;
}
