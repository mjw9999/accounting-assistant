package com.bysj.accounting.repository;

import com.bysj.accounting.domain.InvestmentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InvestmentRecordRepository extends JpaRepository<InvestmentRecord, Long>, JpaSpecificationExecutor<InvestmentRecord> {
    boolean existsByProductId(Long productId);
    java.util.List<InvestmentRecord> findByInvestorId(Long investorId);
}
