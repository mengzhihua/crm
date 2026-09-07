package com.mengzhihua.crm.marketing.repository;

import com.mengzhihua.crm.common.enums.MemberType;
import com.mengzhihua.crm.marketing.entity.CampaignMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CampaignMemberRepository extends JpaRepository<CampaignMember, Long> {
    List<CampaignMember> findByCampaignId(Long campaignId);

    Optional<CampaignMember> findByCampaignIdAndMemberTypeAndMemberId(
            Long campaignId,
            MemberType memberType,
            Long memberId
    );

    long countByCampaignId(Long campaignId);

    long countByCampaignIdAndStatus(
            Long campaignId,
            com.mengzhihua.crm.common.enums.MemberStatus status
    );
}
