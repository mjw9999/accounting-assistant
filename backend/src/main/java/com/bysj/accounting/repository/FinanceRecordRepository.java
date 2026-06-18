package com.bysj.accounting.repository;

import com.bysj.accounting.domain.FinanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FinanceRecordRepository extends JpaRepository<FinanceRecord, Long>, JpaSpecificationExecutor<FinanceRecord> {
    boolean existsByCategoryId(Long categoryId);

    boolean existsByRemarkContaining(String substring);
    java.util.List<FinanceRecord> findByUserId(Long userId);
}
