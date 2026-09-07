package com.mengzhihua.crm.sales.entity;

import com.mengzhihua.crm.common.BaseEntity;
import com.mengzhihua.crm.common.enums.LeadSource;
import com.mengzhihua.crm.common.enums.OpportunityStage;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "crm_opportunity")
public class Opportunity extends BaseEntity {
    private Long accountId;
    private Long contactId;
    private Long priceBookId;
    private String name;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private OpportunityStage stage;

    private Integer probability;
    private LocalDate expectedCloseDate;

    @Enumerated(EnumType.STRING)
    private LeadSource source;

    private String competitor;
    private String lostReason;

    @Column(length = 2000)
    private String description;

    private LocalDateTime closedAt;
}
