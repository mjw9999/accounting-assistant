package com.bysj.accounting.controller;

import com.bysj.accounting.common.ApiResponse;
import com.bysj.accounting.common.BusinessException;
import com.bysj.accounting.domain.CommonStatus;
import com.bysj.accounting.domain.FinanceRecord;
import com.bysj.accounting.domain.RecordType;
import com.bysj.accounting.domain.User;
import com.bysj.accounting.dto.Requests;
import com.bysj.accounting.repository.AccountingCategoryRepository;
import com.bysj.accounting.repository.FinanceRecordRepository;
import com.bysj.accounting.repository.UserRepository;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.persistence.criteria.Predicate;

@RestController
@RequestMapping("/api/finance-records")
@SuppressWarnings("null")
/**
 * 财务记录控制器 (Finance Record Controller)
 * 负责处理用户日常记账（收支明细）的增删改查、数据统计与导入等接口请求。
 */
public class FinanceRecordController {
    // 依赖注入的数据访问层(Repository)和安全支持类
    private final FinanceRecordRepository financeRecordRepository; // 财务记录数据库操作
    private final UserRepository userRepository;                   // 用户数据库操作
    private final AccountingCategoryRepository categoryRepository; // 账目分类数据库操作
    private final SecuritySupport securitySupport;                 // 安全与权限校验工具

    // 构造函数注入依赖，Spring官方推荐的依赖注入方式
    public FinanceRecordController(FinanceRecordRepository financeRecordRepository,
                                   UserRepository userRepository,
                                   AccountingCategoryRepository categoryRepository,
                                   SecuritySupport securitySupport) {
        this.financeRecordRepository = financeRecordRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.securitySupport = securitySupport;
    }

    /**
     * 查询财务记录列表接口（支持分页和多条件动态查询）
     * 对应前端的明细列表展示功能
     *
     * @param type      记录类型（INCOME:收入 / EXPENSE:支出）
     * @param keyword   搜索关键字（支持匹配分类名称、备注、创建人）
     * @param userId    查询指定用户的记录（管理员可以查所有人，普通用户只能查自己）
     * @param startDate 查询开始日期
     * @param endDate   查询结束日期
     * @param page      当前页码（从0开始）
     * @param size      每页显示的条数（如果传入-1或大于等于9999，则查询全部，不分页）
     * @param current   当前发起请求的登录用户（Spring Security自动注入）
     * @return          返回包含财务记录分页数据的标准API响应
     */
    @GetMapping
    public ApiResponse<PageResult<FinanceRecord>> list(@RequestParam(required = false) RecordType type,
                                                 @RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) Long userId,
                                                 @RequestParam(required = false) LocalDate startDate,
                                                 @RequestParam(required = false) LocalDate endDate,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size,
                                                 @AuthenticationPrincipal CurrentAccount current) {
        // 判断是否需要分页：如果size为-1或非常大，则直接查询所有符合条件的数据
        if (size == -1 || size >= 9999) {
            // 使用 buildSpec 动态构建查询条件，并按日期降序、ID降序排序
            List<FinanceRecord> all = financeRecordRepository.findAll(
                    buildSpec(type, keyword, userId, startDate, endDate, current),
                    Sort.by(Sort.Direction.DESC, "recordDate", "id")
            );
            return ApiResponse.ok(PageResult.of(new PageImpl<>(all)));
        }
        // 如果需要分页，则传入 PageRequest 对象，包含页码、每页大小和排序规则
        return ApiResponse.ok(PageResult.of(financeRecordRepository.findAll(
                buildSpec(type, keyword, userId, startDate, endDate, current),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "recordDate", "id"))
        )));
    }

    /**
     * 获取财务数据汇总和统计图表所需数据
     * 对应前端的控制台（Dashboard）和数据报表功能
     */
    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary(@RequestParam(required = false) RecordType type,
                                                    @RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) Long userId,
                                                    @RequestParam(required = false) LocalDate startDate,
                                                    @RequestParam(required = false) LocalDate endDate,
                                                    @AuthenticationPrincipal CurrentAccount current) {
        // 先根据条件查询出所有记录，并按日期升序排序，方便后续按月份统计趋势
        List<FinanceRecord> records = financeRecordRepository.findAll(
                buildSpec(type, keyword, userId, startDate, endDate, current),
                Sort.by(Sort.Direction.ASC, "recordDate")
        );
        
        // 初始化统计所需的基础变量
        BigDecimal income = BigDecimal.ZERO;  // 总收入
        BigDecimal expense = BigDecimal.ZERO; // 总支出
        BigDecimal min = null;                // 记录中的最小金额

        // 用于图表展示的各类统计Map
        Map<String, BigDecimal> byCategory = new LinkedHashMap<>();       // 按分类汇总总金额（不管收支）
        Map<String, BigDecimal> incomeByCategory = new LinkedHashMap<>(); // 按分类汇总收入金额
        Map<String, BigDecimal> expenseByCategory = new LinkedHashMap<>();// 按分类汇总支出金额
        Map<String, Integer> incomeCategoryCount = new HashMap<>();       // 各收入分类的记录条数
        Map<String, Integer> expenseCategoryCount = new HashMap<>();      // 各支出分类的记录条数
        Map<String, BigDecimal> byMonth = new LinkedHashMap<>();          // 按月汇总总净值（收入-支出）
        Map<String, BigDecimal> incomeByMonth = new LinkedHashMap<>();    // 按月汇总收入
        Map<String, BigDecimal> expenseByMonth = new LinkedHashMap<>();   // 按月汇总支出
        
        YearMonth currentMonth = YearMonth.now(); // 当前月份，用于计算当月结余
        BigDecimal monthIncome = BigDecimal.ZERO; // 当月总收入
        BigDecimal monthExpense = BigDecimal.ZERO;// 当月总支出

        // 遍历所有查询到的记录进行各项数据的累加和统计
        for (FinanceRecord item : records) {
            if (item.type == RecordType.INCOME) { // 如果是收入记录
                income = income.add(item.amount); // 累加总收入
                incomeByCategory.merge(item.categoryName, item.amount, BigDecimal::add); // 累加该分类的收入
                incomeCategoryCount.put(item.categoryName, incomeCategoryCount.getOrDefault(item.categoryName, 0) + 1); // 该分类次数+1
            } else { // 如果是支出记录
                expense = expense.add(item.amount); // 累加总支出
                expenseByCategory.merge(item.categoryName, item.amount, BigDecimal::add); // 累加该分类的支出
                expenseCategoryCount.put(item.categoryName, expenseCategoryCount.getOrDefault(item.categoryName, 0) + 1); // 该分类次数+1
            }
            
            // 找出金额最小的一笔记录
            if (min == null || item.amount.compareTo(min) < 0) {
                min = item.amount;
            }
            
            // 按分类累加总金额（不区分收支，通常用于饼图分布展示）
            byCategory.merge(item.categoryName, item.amount, BigDecimal::add);
            
            // 获取该记录所属的月份，例如 "2023-10"
            String monthKey = YearMonth.from(item.recordDate).toString();
            if (item.type == RecordType.INCOME) {
                incomeByMonth.merge(monthKey, item.amount, BigDecimal::add);
                byMonth.merge(monthKey, item.amount, BigDecimal::add); // 收入为正数累加到 byMonth
            } else {
                expenseByMonth.merge(monthKey, item.amount, BigDecimal::add);
                byMonth.merge(monthKey, item.amount.negate(), BigDecimal::add); // 支出转换为负数累加到 byMonth
            }
            
            // 如果记录发生在本月，则累加到当月收支中
            if (YearMonth.from(item.recordDate).equals(currentMonth)) {
                if (item.type == RecordType.INCOME) {
                    monthIncome = monthIncome.add(item.amount);
                } else {
                    monthExpense = monthExpense.add(item.amount);
                }
            }
        }

        // 将计算好的各项数据放入 result Map 中返回给前端
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("income", income); // 期间总收入
        result.put("expense", expense); // 期间总支出
        result.put("balance", income.subtract(expense)); // 期间总结余（收入 - 支出）
        result.put("minAmount", min == null ? BigDecimal.ZERO : min); // 最小金额
        result.put("monthBalance", monthIncome.subtract(monthExpense)); // 当月结余
        result.put("categoryChart", byCategory); // 分类汇总数据
        
        // 构建包含百分比的详细分类统计列表（收入和支出合并）
        List<Map<String, Object>> categoryStats = new ArrayList<>();
        categoryStats.addAll(buildCategoryStats(incomeByCategory, incomeCategoryCount, income, RecordType.INCOME));
        categoryStats.addAll(buildCategoryStats(expenseByCategory, expenseCategoryCount, expense, RecordType.EXPENSE));
        result.put("categoryStats", categoryStats); // 详细的分类统计数据（包含占比）
        
        result.put("monthChart", byMonth); // 按月结余数据
        result.put("monthTrend", buildMonthTrend(incomeByMonth, expenseByMonth)); // 按月收支详细趋势数据（包含近12个月的空缺补齐）
        return ApiResponse.ok(result);
    }

    /**
     * 新增记账记录接口
     */
    @PostMapping
    public ApiResponse<FinanceRecord> create(@Valid @RequestBody Requests.FinanceRecordRequest request,
                                             @AuthenticationPrincipal CurrentAccount current) {
        FinanceRecord record = new FinanceRecord();
        // 将请求参数和当前用户信息应用到实体对象中
        apply(record, request, current);
        // 保存到数据库并返回
        return ApiResponse.ok("记账信息创建成功", financeRecordRepository.save(record));
    }

    /**
     * 修改记账记录接口
     * @param id 路径参数，要修改的记录ID
     */
    @PutMapping("/{id}")
    public ApiResponse<FinanceRecord> update(@PathVariable Long id,
                                             @Valid @RequestBody Requests.FinanceRecordRequest request,
                                             @AuthenticationPrincipal CurrentAccount current) {
        // 先从数据库查询要修改的记录，找不到则抛出异常
        FinanceRecord record = financeRecordRepository.findById(id).orElseThrow(() -> new BusinessException("记账信息不存在"));
        
        // 权限校验：只能修改自己的记录（管理员除外）
        securitySupport.requireOwnerOrAdmin(current, record.userId);
        
        // 业务校验：如果记录是由理财模块“自动赎回”生成的，则不允许手动修改，保证数据一致性
        if ("AUTO_REDEMPTION".equals(record.recordSource) || (record.remark != null && record.remark.contains("赎回收益自动入账"))) {
            throw new BusinessException("该记录由理财赎回自动生成，不支持手动修改");
        }
        
        // 更新字段内容
        apply(record, request, current);
        return ApiResponse.ok("记账信息更新成功", financeRecordRepository.save(record));
    }

    /**
     * 删除记账记录接口
     * @param id 路径参数，要删除的记录ID
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal CurrentAccount current) {
        // 查找记录
        FinanceRecord record = financeRecordRepository.findById(id).orElseThrow(() -> new BusinessException("记账信息不存在"));
        
        // 权限校验：只能删除自己的记录（管理员除外）
        securitySupport.requireOwnerOrAdmin(current, record.userId);
        
        // 业务校验：禁止删除理财系统自动生成的记账记录
        if ("AUTO_REDEMPTION".equals(record.recordSource) || (record.remark != null && record.remark.contains("赎回收益自动入账"))) {
            throw new BusinessException("该记录由理财赎回自动生成，不支持手动删除");
        }
        
        // 从数据库删除
        financeRecordRepository.delete(record);
        return ApiResponse.ok("记账信息删除成功", null);
    }

    /**
     * 导入CSV格式的记账记录接口
     * 支持批量导入外部记账数据
     */
    @PostMapping("/import-csv")
    public ApiResponse<Map<String, Integer>> importCsv(@RequestPart("file") MultipartFile file,
                                                       @RequestParam(required = false) Long userId,
                                                       @AuthenticationPrincipal CurrentAccount current) throws IOException {
        // 确定导入的目标用户（如果是管理员可以指定userId导入到别人名下，否则只能导入到自己名下）
        Long targetUserId = current.role() == AccountRole.ADMIN && userId != null ? userId : current.id();
        User user = userRepository.findById(targetUserId).orElseThrow(() -> new BusinessException("记账用户不存在"));
        
        int count = 0; // 记录成功导入的条数
        int failedCount = 0; // 记录解析失败跳过的条数
        
        // 使用 BufferedReader 逐行读取上传的 CSV 文件内容
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 跳过空行和表头行（自动兼容无 BOM 或是带 BOM 的 "\uFEFF类型" 表头行）
                if (!StringUtils.hasText(line) || line.startsWith("类型") || line.startsWith("\uFEFF类型")) {
                    continue;
                }
                
                try {
                    // 按逗号分割每行数据
                    String[] values = line.split(",", -1);
                    // 确保至少有4列数据（类型、分类、金额、备注）
                    if (values.length < 4) {
                        continue;
                    }
                    
                    FinanceRecord record = new FinanceRecord();
                    // 去除可能残存的 BOM 头字符
                    String typeStr = values[0].replace("\uFEFF", "").trim();
                    record.type = parseType(typeStr);
                    record.categoryName = values[1].trim();
                    record.amount = new BigDecimal(values[2].trim());
                    record.remark = values[3].trim();
                    
                    // 解析日期，如果有第5列且不为空，则解析为日期；否则使用当前日期
                    record.recordDate = values.length >= 5 && StringUtils.hasText(values[4]) ? LocalDate.parse(values[4].trim()) : LocalDate.now();
                    
                    record.userId = user.id;
                    record.createdBy = user.realName;
                    record.recordSource = "MANUAL"; // 标记为手动导入
                    
                    // 逐条保存到数据库
                    financeRecordRepository.save(record);
                    count++;
                } catch (Exception e) {
                    failedCount++; // 一行出错时仅累加跳过数，不崩溃，继续解析下一行
                }
            }
        }
        
        // 拼装前台可视的导入统计信息
        String message = "导入成功！共成功导入 " + count + " 条。";
        if (failedCount > 0) {
            message = "导入处理完成。成功 " + count + " 条，跳过 " + failedCount + " 条格式错误记录。";
        }
        
        // 返回导入成功的总条数
        return ApiResponse.ok(message, Map.of("count", count));
    }

    /**
     * 辅助方法：使用 JPA Criteria API 动态构建查询条件（Specification）
     * 这是一种类型安全且灵活的高级查询构建方式，类似于 MyBatis 的动态 SQL
     */
    private Specification<FinanceRecord> buildSpec(RecordType type, String keyword, Long userId,
                                                   LocalDate startDate, LocalDate endDate,
                                                   CurrentAccount current) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction(); // 初始一个默认为真的条件 (1=1)
            
            // 如果是普通用户，强制加上 userId = 自己的ID 的条件（数据隔离）
            if (current.role() == AccountRole.USER) {
                predicate = cb.and(predicate, cb.equal(root.get("userId"), current.id()));
            } else if (userId != null) { 
                // 如果是管理员并且指定了查询某个人的，加上 userId 条件
                predicate = cb.and(predicate, cb.equal(root.get("userId"), userId));
            }
            
            // 按收支类型筛选
            if (type != null) {
                predicate = cb.and(predicate, cb.equal(root.get("type"), type));
            }
            
            // 按日期范围筛选 ( >= startDate )
            if (startDate != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("recordDate"), startDate));
            }
            
            // 按日期范围筛选 ( <= endDate )
            if (endDate != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("recordDate"), endDate));
            }
            
            // 模糊搜索：分类名、备注或创建人包含关键字即可（使用 OR 连接）
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(root.get("categoryName"), like),
                        cb.like(root.get("remark"), like),
                        cb.like(root.get("createdBy"), like)
                ));
            }
            return predicate;
        };
    }

    /**
     * 辅助方法：将请求DTO (Data Transfer Object) 中的属性赋值给数据库实体，并补充必要信息
     */
    private void apply(FinanceRecord record, Requests.FinanceRecordRequest request, CurrentAccount current) {
        // 确定该记录所属的用户
        Long targetUserId = current.role() == AccountRole.ADMIN && request.userId != null ? request.userId : current.id();
        User user = userRepository.findById(targetUserId).orElseThrow(() -> new BusinessException("记账用户不存在"));
        
        // 如果请求中包含分类ID，则验证分类是否存在且处于启用状态
        if (request.categoryId != null) {
            com.bysj.accounting.domain.AccountingCategory category = categoryRepository.findById(request.categoryId).orElseThrow(() -> new BusinessException("分类不存在"));
            if (category.status != CommonStatus.ENABLED) {
                throw new BusinessException("该分类已停用，不能用于记账");
            }
        }
        
        // 复制基本属性
        record.type = request.type;
        record.categoryId = request.categoryId;
        record.categoryName = request.categoryName;
        record.amount = request.amount;
        record.remark = request.remark;
        
        // 如果未指定日期，默认使用今天
        record.recordDate = request.recordDate == null ? LocalDate.now() : request.recordDate;
        record.userId = user.id;
        record.createdBy = user.realName; // 记录创建人姓名
        
        // 设置记录来源
        if (record.recordSource == null) {
            record.recordSource = "MANUAL"; // 默认为手动录入
        }
    }

    /**
     * 辅助方法：构建近12个月的收支趋势图表数据
     * 确保即使某个月没有数据，也会被补齐为0，以便前端图表正常显示连续的X轴
     */
    private List<Map<String, Object>> buildMonthTrend(Map<String, BigDecimal> incomeByMonth,
                                                       Map<String, BigDecimal> expenseByMonth) {
        List<Map<String, Object>> monthTrend = new ArrayList<>();
        YearMonth now = YearMonth.now(); // 获取当前年月
        
        // 循环推算过去11个月和当月（共12个月），从旧到新排列

        for (int i = 11; i >= 0; i--) {
            YearMonth month = now.minusMonths(i);
            String key = month.toString(); // 格式如 "2023-10"

            // 获取该月的收支总额，若不存在则补0
            BigDecimal monthIncome = incomeByMonth.getOrDefault(key, BigDecimal.ZERO);
            BigDecimal monthExpense = expenseByMonth.getOrDefault(key, BigDecimal.ZERO);

            // 组装返回给前端的一行数据
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("month", key);
            row.put("income", monthIncome);
            row.put("expense", monthExpense);
            row.put("balance", monthIncome.subtract(monthExpense)); // 结余 = 收入 - 支出
            monthTrend.add(row);
        }
        return monthTrend;
    }

    /**
     * 辅助方法：计算每个分类的金额及占总金额的百分比
     */
    private List<Map<String, Object>> buildCategoryStats(Map<String, BigDecimal> byCategory,
                                                         Map<String, Integer> categoryCount,
                                                         BigDecimal total,
                                                         RecordType type) {
        List<Map<String, Object>> stats = new ArrayList<>();
        // 遍历每一个分类的统计数据
        for (Map.Entry<String, BigDecimal> entry : byCategory.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("categoryName", entry.getKey()); // 分类名称
            row.put("amount", entry.getValue());     // 分类总金额
            row.put("type", type.name());            // 记录类型（收入/支出）
            row.put("count", categoryCount.getOrDefault(entry.getKey(), 0)); // 记录笔数

            // 计算占比百分比：(分类金额 * 100) / 总金额，保留2位小数，四舍五入
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                row.put("percent", entry.getValue()
                        .multiply(BigDecimal.valueOf(100))
                        .divide(total, 2, RoundingMode.HALF_UP));
            } else {
                row.put("percent", BigDecimal.ZERO);
            }
            stats.add(row);
        }
        return stats;
    }

    /**
     * 辅助方法：解析CSV导入时的收支类型字符串为枚举
     */
    private RecordType parseType(String value) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException("记账类型不能为空");
        }
        String text = value.trim();
        // 容错处理：支持中文"收入"或英文"INCOME"
        if ("收入".equals(text) || "INCOME".equalsIgnoreCase(text)) {
            return RecordType.INCOME;
        }
        if ("支出".equals(text) || "EXPENSE".equalsIgnoreCase(text)) {
            return RecordType.EXPENSE;
        }
        throw new BusinessException("不支持的记账类型: " + value);
    }
}
