package com.bysj.accounting.controller;

import com.bysj.accounting.common.ApiResponse;
import com.bysj.accounting.common.BusinessException;
import com.bysj.accounting.domain.*;
import com.bysj.accounting.dto.Requests;
import com.bysj.accounting.repository.FinanceRecordRepository;
import com.bysj.accounting.repository.FinancialProductRepository;
import com.bysj.accounting.repository.InvestmentRecordRepository;
import com.bysj.accounting.repository.RedemptionRecordRepository;
import com.bysj.accounting.repository.UserRepository;
import com.bysj.accounting.repository.AccountingCategoryRepository;
import com.bysj.accounting.security.AccountRole;
import com.bysj.accounting.security.CurrentAccount;
import com.bysj.accounting.service.SecuritySupport;
import jakarta.validation.Valid;
import com.bysj.accounting.common.PageResult;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/investments")
@SuppressWarnings("null")
public class InvestmentController {
    private final InvestmentRecordRepository investmentRepository;
    private final RedemptionRecordRepository redemptionRepository;
    private final FinancialProductRepository productRepository;
    private final FinanceRecordRepository financeRecordRepository;
    private final UserRepository userRepository;
    private final SecuritySupport securitySupport;
    private final AccountingCategoryRepository categoryRepository;

    public InvestmentController(InvestmentRecordRepository investmentRepository,
                                RedemptionRecordRepository redemptionRepository,
                                FinancialProductRepository productRepository,
                                FinanceRecordRepository financeRecordRepository,
                                UserRepository userRepository,
                                SecuritySupport securitySupport,
                                AccountingCategoryRepository categoryRepository) {
        this.investmentRepository = investmentRepository;
        this.redemptionRepository = redemptionRepository;
        this.productRepository = productRepository;
        this.financeRecordRepository = financeRecordRepository;
        this.userRepository = userRepository;
        this.securitySupport = securitySupport;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public ApiResponse<PageResult<InvestmentRecord>> list(@RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) InvestmentStatus status,
                                                    @RequestParam(required = false) Long userId,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    @AuthenticationPrincipal CurrentAccount current) {
        if (size == -1 || size >= 9999) {
            List<InvestmentRecord> all = investmentRepository.findAll(
                    buildInvestmentSpec(keyword, status, userId, current),
                    Sort.by(Sort.Direction.DESC, "id")
            );
            return ApiResponse.ok(PageResult.of(new PageImpl<>(all)));
        }
        return ApiResponse.ok(PageResult.of(investmentRepository.findAll(
                buildInvestmentSpec(keyword, status, userId, current),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))
        )));
    }

    @PostMapping
    @Transactional
    public ApiResponse<InvestmentRecord> create(@Valid @RequestBody Requests.InvestmentRequest request,
                                                @AuthenticationPrincipal CurrentAccount current) {
        if (current.role() != AccountRole.USER) {
            throw new BusinessException("请以普通用户身份添加理财记录");
        }
        InvestmentRecord record = new InvestmentRecord();
        apply(record, request, current);
        InvestmentRecord savedRecord = investmentRepository.save(record);

        // 自动生成“理财投资”本金支出流水
        AccountingCategory category = categoryRepository.findFirstByNameAndTypeAndStatus("理财投资", RecordType.EXPENSE, CommonStatus.ENABLED)
                .orElseGet(() -> {
                    if (categoryRepository.existsByNameAndType("理财投资", RecordType.EXPENSE)) {
                        throw new BusinessException("理财投资分类不可用");
                    }
                    AccountingCategory newCategory = new AccountingCategory();
                    newCategory.name = "理财投资";
                    newCategory.type = RecordType.EXPENSE;
                    newCategory.status = CommonStatus.ENABLED;
                    newCategory.remark = "理财买入本金自动记账分类";
                    return categoryRepository.save(newCategory);
                });

        FinanceRecord fr = new FinanceRecord();
        fr.type = RecordType.EXPENSE;
        fr.categoryId = category.id;
        fr.categoryName = category.name;
        fr.amount = savedRecord.amount;
        fr.remark = buildInvestmentRemark("买入本金自动划出", savedRecord.productName, savedRecord.productCode, savedRecord.id);
        fr.userId = savedRecord.investorId;
        fr.createdBy = savedRecord.investorName;
        fr.recordSource = "AUTO_INVESTMENT";
        fr.recordDate = savedRecord.startDate;
        financeRecordRepository.save(fr);

        return ApiResponse.ok("理财记录创建成功", savedRecord);
    }

    @PutMapping("/{id}")
    @Transactional
    public ApiResponse<InvestmentRecord> update(@PathVariable Long id,
                                                @Valid @RequestBody Requests.InvestmentRequest request,
                                                @AuthenticationPrincipal CurrentAccount current) {
        InvestmentRecord record = investmentRepository.findById(id).orElseThrow(() -> new BusinessException("理财记录不存在"));
        securitySupport.requireOwnerOrAdmin(current, record.investorId);
        if (record.status == InvestmentStatus.REDEEMED) {
            throw new BusinessException("已赎回的理财记录不能修改");
        }
        apply(record, request, current.role() == AccountRole.USER ? current : new CurrentAccount(record.investorId, record.investorName, AccountRole.USER, record.investorName, null));
        InvestmentRecord savedRecord = investmentRepository.save(record);

        // 联动查找并更新买入本金的财务支出流水
        financeRecordRepository.findByUserId(savedRecord.investorId).stream()
                .filter(fr -> "AUTO_INVESTMENT".equals(fr.recordSource) && fr.remark != null && fr.remark.contains("#INVESTMENT_ID=" + savedRecord.id + "#"))
                .forEach(fr -> {
                    fr.amount = savedRecord.amount;
                    fr.recordDate = savedRecord.startDate;
                    fr.remark = buildInvestmentRemark("买入本金自动划出", savedRecord.productName, savedRecord.productCode, savedRecord.id);
                    financeRecordRepository.save(fr);
                });

        return ApiResponse.ok("理财记录更新成功", savedRecord);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal CurrentAccount current) {
        InvestmentRecord record = investmentRepository.findById(id).orElseThrow(() -> new BusinessException("理财记录不存在"));
        securitySupport.requireOwnerOrAdmin(current, record.investorId);
        if (record.status == InvestmentStatus.REDEEMED) {
            throw new BusinessException("已赎回的理财记录不能删除");
        }
        
        investmentRepository.delete(record);

        // 联动删除关联的“理财投资”本金支出财务流水
        financeRecordRepository.findByUserId(record.investorId).stream()
                .filter(fr -> "AUTO_INVESTMENT".equals(fr.recordSource) && fr.remark != null && fr.remark.contains("#INVESTMENT_ID=" + record.id + "#"))
                .forEach(financeRecordRepository::delete);

        return ApiResponse.ok("理财记录删除成功", null);
    }

    @PostMapping("/{id}/redeem")
    @Transactional
    public ApiResponse<RedemptionRecord> redeem(@PathVariable Long id,
                                                @RequestParam(required = false) LocalDate redeemDate,
                                                @RequestParam(required = false) String remark,
                                                @AuthenticationPrincipal CurrentAccount current) {
        InvestmentRecord investment = investmentRepository.findById(id).orElseThrow(() -> new BusinessException("理财记录不存在"));
        securitySupport.requireOwnerOrAdmin(current, investment.investorId);
        if (investment.status == InvestmentStatus.REDEEMED) {
            throw new BusinessException("该理财记录已赎回");
        }
        LocalDate date = redeemDate == null ? LocalDate.now() : redeemDate;
        if (date.isBefore(investment.startDate)) {
            throw new BusinessException("赎回日期不能早于投资开始日");
        }
        int actualDays = (int) ChronoUnit.DAYS.between(investment.startDate, date);
        if (actualDays < 0) {
            throw new BusinessException("赎回日期不能早于投资开始日");
        }
        BigDecimal income = calculateIncome(investment.amount, investment.annualRate, actualDays);

        investment.actualDays = actualDays;
        investment.actualIncome = income;
        investment.status = InvestmentStatus.REDEEMED;
        investmentRepository.save(investment);

        RedemptionRecord redemption = new RedemptionRecord();
        redemption.investmentId = investment.id;
        redemption.productId = investment.productId;
        redemption.productName = investment.productName;
        redemption.amount = investment.amount;
        redemption.actualDays = actualDays;
        redemption.income = income;
        redemption.redeemerId = investment.investorId;
        redemption.redeemerName = investment.investorName;
        redemption.redeemDate = date;
        redemption.remark = remark;
        RedemptionRecord saved = redemptionRepository.save(redemption);
        redemptionRepository.flush();

        // 自动从数据库获取最新实名，防止 Session/Token 滞后
        String latestRealName = userRepository.findById(investment.investorId)
                .map(u -> u.realName)
                .orElse(investment.investorName);

        // 自动生成“理财本金收回”本金收入流水
        AccountingCategory principalCategory = categoryRepository.findFirstByNameAndTypeAndStatus("理财本金收回", RecordType.INCOME, CommonStatus.ENABLED)
                .orElseGet(() -> {
                    if (categoryRepository.existsByNameAndType("理财本金收回", RecordType.INCOME)) {
                        throw new BusinessException("理财本金收回分类不可用");
                    }
                    AccountingCategory newCategory = new AccountingCategory();
                    newCategory.name = "理财本金收回";
                    newCategory.type = RecordType.INCOME;
                    newCategory.status = CommonStatus.ENABLED;
                    newCategory.remark = "理财赎回本金收回自动记账分类";
                    return categoryRepository.save(newCategory);
                });

        FinanceRecord principalRecord = new FinanceRecord();
        principalRecord.type = RecordType.INCOME;
        principalRecord.categoryId = principalCategory.id;
        principalRecord.categoryName = principalCategory.name;
        principalRecord.amount = investment.amount;
        principalRecord.remark = buildInvestmentRemark("赎回本金自动收回", investment.productName, investment.productCode, investment.id);
        principalRecord.userId = investment.investorId;
        principalRecord.createdBy = latestRealName;
        principalRecord.recordSource = "AUTO_REDEMPTION";
        principalRecord.recordDate = date;
        financeRecordRepository.save(principalRecord);

        if (income.compareTo(BigDecimal.ZERO) > 0) {
            AccountingCategory category = categoryRepository.findFirstByNameAndTypeAndStatus("理财收益", RecordType.INCOME, CommonStatus.ENABLED)
                    .orElseGet(() -> categoryRepository.findFirstByNameAndTypeAndStatus("投资收益", RecordType.INCOME, CommonStatus.ENABLED)
                            .orElseGet(() -> {
                                if (categoryRepository.existsByNameAndType("理财收益", RecordType.INCOME)) {
                                    throw new BusinessException("理财收益分类不可用");
                                }
                                AccountingCategory newCategory = new AccountingCategory();
                                newCategory.name = "理财收益";
                                newCategory.type = RecordType.INCOME;
                                newCategory.status = CommonStatus.ENABLED;
                                newCategory.remark = "理财赎回收益自动记账分类";
                                return categoryRepository.save(newCategory);
                            }));

            String remarkText = buildInvestmentRemark("赎回收益自动入账", investment.productName, investment.productCode, investment.id);
            FinanceRecord record = new FinanceRecord();
            record.type = RecordType.INCOME;
            record.categoryId = category.id;
            record.categoryName = category.name;
            record.amount = income;
            record.remark = remarkText.length() > 300 ? remarkText.substring(0, 300) : remarkText;
            record.userId = investment.investorId;
            record.createdBy = latestRealName;
            record.recordSource = "AUTO_REDEMPTION";
            record.recordDate = date;
            financeRecordRepository.save(record);
        }

        return ApiResponse.ok("赎回成功", saved);
    }

    @GetMapping("/redemptions")
    public ApiResponse<PageResult<RedemptionRecord>> redemptions(@RequestParam(required = false) String keyword,
                                                           @RequestParam(required = false) Long userId,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size,
                                                           @AuthenticationPrincipal CurrentAccount current) {
        Specification<RedemptionRecord> spec = (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (current.role() == AccountRole.USER) {
                predicate = cb.and(predicate, cb.equal(root.get("redeemerId"), current.id()));
            } else if (userId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("redeemerId"), userId));
            }
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                predicate = cb.and(predicate, cb.or(cb.like(root.get("productName"), like), cb.like(root.get("redeemerName"), like)));
            }
            return predicate;
        };
        return ApiResponse.ok(PageResult.of(redemptionRepository.findAll(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")))));
    }

    @GetMapping("/fix-data")
    @Transactional
    public ApiResponse<String> fixData(@AuthenticationPrincipal CurrentAccount current) {
        // 1. 基础数据修复（理财编号）
        List<InvestmentRecord> allInvestments = investmentRepository.findAll();
        for (InvestmentRecord record : allInvestments) {
            productRepository.findById(record.productId).ifPresent(p -> {
                record.productCode = p.productCode;
                investmentRepository.save(record);
            });
        }

        // 2. 财务记录深度清理与修复
        List<FinanceRecord> allFinances = financeRecordRepository.findAll();
        for (FinanceRecord fr : allFinances) {
            boolean changed = false;
            // A. 清理备注中的 ID 标记（用户所谓的“乱码”）
            // A. 识别并清理自动入账记录的来源与备注
            if (StringUtils.hasText(fr.remark)) {
                // 模式 1：包含旧的ID标记
                if (fr.remark.contains("#REDEMPTION_INCOME_ID=")) {
                    fr.remark = fr.remark.replaceAll("#REDEMPTION_INCOME_ID=\\d+#", "").trim();
                    fr.recordSource = "AUTO_REDEMPTION";
                    changed = true;
                }
                // 模式 2：包含标准的赎回自动入账文案
                else if (fr.remark.contains("赎回收益自动入账")) {
                    fr.recordSource = "AUTO_REDEMPTION";
                    changed = true;
                }
            }
            // B. 修复缺失的userId关联
            if (fr.userId == null && StringUtils.hasText(fr.createdBy)) {
                Optional<com.bysj.accounting.domain.User> userOpt = userRepository.findByUsername(fr.createdBy);
                if (userOpt.isPresent()) {
                    fr.userId = userOpt.get().id;
                    changed = true;
                }
            }
            // C. 确保来源字段不为空
            if (fr.recordSource == null) {
                fr.recordSource = "MANUAL";
                changed = true;
            }
            if (changed) {
                financeRecordRepository.save(fr);
            }
        }

        // 3. 全量历史姓名同步（以当前用户最新的实名为准）
        if (current != null) {
            userRepository.findById(current.id()).ifPresent(user -> {
                String latestName = user.realName;
                if (!StringUtils.hasText(latestName)) return;

                // 同步所有业务记录的姓名快照
                investmentRepository.findByInvestorId(user.id).forEach(inv -> {
                    inv.investorName = latestName;
                    investmentRepository.save(inv);
                });
                redemptionRepository.findByRedeemerId(user.id).forEach(red -> {
                    red.redeemerName = latestName;
                    redemptionRepository.save(red);
                });
                financeRecordRepository.findByUserId(user.id).forEach(fr -> {
                    fr.createdBy = latestName;
                    if (fr.recordSource == null) fr.recordSource = "MANUAL";
                    financeRecordRepository.save(fr);
                });
            });
        }

        // 4. 自动修补所有历史理财的本金收支财务流水
        AccountingCategory buyCategory = categoryRepository.findFirstByNameAndTypeAndStatus("理财投资", RecordType.EXPENSE, CommonStatus.ENABLED)
                .orElseGet(() -> {
                    AccountingCategory newCategory = new AccountingCategory();
                    newCategory.name = "理财投资";
                    newCategory.type = RecordType.EXPENSE;
                    newCategory.status = CommonStatus.ENABLED;
                    newCategory.remark = "理财买入本金自动记账分类";
                    return categoryRepository.save(newCategory);
                });

        AccountingCategory redeemCategory = categoryRepository.findFirstByNameAndTypeAndStatus("理财本金收回", RecordType.INCOME, CommonStatus.ENABLED)
                .orElseGet(() -> {
                    AccountingCategory newCategory = new AccountingCategory();
                    newCategory.name = "理财本金收回";
                    newCategory.type = RecordType.INCOME;
                    newCategory.status = CommonStatus.ENABLED;
                    newCategory.remark = "理财赎回本金收回自动记账分类";
                    return categoryRepository.save(newCategory);
                });

        List<FinanceRecord> currentFinances = financeRecordRepository.findAll();
        for (InvestmentRecord record : allInvestments) {
            String buyKeyword = "#INVESTMENT_ID=" + record.id + "#";
            
            // A. 检查是否有买入本金记录
            boolean hasBuy = currentFinances.stream()
                    .anyMatch(fr -> "AUTO_INVESTMENT".equals(fr.recordSource) && fr.remark != null && fr.remark.contains(buyKeyword));
            if (!hasBuy) {
                FinanceRecord buyRecord = new FinanceRecord();
                buyRecord.type = RecordType.EXPENSE;
                buyRecord.categoryId = buyCategory.id;
                buyRecord.categoryName = buyCategory.name;
                buyRecord.amount = record.amount;
                buyRecord.remark = buildInvestmentRemark("买入本金自动划出", record.productName, record.productCode, record.id);
                buyRecord.userId = record.investorId;
                buyRecord.createdBy = record.investorName;
                buyRecord.recordSource = "AUTO_INVESTMENT";
                buyRecord.recordDate = record.startDate;
                financeRecordRepository.save(buyRecord);
            }

            // B. 如果已赎回，检查是否有赎回本金记录
            if (record.status == InvestmentStatus.REDEEMED) {
                boolean hasRedeem = currentFinances.stream()
                        .anyMatch(fr -> "AUTO_REDEMPTION".equals(fr.recordSource) && fr.remark != null && fr.remark.contains(buyKeyword) && "理财本金收回".equals(fr.categoryName));
                if (!hasRedeem) {
                    LocalDate redeemDate = redemptionRepository.findAll().stream()
                            .filter(r -> record.id.equals(r.investmentId))
                            .map(r -> r.redeemDate)
                            .findFirst()
                            .orElse(record.startDate.plusDays(record.actualDays != null ? record.actualDays : 30));

                    FinanceRecord redeemRecord = new FinanceRecord();
                    redeemRecord.type = RecordType.INCOME;
                    redeemRecord.categoryId = redeemCategory.id;
                    redeemRecord.categoryName = redeemCategory.name;
                    redeemRecord.amount = record.amount;
                    redeemRecord.remark = buildInvestmentRemark("赎回本金自动收回", record.productName, record.productCode, record.id);
                    redeemRecord.userId = record.investorId;
                    redeemRecord.createdBy = record.investorName;
                    redeemRecord.recordSource = "AUTO_REDEMPTION";
                    redeemRecord.recordDate = redeemDate;
                    financeRecordRepository.save(redeemRecord);
                }
            }
        }

        return ApiResponse.ok("历史数据清洗与姓名同步完毕，本金财务流水已修补平齐");
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats(@RequestParam(required = false) Long userId,
                                                  @AuthenticationPrincipal CurrentAccount current) {
        List<InvestmentRecord> records = investmentRepository.findAll(buildInvestmentSpec(null, null, userId, current));
        Map<String, BigDecimal> amountByType = new LinkedHashMap<>();
        Map<String, BigDecimal> incomeByProduct = new LinkedHashMap<>();
        BigDecimal holding = BigDecimal.ZERO;
        BigDecimal redeemedIncome = BigDecimal.ZERO;
        for (InvestmentRecord record : records) {
            amountByType.merge(record.productType, record.amount, BigDecimal::add);
            BigDecimal income = record.actualIncome == null ? record.expectedIncome : record.actualIncome;
            incomeByProduct.merge(record.productName, income, BigDecimal::add);
            if (record.status == InvestmentStatus.HOLDING) {
                holding = holding.add(record.amount);
            } else if (record.actualIncome != null) {
                redeemedIncome = redeemedIncome.add(record.actualIncome);
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("holdingAmount", holding);
        result.put("redeemedIncome", redeemedIncome);
        result.put("amountByType", amountByType);
        result.put("incomeByProduct", incomeByProduct);
        Long investorId = current.role() == AccountRole.USER ? current.id() : userId;
        if (investorId != null) {
            result.put("investableBalance", investableBalanceForUser(investorId, null));
        }
        return ApiResponse.ok(result);
    }

    private Specification<InvestmentRecord> buildInvestmentSpec(String keyword, InvestmentStatus status, Long userId, CurrentAccount current) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (current.role() == AccountRole.USER) {
                predicate = cb.and(predicate, cb.equal(root.get("investorId"), current.id()));
            } else if (userId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("investorId"), userId));
            }
            if (status != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), status));
            }
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(root.get("productName"), like),
                        cb.like(root.get("productType"), like),
                        cb.like(root.get("investorName"), like)
                ));
            }
            return predicate;
        };
    }

    private void apply(InvestmentRecord record, Requests.InvestmentRequest request, CurrentAccount current) {
        FinancialProduct product = productRepository.findById(request.productId).orElseThrow(() -> new BusinessException("理财产品不存"));
        if (product.status != CommonStatus.ENABLED) {
            throw new BusinessException("该理财产品已停用，不能投");
        }
        if (request.amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("投入金额必须大于0");
        }
        if (request.amount.compareTo(product.minAmount) < 0) {
            throw new BusinessException("投入金额不能低于产品最低投入金");
        }
        if (request.amount.compareTo(product.maxAmount) > 0) {
            throw new BusinessException("投入金额不能高于产品最高投入金");
        }
        User user = userRepository.findById(current.id()).orElseThrow(() -> new BusinessException("用户信息不存"));
        BigDecimal investableBalance = investableBalanceForUser(user.id, record.id);
        if (request.amount.compareTo(investableBalance) > 0) {
            throw new BusinessException("当前可用于理财的结余不足，最多可登记 " + investableBalance.toPlainString() + " ");
        }
        LocalDate start = request.startDate == null ? LocalDate.now() : request.startDate;
        LocalDate expected = request.expectedRedeemDate == null ? start.plusDays(product.termDays == null ? 30 : product.termDays) : request.expectedRedeemDate;
        if (expected.isBefore(start)) {
            throw new BusinessException("预计赎回日期不能早于投资开始日");
        }
        int days = Math.max(1, (int) ChronoUnit.DAYS.between(start, expected));

        record.productId = product.id;
        record.productCode = product.productCode;
        record.productName = product.name;
        record.productType = product.type;
        record.annualRate = product.annualRate;
        record.amount = request.amount;
        record.investorId = user.id;
        record.investorName = user.realName;
        record.startDate = start;
        record.expectedRedeemDate = expected;
        record.actualDays = 0;
        record.expectedIncome = calculateIncome(request.amount, product.annualRate, days);
        record.status = InvestmentStatus.HOLDING;
    }

    private BigDecimal calculateIncome(BigDecimal amount, BigDecimal annualRate, int days) {
        return amount.multiply(annualRate)
                .multiply(BigDecimal.valueOf(days))
                .divide(BigDecimal.valueOf(36500), 2, RoundingMode.HALF_UP);
    }

    private String buildInvestmentRemark(String actionType, String productName, String productCode, Long id) {
        String label = productName != null ? productName : "理财产品";
        StringBuilder sb = new StringBuilder();
        sb.append("理财产品【").append(label).append("】").append(actionType);
        if (StringUtils.hasText(productCode)) {
            sb.append("（理财编号：").append(productCode.trim()).append("）");
        }
        sb.append("#INVESTMENT_ID=").append(id).append("#");
        return sb.toString();
    }

    private BigDecimal financeBalanceForUser(Long userId) {
        Specification<FinanceRecord> spec = (root, query, cb) -> cb.equal(root.get("userId"), userId);
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
        for (FinanceRecord item : financeRecordRepository.findAll(spec)) {
            if (item.type == RecordType.INCOME) {
                income = income.add(item.amount);
            } else {
                expense = expense.add(item.amount);
            }
        }
        return income.subtract(expense);
    }

    private BigDecimal holdingPrincipalForUser(Long userId, Long excludeInvestmentId) {
        Specification<InvestmentRecord> spec = (root, query, cb) -> {
            var predicate = cb.and(
                    cb.equal(root.get("investorId"), userId),
                    cb.equal(root.get("status"), InvestmentStatus.HOLDING)
            );
            if (excludeInvestmentId != null) {
                predicate = cb.and(predicate, cb.notEqual(root.get("id"), excludeInvestmentId));
            }
            return predicate;
        };
        BigDecimal holding = BigDecimal.ZERO;
        for (InvestmentRecord item : investmentRepository.findAll(spec)) {
            holding = holding.add(item.amount);
        }
        return holding;
    }

    private BigDecimal investableBalanceForUser(Long userId, Long excludeInvestmentId) {
        BigDecimal balance = financeBalanceForUser(userId);
        if (excludeInvestmentId != null) {
            InvestmentRecord record = investmentRepository.findById(excludeInvestmentId).orElse(null);
            if (record != null && record.status == InvestmentStatus.HOLDING) {
                balance = balance.add(record.amount);
            }
        }
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return balance.setScale(2, RoundingMode.HALF_UP);
    }
}
