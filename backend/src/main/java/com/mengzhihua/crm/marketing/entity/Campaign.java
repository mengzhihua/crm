package com.mengzhihua.crm.marketing.entity;

import com.mengzhihua.crm.common.BaseEntity;
import com.mengzhihua.crm.common.enums.CampaignStatus;
import com.mengzhihua.crm.common.enums.CampaignType;
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
@Table(name = "crm_campaign")
public class Campaign extends BaseEntity {
    private String name;

    @Enumerated(EnumType.STRING)
    private CampaignType type;

    @Enumerated(EnumType.STRING)
    private CampaignStatus status;

    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal budgetCost;
    private BigDecimal actualCost;
    private BigDecimal expectedRevenue;

    @Column(length = 2000)
    private String description;
}
