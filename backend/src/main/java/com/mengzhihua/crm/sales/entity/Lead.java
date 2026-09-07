package com.mengzhihua.crm.sales.entity;

import com.mengzhihua.crm.common.BaseEntity;
import com.mengzhihua.crm.common.enums.LeadSource;
import com.mengzhihua.crm.common.enums.LeadStatus;
import com.mengzhihua.crm.common.enums.Rating;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "crm_lead")
public class Lead extends BaseEntity {
    private String name;
    private String company;
    private String title;
    private String phone;
    private String email;
    private String industry;
    private Long campaignId;

    @Enumerated(EnumType.STRING)
    private LeadSource source;

    @Enumerated(EnumType.STRING)
    private LeadStatus status;

    @Enumerated(EnumType.STRING)
    private Rating rating;

    @Column(length = 2000)
    private String remark;

    private Long convertedAccountId;
    private Long convertedContactId;
    private Long convertedOpportunityId;
    private LocalDateTime convertedAt;
}
