package com.bysj.accounting.controller;

import com.bysj.accounting.common.ApiResponse;
import com.bysj.accounting.common.BusinessException;
import com.bysj.accounting.domain.InvestmentRecord;
import com.bysj.accounting.domain.Share;
import com.bysj.accounting.dto.Requests;
import com.bysj.accounting.repository.InvestmentRecordRepository;
import com.bysj.accounting.repository.ShareRepository;
import com.bysj.accounting.security.AccountRole;
import com.bysj.accounting.security.CurrentAccount;
import com.bysj.accounting.service.SecuritySupport;
import jakarta.validation.Valid;
import com.bysj.accounting.common.PageResult;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/shares")
@SuppressWarnings("null")
public class ShareController {
    private final ShareRepository shareRepository;
    private final InvestmentRecordRepository investmentRepository;
    private final SecuritySupport securitySupport;

    public ShareController(ShareRepository shareRepository,
                           InvestmentRecordRepository investmentRepository,
                           SecuritySupport securitySupport) {
        this.shareRepository = shareRepository;
        this.investmentRepository = investmentRepository;
        this.securitySupport = securitySupport;
    }

    @GetMapping
    public ApiResponse<PageResult<Share>> list(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) Long userId,
                                         @RequestParam(required = false) String status,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         @AuthenticationPrincipal CurrentAccount current) {
        Specification<Share> spec = (root, query, cb) -> {
            var predicate = cb.conjunction();
            // 管理端查询：如果不是查询“展示中”的内容，且是普通用户，则只看自己的
            if (!"ENABLED".equals(status) && current.role() == AccountRole.USER) {
                predicate = cb.and(predicate, cb.equal(root.get("userId"), current.id()));
            } else if (userId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("userId"), userId));
            }
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(root.get("productName"), like),
                        cb.like(root.get("title"), like),
                        cb.like(root.get("content"), like),
                        cb.like(root.get("investorName"), like)
                ));
            }
            if (StringUtils.hasText(status)) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            return predicate;
        };
        return ApiResponse.ok(PageResult.of(shareRepository.findAll(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")))));
    }

    @GetMapping("/{id}")
    public ApiResponse<Share> getOne(@PathVariable Long id) {
        return ApiResponse.ok(shareRepository.findById(id).orElseThrow(() -> new BusinessException("分享不存在")));
    }

    @PostMapping
    public ApiResponse<Share> create(@Valid @RequestBody Requests.ShareRequest request,
                                     @AuthenticationPrincipal CurrentAccount current) {
        InvestmentRecord investment = investmentRepository.findById(request.investmentId)
                .orElseThrow(() -> new BusinessException("投资记录不存在"));
        securitySupport.requireOwnerOrAdmin(current, investment.investorId);

        Share share = new Share();
        share.investmentId = investment.id;
        share.productId = investment.productId;
        share.productName = investment.productName;
        share.investorName = investment.investorName;
        share.amount = investment.amount;
        share.income = investment.actualIncome == null ? investment.expectedIncome : investment.actualIncome;
        share.title = request.title;
        share.content = request.content;
        share.userId = investment.investorId;
        share.userName = investment.investorName;
        share.status = request.status;
        return ApiResponse.ok("分享创建成功", shareRepository.save(share));
    }

    @PutMapping("/{id}")
    public ApiResponse<Share> update(@PathVariable Long id,
                                     @Valid @RequestBody Requests.ShareRequest request,
                                     @AuthenticationPrincipal CurrentAccount current) {
        Share share = shareRepository.findById(id).orElseThrow(() -> new BusinessException("分享不存在"));
        securitySupport.requireOwnerOrAdmin(current, share.userId);
        share.title = request.title;
        share.content = request.content;
        share.status = request.status;
        if (request.investmentId != null && !request.investmentId.equals(share.investmentId)) {
            InvestmentRecord investment = investmentRepository.findById(request.investmentId)
                    .orElseThrow(() -> new BusinessException("投资记录不存在"));
            securitySupport.requireOwnerOrAdmin(current, investment.investorId);
            share.investmentId = investment.id;
            share.productId = investment.productId;
            share.productName = investment.productName;
            share.investorName = investment.investorName;
            share.amount = investment.amount;
            share.income = investment.actualIncome == null ? investment.expectedIncome : investment.actualIncome;
            share.userId = investment.investorId;
            share.userName = investment.investorName;
        }
        if (share.income == null) {
            share.income = BigDecimal.ZERO;
        }
        return ApiResponse.ok("分享更新成功", shareRepository.save(share));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal CurrentAccount current) {
        Share share = shareRepository.findById(id).orElseThrow(() -> new BusinessException("分享不存在"));
        securitySupport.requireOwnerOrAdmin(current, share.userId);
        shareRepository.delete(share);
        return ApiResponse.ok("分享删除成功", null);
    }
}
