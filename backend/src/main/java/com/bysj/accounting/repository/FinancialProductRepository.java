package com.bysj.accounting.repository;

import com.bysj.accounting.domain.FinancialProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FinancialProductRepository extends JpaRepository<FinancialProduct, Long>, JpaSpecificationExecutor<FinancialProduct> {
}
