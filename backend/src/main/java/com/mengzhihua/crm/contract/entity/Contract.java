package com.mengzhihua.crm.contract.entity;

import com.mengzhihua.crm.common.BaseEntity;
import com.mengzhihua.crm.common.enums.ContractStatus;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "crm_contract")
public class Contract extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String contractNo;
    @Column(nullable = false)
    private String name;
    private Long accountId;
    private Long opportunityId;
    private Long quoteId;
    private BigDecimal amount = BigDecimal.ZERO;
    private BigDecimal receivedAmount = BigDecimal.ZERO;
    private BigDecimal receivableAmount = BigDecimal.ZERO;
    private LocalDate signDate;
    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private ContractStatus status = ContractStatus.DRAFT;

    @Column(length = 1000)
    private String paymentTerms;

    @Column(length = 2000)
    private String remark;
}
