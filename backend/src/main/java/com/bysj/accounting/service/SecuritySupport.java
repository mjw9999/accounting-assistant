package com.bysj.accounting.service;

import com.bysj.accounting.common.BusinessException;
import com.bysj.accounting.security.AccountRole;
import com.bysj.accounting.security.CurrentAccount;
import org.springframework.stereotype.Component;

@Component
public class SecuritySupport {
    public void requireOwnerOrAdmin(CurrentAccount account, Long ownerId) {
        if (account.role() == AccountRole.ADMIN) {
            return;
        }
        if (!account.id().equals(ownerId)) {
            throw new BusinessException("只能操作自己的数据");
        }
    }
}
