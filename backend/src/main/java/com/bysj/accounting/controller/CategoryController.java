package com.bysj.accounting.controller;

import com.bysj.accounting.common.ApiResponse;
import com.bysj.accounting.common.BusinessException;
import com.bysj.accounting.domain.AccountingCategory;
import com.bysj.accounting.domain.RecordType;
import com.bysj.accounting.dto.Requests;
import com.bysj.accounting.repository.AccountingCategoryRepository;
import com.bysj.accounting.repository.FinanceRecordRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@SuppressWarnings("null")
public class CategoryController {
    private final AccountingCategoryRepository categoryRepository;
    private final FinanceRecordRepository financeRecordRepository;

    public CategoryController(AccountingCategoryRepository categoryRepository, FinanceRecordRepository financeRecordRepository) {
        this.categoryRepository = categoryRepository;
        this.financeRecordRepository = financeRecordRepository;
    }

    @GetMapping
    public ApiResponse<List<AccountingCategory>> list(@RequestParam(required = false) RecordType type,
                                                      @RequestParam(required = false) String status,
                                                      @RequestParam(required = false) String keyword) {
        Specification<AccountingCategory> spec = (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (type != null) {
                predicate = cb.and(predicate, cb.equal(root.get("type"), type));
            }
            if (StringUtils.hasText(status)) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            if (StringUtils.hasText(keyword)) {
                predicate = cb.and(predicate, cb.like(root.get("name"), "%" + keyword.trim() + "%"));
            }
            return predicate;
        };
        return ApiResponse.ok(categoryRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "id")));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AccountingCategory> create(@Valid @RequestBody Requests.CategoryRequest request) {
        if (categoryRepository.existsByNameAndType(request.name, request.type)) {
            throw new BusinessException("同类型下分类名称已存");
        }
        AccountingCategory category = new AccountingCategory();
        apply(category, request);
        return ApiResponse.ok("分类创建成功", categoryRepository.save(category));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AccountingCategory> update(@PathVariable Long id, @Valid @RequestBody Requests.CategoryRequest request) {
        AccountingCategory category = categoryRepository.findById(id).orElseThrow(() -> new BusinessException("分类不存在"));
        apply(category, request);
        return ApiResponse.ok("分类更新成功", categoryRepository.save(category));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (financeRecordRepository.existsByCategoryId(id)) {
            throw new BusinessException("该分类已有记账记录，不能删除，可将状态改为停");
        }
        categoryRepository.deleteById(id);
        return ApiResponse.ok("分类删除成功", null);
    }

    private void apply(AccountingCategory category, Requests.CategoryRequest request) {
        category.name = request.name;
        category.type = request.type;
        category.status = request.status;
        category.remark = request.remark;
    }
}
