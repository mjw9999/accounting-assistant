package com.bysj.accounting.repository;

import com.bysj.accounting.domain.Share;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ShareRepository extends JpaRepository<Share, Long>, JpaSpecificationExecutor<Share> {
    List<Share> findByUserId(Long userId);
}
