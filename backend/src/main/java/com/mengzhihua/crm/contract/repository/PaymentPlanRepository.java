package com.mengzhihua.crm.contract.repository;

import com.mengzhihua.crm.contract.entity.PaymentPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentPlanRepository extends JpaRepository<PaymentPlan, Long> {
    List<PaymentPlan> findByContractIdOrderBySeqAsc(Long contractId);
}
