package com.bysj.accounting.security;

import com.bysj.accounting.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
public class TokenService {
    @Value("${app.security.token-secret}")
    private String secret;

    @Value("${app.security.token-expiration-hours:24}")
    private long expirationHours;

    public String createToken(CurrentAccount account) {
        long expiresAt = Instant.now().plusSeconds(expirationHours * 3600).getEpochSecond();
        String payload = account.id() + "|" + account.username() + "|" + account.role().name() + "|"
                + safe(account.displayName()) + "|" + safe(account.avatarUrl()) + "|" + expiresAt;
        String encodedPayload = encode(payload.getBytes(StandardCharsets.UTF_8));
        return encodedPayload + "." + sign(encodedPayload);
    }

    public CurrentAccount parseToken(String token) {
        if (token == null || !token.contains(".")) {
            throw new BusinessException("登录状态无效");
        }
        String[] parts = token.split("\\.", 2);
        String expected = sign(parts[0]);
        if (!constantTimeEquals(expected, parts[1])) {
            throw new BusinessException("登录状态校验失败");
        }
        String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        String[] values = payload.split("\\|", -1);
        if (values.length != 6) {
            throw new BusinessException("登录状态格式错误");
        }
        long expiresAt = Long.parseLong(values[5]);
        if (Instant.now().getEpochSecond() > expiresAt) {
            throw new BusinessException("登录状态已过期");
        }
        return new CurrentAccount(Long.valueOf(values[0]), values[1], AccountRole.valueOf(values[2]),
                restore(values[3]), restore(values[4]));
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return encode(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Token签名失败", ex);
        }
    }

    private String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private boolean constantTimeEquals(String a, String b) {
        return MessageDigestCompat.equals(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    private String safe(String value) {
        return value == null ? "" : value.replace("|", "");
    }

    private String restore(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static class MessageDigestCompat {
        static boolean equals(byte[] a, byte[] b) {
            if (a.length != b.length) {
                return false;
            }
            int result = 0;
            for (int i = 0; i < a.length; i++) {
                result |= a[i] ^ b[i];
            }
            return result == 0;
        }
    }
}
