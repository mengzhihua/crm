package com.mengzhihua.crm.marketing.entity;

import com.mengzhihua.crm.common.BaseEntity;
import com.mengzhihua.crm.common.enums.MemberStatus;
import com.mengzhihua.crm.common.enums.MemberType;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Data
@Entity
@Table(
        name = "crm_campaign_member",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"campaignId", "memberType", "memberId"}
        )
)
public class CampaignMember extends BaseEntity {
    private Long campaignId;
    private Long memberId;

    @Enumerated(EnumType.STRING)
    private MemberType memberType;

    @Enumerated(EnumType.STRING)
    private MemberStatus status;
}
