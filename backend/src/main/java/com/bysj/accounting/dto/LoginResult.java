package com.bysj.accounting.dto;

import com.bysj.accounting.security.AccountRole;

public class LoginResult {
    public String token;
    public Long id;
    public String username;
    public String displayName;
    public String avatarUrl;
    public AccountRole role;

    public LoginResult(String token, Long id, String username, String displayName, String avatarUrl, AccountRole role) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.role = role;
    }
}
