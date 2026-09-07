package com.mengzhihua.crm.marketing.service;

import com.mengzhihua.crm.common.BizException;
import com.mengzhihua.crm.common.DtoUtil;
import com.mengzhihua.crm.common.PageResult;
import com.mengzhihua.crm.common.enums.CampaignStatus;
import com.mengzhihua.crm.common.enums.CampaignType;
import com.mengzhihua.crm.common.enums.MemberStatus;
import com.mengzhihua.crm.common.enums.MemberType;
import com.mengzhihua.crm.common.enums.OpportunityStage;
import com.mengzhihua.crm.marketing.dto.CampaignMemberRequest;
import com.mengzhihua.crm.marketing.entity.Campaign;
import com.mengzhihua.crm.marketing.entity.CampaignMember;
import com.mengzhihua.crm.marketing.repository.CampaignMemberRepository;
import com.mengzhihua.crm.marketing.repository.CampaignRepository;
import com.mengzhihua.crm.sales.entity.Opportunity;
import com.mengzhihua.crm.sales.repository.OpportunityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CampaignService {
    private final CampaignRepository campaignRepository;
    private final CampaignMemberRepository memberRepository;
    private final OpportunityRepository opportunityRepository;

    public CampaignService(
            CampaignRepository campaignRepository,
            CampaignMemberRepository memberRepository,
            OpportunityRepository opportunityRepository
    ) {
        this.campaignRepository = campaignRepository;
        this.memberRepository = memberRepository;
        this.opportunityRepository = opportunityRepository;
    }

    public PageResult<Campaign> list(
            int page,
            int size,
            String keyword,
            CampaignType type,
            CampaignStatus status
    ) {
        Specification<Campaign> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                predicates.add(builder.like(
                        builder.lower(root.get("name")),
                        "%" + keyword.trim().toLowerCase() + "%"
                ));
            }
            if (type != null) {
                predicates.add(builder.equal(root.get("type"), type));
            }
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
        Page<Campaign> result = campaignRepository.findAll(
                specification,
                DtoUtil.pageable(page, size)
        );
        return DtoUtil.page(result, item -> (Campaign) item);
    }

    public Campaign get(Long id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new BizException("市场活动不存在"));
    }

    public Campaign save(Campaign campaign) {
        if (campaign.getType() == null) {
            campaign.setType(CampaignType.OTHER);
        }
        if (campaign.getStatus() == null) {
            campaign.setStatus(CampaignStatus.PLANNED);
        }
        return campaignRepository.save(campaign);
    }

    public void delete(Long id) {
        get(id);
        campaignRepository.deleteById(id);
    }

    public List<CampaignMember> members(Long campaignId) {
        get(campaignId);
        return memberRepository.findByCampaignId(campaignId);
    }

    @Transactional
    public List<CampaignMember> addMembers(
            Long campaignId,
            CampaignMemberRequest request
    ) {
        get(campaignId);
        List<CampaignMember> result = new ArrayList<>();
        addMemberIds(
                campaignId,
                request.getLeadIds(),
                MemberType.LEAD,
                result
        );
        addMemberIds(
                campaignId,
                request.getContactIds(),
                MemberType.CONTACT,
                result
        );
        return memberRepository.saveAll(result);
    }

    private void addMemberIds(
            Long campaignId,
            List<Long> ids,
            MemberType type,
            List<CampaignMember> result
    ) {
        for (Long memberId : ids) {
            if (memberRepository
                    .findByCampaignIdAndMemberTypeAndMemberId(
                            campaignId,
                            type,
                            memberId
                    )
                    .isPresent()) {
                continue;
            }
            CampaignMember member = new CampaignMember();
            member.setCampaignId(campaignId);
            member.setMemberId(memberId);
            member.setMemberType(type);
            member.setStatus(MemberStatus.SENT);
            result.add(member);
        }
    }

    public void deleteMember(Long campaignId, Long memberId) {
        CampaignMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BizException("活动成员不存在"));
        if (!campaignId.equals(member.getCampaignId())) {
            throw new BizException("活动成员不属于当前活动");
        }
        memberRepository.delete(member);
    }

    public Map<String, Object> stats(Long campaignId) {
        get(campaignId);
        List<CampaignMember> members = memberRepository.findByCampaignId(campaignId);
        long leadCount = members.stream()
                .filter(item -> item.getMemberType() == MemberType.LEAD)
                .count();
        long respondedCount = members.stream()
                .filter(item -> item.getStatus() != MemberStatus.SENT)
                .count();
        List<Opportunity> opportunities = opportunityRepository
                .findByCampaignId(campaignId);
        BigDecimal wonAmount = opportunities.stream()
                .filter(item -> item.getStage() == OpportunityStage.CLOSED_WON)
                .map(Opportunity::getAmount)
                .filter(item -> item != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Campaign campaign = get(campaignId);
        BigDecimal actualCost = campaign.getActualCost() == null
                ? BigDecimal.ZERO
                : campaign.getActualCost();
        BigDecimal roi = actualCost.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : wonAmount.subtract(actualCost)
                .divide(actualCost, 4, BigDecimal.ROUND_HALF_UP);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("memberCount", members.size());
        result.put("respondedCount", respondedCount);
        result.put("leadCount", leadCount);
        result.put("opportunityCount", opportunities.size());
        result.put("wonAmount", wonAmount);
        result.put("roi", roi);
        return result;
    }
}
