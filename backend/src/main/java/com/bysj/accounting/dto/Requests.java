package com.bysj.accounting.dto;

import com.bysj.accounting.domain.CommonStatus;
import com.bysj.accounting.domain.RecordType;
import com.bysj.accounting.security.AccountRole;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class Requests {
    private Requests() {
    }

    public static class LoginRequest {
        @NotBlank
        public String username;
        @NotBlank
        public String password;
        @NotNull
        public AccountRole role;
    }

    public static class PasswordChangeRequest {
        @NotBlank
        public String oldPassword;
        @NotBlank
        @Size(min = 6, max = 32)
        public String newPassword;
    }

    public static class ResetPasswordRequest {
        @NotBlank
        public String username;
        @NotBlank
        public String realName;
        @NotNull
        public AccountRole role;
        @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
        public String phone;
        @NotBlank
        @Size(min = 6, max = 32, message = "密码长度必须在6-32之间")
        public String newPassword;
    }

    public static class AdminRequest {
        @NotBlank
        public String username;
        public String password;
        public String realName;
        public String phone;
        public CommonStatus status = CommonStatus.ENABLED;
    }

    public static class UserRequest {
        @NotBlank
        public String username;
        public String password;
        @NotBlank
        public String realName;
        @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
        public String phone;
        public String address;
        public String avatarUrl;
        public CommonStatus status = CommonStatus.ENABLED;
    }

    public static class CategoryRequest {
        @NotBlank
        public String name;
        @NotNull
        public RecordType type;
        public CommonStatus status = CommonStatus.ENABLED;
        public String remark;
    }

    public static class FinanceRecordRequest {
        @NotNull
        public RecordType type;
        public Long categoryId;
        @NotBlank
        public String categoryName;
        @NotNull
        @DecimalMin(value = "0.01")
        public BigDecimal amount;
        public String remark;
        public Long userId;
        public LocalDate recordDate;
    }

    public static class ProductRequest {
        public String productCode;
        @NotBlank
        public String name;
        @NotBlank
        public String type;
        @NotNull
        @DecimalMin(value = "0.0000")
        public BigDecimal annualRate;
        public String publisher;
        @NotNull
        @DecimalMin(value = "0.00")
        public BigDecimal minAmount;
        @NotNull
        @DecimalMin(value = "0.01")
        public BigDecimal maxAmount;
        @Min(1)
        public Integer termDays;
        public String riskLevel;
        public CommonStatus status = CommonStatus.ENABLED;
        public String remark;
    }

    public static class InvestmentRequest {
        @NotNull
        public Long productId;
        @NotNull
        @DecimalMin(value = "0.01")
        public BigDecimal amount;
        public LocalDate startDate;
        public LocalDate expectedRedeemDate;
    }

    public static class ShareRequest {
        @NotNull
        public Long investmentId;
        @NotBlank
        public String title;
        @NotBlank
        public String content;
        public CommonStatus status = CommonStatus.ENABLED;
    }

    public static class RegisterRequest {
        @NotBlank(message = "账号不能为空")
        public String username;
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 32, message = "密码长度必须在6-32之间")
        public String password;
        @NotBlank(message = "姓名不能为空")
        public String realName;
    }
}
