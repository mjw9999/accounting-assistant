package com.bysj.accounting.controller;

import com.bysj.accounting.common.ApiResponse;
import com.bysj.accounting.common.BusinessException;
import com.bysj.accounting.domain.FinancialProduct;
import com.bysj.accounting.dto.Requests;
import com.bysj.accounting.repository.FinancialProductRepository;
import com.bysj.accounting.repository.InvestmentRecordRepository;
import jakarta.validation.Valid;
import com.bysj.accounting.common.PageResult;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@SuppressWarnings("null")
public class ProductController {
    private final FinancialProductRepository productRepository;
    private final InvestmentRecordRepository investmentRecordRepository;

    public ProductController(FinancialProductRepository productRepository, InvestmentRecordRepository investmentRecordRepository) {
        this.productRepository = productRepository;
        this.investmentRecordRepository = investmentRecordRepository;
    }

    @GetMapping
    public ApiResponse<PageResult<FinancialProduct>> list(@RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) String type,
                                                    @RequestParam(required = false) String status,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        Specification<FinancialProduct> spec = (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (StringUtils.hasText(type)) {
                predicate = cb.and(predicate, cb.equal(root.get("type"), type));
            }
            if (StringUtils.hasText(status)) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                predicate = cb.and(predicate, cb.or(cb.like(root.get("name"), like), cb.like(root.get("publisher"), like)));
            }
            return predicate;
        };
        return ApiResponse.ok(PageResult.of(productRepository.findAll(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")))));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<FinancialProduct> create(@Valid @RequestBody Requests.ProductRequest request) {
        FinancialProduct product = new FinancialProduct();
        apply(product, request);
        return ApiResponse.ok("理财产品创建成功", productRepository.save(product));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<FinancialProduct> update(@PathVariable Long id, @Valid @RequestBody Requests.ProductRequest request) {
        FinancialProduct product = productRepository.findById(id).orElseThrow(() -> new BusinessException("理财产品不存在"));
        apply(product, request);
        return ApiResponse.ok("理财产品更新成功", productRepository.save(product));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (investmentRecordRepository.existsByProductId(id)) {
            throw new BusinessException("该产品已有投资记录，不能删除，可将状态改为停");
        }
        productRepository.deleteById(id);
        return ApiResponse.ok("理财产品删除成功", null);
    }

    private void apply(FinancialProduct product, Requests.ProductRequest request) {
        if (request.maxAmount.compareTo(request.minAmount) < 0) {
            throw new BusinessException("最大投资金额不能小于最小投资金");
        }
        product.name = request.name;
        product.type = request.type;
        product.annualRate = request.annualRate;
        product.publisher = request.publisher;
        product.minAmount = request.minAmount;
        product.maxAmount = request.maxAmount;
        product.termDays = request.termDays;
        product.riskLevel = request.riskLevel;
        product.status = request.status;
        product.remark = request.remark;
    }
}
