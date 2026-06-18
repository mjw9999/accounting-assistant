package com.bysj.accounting.repository;

import com.bysj.accounting.domain.RedemptionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RedemptionRecordRepository extends JpaRepository<RedemptionRecord, Long>, JpaSpecificationExecutor<RedemptionRecord> {
    java.util.List<RedemptionRecord> findByRedeemerId(Long redeemerId);
}
