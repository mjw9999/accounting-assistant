package com.bysj.accounting.repository;

import com.bysj.accounting.domain.AccountingCategory;
import com.bysj.accounting.domain.CommonStatus;
import com.bysj.accounting.domain.RecordType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AccountingCategoryRepository extends JpaRepository<AccountingCategory, Long>, JpaSpecificationExecutor<AccountingCategory> {
    boolean existsByNameAndType(String name, RecordType type);

    Optional<AccountingCategory> findFirstByNameAndTypeAndStatus(String name, RecordType type, CommonStatus status);
}
