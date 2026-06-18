package com.bysj.accounting.controller;

import com.bysj.accounting.common.ApiResponse;
import com.bysj.accounting.common.BusinessException;
import com.bysj.accounting.domain.Admin;
import com.bysj.accounting.domain.CommonStatus;
import com.bysj.accounting.domain.User;
import com.bysj.accounting.dto.LoginResult;
import com.bysj.accounting.dto.Requests;
import com.bysj.accounting.repository.AdminRepository;
import com.bysj.accounting.repository.UserRepository;
import com.bysj.accounting.security.AccountRole;
import com.bysj.accounting.security.CurrentAccount;
import com.bysj.accounting.security.TokenService;
import com.bysj.accounting.service.PasswordSupport;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@SuppressWarnings("null")
public class AuthController {
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final PasswordSupport passwordSupport;
    private final TokenService tokenService;

    public AuthController(AdminRepository adminRepository,
                          UserRepository userRepository,
                          PasswordSupport passwordSupport,
                          TokenService tokenService) {
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.passwordSupport = passwordSupport;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResult> login(@Valid @RequestBody Requests.LoginRequest request) {
        if (request.role == AccountRole.ADMIN) {
            Admin admin = adminRepository.findByUsername(request.username)
                    .orElseThrow(() -> new BusinessException("账号或密码错"));
            if (admin.status != CommonStatus.ENABLED) {
                throw new BusinessException("该管理员账号已被停用，请联系上级管理");
            }
            if (!passwordSupport.matches(request.password, admin.password)) {
                throw new BusinessException("账号或密码错");
            }
            CurrentAccount account = new CurrentAccount(admin.id, admin.username, AccountRole.ADMIN, admin.realName, null);
            return ApiResponse.ok("登录成功", new LoginResult(
                    tokenService.createToken(account),
                    admin.id,
                    admin.username,
                    admin.realName,
                    null,
                    AccountRole.ADMIN
            ));
        }

        User user = userRepository.findByUsername(request.username)
                .orElseThrow(() -> new BusinessException("账号或密码错"));
        if (user.status != CommonStatus.ENABLED) {
            throw new BusinessException("该账户已被停用，请联系管理员");
        }
        if (!passwordSupport.matches(request.password, user.password)) {
            throw new BusinessException("账号或密码错");
        }
        CurrentAccount account = new CurrentAccount(user.id, user.username, AccountRole.USER, user.realName, user.avatarUrl);
        return ApiResponse.ok("登录成功", new LoginResult(
                tokenService.createToken(account),
                user.id,
                user.username,
                user.realName,
                user.avatarUrl,
                AccountRole.USER
        ));
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody Requests.RegisterRequest request) {
        if (userRepository.existsByUsername(request.username)) {
            throw new BusinessException("账号已存在，请换一个账");
        }
        User user = new User();
        user.username = request.username;
        user.password = passwordSupport.encode(request.password);
        user.realName = request.realName;
        user.status = CommonStatus.ENABLED;
        userRepository.save(user);
        return ApiResponse.ok("注册成功，请登录", null);
    }

    @GetMapping("/me")
    public ApiResponse<CurrentAccount> me(@AuthenticationPrincipal CurrentAccount current) {
        return ApiResponse.ok(current);
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody Requests.ResetPasswordRequest request) {
        if (request.role != AccountRole.USER) {
            throw new BusinessException("管理员账号不支持自助找回密码，请登录后在个人资料中修改，或由上级管理员在「管理员管理」中重置");
        }

        User user = userRepository.findByUsername(request.username)
                .orElseThrow(() -> new BusinessException("身份验证失败，请检查账号、姓名与手机"));
        ensureAccountEnabled(user.status);
        verifyIdentity(user.realName, user.phone, request.realName, request.phone);
        user.password = passwordSupport.encode(request.newPassword);
        userRepository.save(user);
        return ApiResponse.ok("密码重置成功，请使用新密码登", null);
    }

    private void ensureAccountEnabled(CommonStatus status) {
        if (status != CommonStatus.ENABLED) {
            throw new BusinessException("账户已停用，请联系管理员");
        }
    }

    private void verifyIdentity(String storedName, String storedPhone, String inputName, String inputPhone) {
        if (!StringUtils.hasText(storedName) || !storedName.trim().equals(inputName.trim())) {
            throw new BusinessException("身份验证失败，请检查账号、姓名与手机");
        }
        if (StringUtils.hasText(storedPhone)) {
            if (!StringUtils.hasText(inputPhone) || !storedPhone.trim().equals(inputPhone.trim())) {
                throw new BusinessException("身份验证失败，请检查账号、姓名与手机");
            }
        }
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@AuthenticationPrincipal CurrentAccount current,
                                            @Valid @RequestBody Requests.PasswordChangeRequest request) {
        if (current.role() == AccountRole.ADMIN) {
            Admin admin = adminRepository.findById(current.id()).orElseThrow(() -> new BusinessException("管理员不存在"));
            if (!passwordSupport.matches(request.oldPassword, admin.password)) {
                throw new BusinessException("原密码不正确");
            }
            admin.password = passwordSupport.encode(request.newPassword);
            adminRepository.save(admin);
            return ApiResponse.ok("修改密码成功", null);
        }

        User user = userRepository.findById(current.id()).orElseThrow(() -> new BusinessException("用户不存在"));
        if (!passwordSupport.matches(request.oldPassword, user.password)) {
            throw new BusinessException("原密码不正确");
        }
        user.password = passwordSupport.encode(request.newPassword);
        userRepository.save(user);
        return ApiResponse.ok("修改密码成功", null);
    }
}
