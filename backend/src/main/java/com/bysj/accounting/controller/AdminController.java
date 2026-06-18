package com.bysj.accounting.controller;

import com.bysj.accounting.common.ApiResponse;
import com.bysj.accounting.common.BusinessException;
import com.bysj.accounting.domain.Admin;
import com.bysj.accounting.dto.Requests;
import com.bysj.accounting.repository.AdminRepository;
import com.bysj.accounting.service.PasswordSupport;
import jakarta.validation.Valid;
import com.bysj.accounting.common.PageResult;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admins")
@PreAuthorize("hasRole('ADMIN')")
@SuppressWarnings("null")
public class AdminController {
    private final AdminRepository adminRepository;
    private final PasswordSupport passwordSupport;

    public AdminController(AdminRepository adminRepository, PasswordSupport passwordSupport) {
        this.adminRepository = adminRepository;
        this.passwordSupport = passwordSupport;
    }

    @GetMapping
    public ApiResponse<PageResult<Admin>> list(@RequestParam(required = false) String keyword,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        Specification<Admin> spec = (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) {
                return cb.conjunction();
            }
            String like = "%" + keyword.trim() + "%";
            return cb.or(cb.like(root.get("username"), like), cb.like(root.get("realName"), like));
        };
        return ApiResponse.ok(PageResult.of(adminRepository.findAll(spec, PageRequest.of(page, size))));
    }

    @PostMapping
    public ApiResponse<Admin> create(@Valid @RequestBody Requests.AdminRequest request) {
        if (adminRepository.existsByUsername(request.username)) {
            throw new BusinessException("管理员账号已存在");
        }
        Admin admin = new Admin();
        apply(admin, request);
        admin.password = passwordSupport.encode(StringUtils.hasText(request.password) ? request.password : "admin123");
        return ApiResponse.ok("管理员创建成功", adminRepository.save(admin));
    }

    @PutMapping("/{id}")
    public ApiResponse<Admin> update(@PathVariable Long id, @Valid @RequestBody Requests.AdminRequest request) {
        Admin admin = adminRepository.findById(id).orElseThrow(() -> new BusinessException("管理员不存在"));
        if (!admin.username.equals(request.username) && adminRepository.existsByUsername(request.username)) {
            throw new BusinessException("管理员账号已存在");
        }
        apply(admin, request);
        if (StringUtils.hasText(request.password)) {
            admin.password = passwordSupport.encode(request.password);
        }
        return ApiResponse.ok("管理员更新成功", adminRepository.save(admin));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (adminRepository.count() <= 1) {
            throw new BusinessException("至少保留一个管理员账号");
        }
        adminRepository.deleteById(id);
        return ApiResponse.ok("管理员删除成功", null);
    }

    private void apply(Admin admin, Requests.AdminRequest request) {
        admin.username = request.username;
        admin.realName = request.realName;
        admin.phone = request.phone;
        admin.status = request.status;
    }
}
