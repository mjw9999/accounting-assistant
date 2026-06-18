package com.bysj.accounting.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "admin")
public class Admin extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true, length = 64)
    public String username;

    @Column(nullable = false, length = 120)
    public String password;

    @Column(length = 64)
    public String realName;

    @Column(length = 32)
    public String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    public CommonStatus status = CommonStatus.ENABLED;
}
