package com.mengzhihua.crm.contract.repository;

import com.mengzhihua.crm.contract.entity.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {
    List<PaymentRecord> findByContractIdOrderByPaidDateDesc(Long contractId);

    List<PaymentRecord> findByPlanId(Long planId);
}
