package com.bysj.accounting.security;

public record CurrentAccount(
        Long id,
        String username,
        AccountRole role,
        String displayName,
        String avatarUrl
) {
}
