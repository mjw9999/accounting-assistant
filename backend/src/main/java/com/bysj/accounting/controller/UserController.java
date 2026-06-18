package com.bysj.accounting.controller;

import com.bysj.accounting.common.ApiResponse;
import com.bysj.accounting.common.BusinessException;
import com.bysj.accounting.domain.User;
import com.bysj.accounting.dto.Requests;
import com.bysj.accounting.repository.FinanceRecordRepository;
import com.bysj.accounting.repository.InvestmentRecordRepository;
import com.bysj.accounting.repository.RedemptionRecordRepository;
import com.bysj.accounting.repository.ShareRepository;
import com.bysj.accounting.repository.UserRepository;
import com.bysj.accounting.security.AccountRole;
import com.bysj.accounting.security.CurrentAccount;
import com.bysj.accounting.service.PasswordSupport;
import jakarta.validation.Valid;
import com.bysj.accounting.common.PageResult;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@SuppressWarnings("null")
public class UserController {
    private final UserRepository userRepository;
    private final PasswordSupport passwordSupport;
    private final InvestmentRecordRepository investmentRepository;
    private final RedemptionRecordRepository redemptionRepository;
    private final FinanceRecordRepository financeRecordRepository;
    private final ShareRepository shareRepository;

    public UserController(UserRepository userRepository,
                          PasswordSupport passwordSupport,
                          InvestmentRecordRepository investmentRepository,
                          RedemptionRecordRepository redemptionRepository,
                          FinanceRecordRepository financeRecordRepository,
                          ShareRepository shareRepository) {
        this.userRepository = userRepository;
        this.passwordSupport = passwordSupport;
        this.investmentRepository = investmentRepository;
        this.redemptionRepository = redemptionRepository;
        this.financeRecordRepository = financeRecordRepository;
        this.shareRepository = shareRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResult<User>> list(@RequestParam(required = false) String keyword,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        Specification<User> spec = (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) {
                return cb.conjunction();
            }
            String like = "%" + keyword.trim() + "%";
            return cb.or(
                    cb.like(root.get("username"), like),
                    cb.like(root.get("realName"), like),
                    cb.like(root.get("phone"), like)
            );
        };
        return ApiResponse.ok(PageResult.of(userRepository.findAll(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")))));
    }

    @GetMapping("/{id}")
    public ApiResponse<User> detail(@PathVariable Long id, @AuthenticationPrincipal CurrentAccount current) {
        if (current.role() == AccountRole.USER && !current.id().equals(id)) {
            throw new BusinessException("只能查看自己的资料");
        }
        return ApiResponse.ok(userRepository.findById(id).orElseThrow(() -> new BusinessException("用户不存在")));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<User> create(@Valid @RequestBody Requests.UserRequest request) {
        if (userRepository.existsByUsername(request.username)) {
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        apply(user, request);
        user.password = passwordSupport.encode(StringUtils.hasText(request.password) ? request.password : "123456");
        return ApiResponse.ok("用户创建成功", userRepository.save(user));
    }

    @PutMapping("/{id}")
    @Transactional
    public ApiResponse<User> update(@PathVariable Long id,
                                    @Valid @RequestBody Requests.UserRequest request,
                                    @AuthenticationPrincipal CurrentAccount current) {
        if (current.role() == AccountRole.USER && !current.id().equals(id)) {
            throw new BusinessException("只能修改自己的资料");
        }
        User user = userRepository.findById(id).orElseThrow(() -> new BusinessException("用户不存在"));
        if (!user.username.equals(request.username) && userRepository.existsByUsername(request.username)) {
            throw new BusinessException("用户名已存在");
        }
        apply(user, request);
        String newRealName = user.realName;

        if (StringUtils.hasText(request.password) && current.role() == AccountRole.ADMIN) {
            user.password = passwordSupport.encode(request.password);
        }
        User savedUser = userRepository.save(user);

        // 触发全量数据清洗与同步（不仅限姓名变化，确保来源打标等逻辑执行）
        syncUserRealName(id, newRealName);

        return ApiResponse.ok("用户更新成功", savedUser);
    }

    private void syncUserRealName(Long userId, String newName) {
        // 1. 全站数据大扫除：识别来源、清理乱码、修复关联
        financeRecordRepository.findAll().forEach(fr -> {
            boolean changed = false;
            // A. 识别来源并清理备注
            if (StringUtils.hasText(fr.remark)) {
                if (fr.remark.contains("#REDEMPTION_INCOME_ID=") || fr.remark.contains("赎回收益自动入账")) {
                    fr.remark = fr.remark.replaceAll("#REDEMPTION_INCOME_ID=\\d+#", "").trim();
                    fr.recordSource = "AUTO_REDEMPTION";
                    changed = true;
                }
            }
            // B. 确保所有记录都有来源
            if (fr.recordSource == null) {
                fr.recordSource = "MANUAL";
                changed = true;
            }
            // C. 修复姓名同步
            if (fr.userId != null && fr.userId.equals(userId) && StringUtils.hasText(newName)) {
                fr.createdBy = newName;
                changed = true;
            }
            if (changed) financeRecordRepository.save(fr);
        });

        // 2. 同步投资记录
        investmentRepository.findByInvestorId(userId).forEach(inv -> {
            inv.investorName = newName;
            investmentRepository.save(inv);
        });
        // 3. 同步赎回记录
        redemptionRepository.findByRedeemerId(userId).forEach(red -> {
            red.redeemerName = newName;
            redemptionRepository.save(red);
        });
        // 4. 同步属于该用户的财务记录姓名
        financeRecordRepository.findByUserId(userId).forEach(fr -> {
            fr.createdBy = newName;
            financeRecordRepository.save(fr);
        });
        // 5. 同步分享记录中的理财人姓名
        if (StringUtils.hasText(newName)) {
            shareRepository.findByUserId(userId).forEach(share -> {
                share.investorName = newName;
                share.userName = newName;
                shareRepository.save(share);
            });
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ApiResponse.ok("用户删除成功", null);
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<User> profile(@AuthenticationPrincipal CurrentAccount current) {
        return ApiResponse.ok(userRepository.findById(current.id()).orElseThrow(() -> new BusinessException("用户不存在")));
    }

    private void apply(User user, Requests.UserRequest request) {
        user.username = request.username;
        user.realName = request.realName;
        user.phone = request.phone;
        user.address = request.address;
        user.avatarUrl = request.avatarUrl;
        user.status = request.status;
    }
}
