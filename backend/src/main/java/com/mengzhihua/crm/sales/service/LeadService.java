package com.mengzhihua.crm.sales.service;
import com.mengzhihua.crm.common.*;
import com.mengzhihua.crm.sales.entity.*;
import com.mengzhihua.crm.sales.repository.*;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.time.*;
import java.util.*;

@Service public class LeadService {
    private final LeadRepository leads; private final AccountRepository accounts; private final ContactRepository contacts; private final OpportunityRepository opportunities;
    public LeadService(LeadRepository l,AccountRepository a,ContactRepository c,OpportunityRepository o){leads=l;accounts=a;contacts=c;opportunities=o;}
    public PageResult<Lead> list(int page,int size,String keyword){return DtoUtil.page(keyword==null||keyword.isEmpty()?leads.findAll(DtoUtil.pageable(page,size)):leads.findByNameContainingIgnoreCaseOrCompanyContainingIgnoreCase(keyword,keyword,DtoUtil.pageable(page,size)),x->(Lead)x);}
    public Lead get(Long id){return leads.findById(id).orElseThrow(()->new BizException("线索不存在"));}
    public Lead save(Lead x){if(x.getStatus()==null)x.setStatus(Enums.LeadStatus.NEW); if(x.getRating()==null)x.setRating(Enums.Rating.WARM); return leads.save(x);}
    public void delete(Long id){leads.deleteById(id);}
    @Transactional public Map<String,Long> convert(Long id, Map<String,Object> body){
        Lead l=get(id); if(l.getStatus()==Enums.LeadStatus.CONVERTED)throw new BizException("线索已转化");
        Long accountId=body.get("accountId")==null?null:Long.valueOf(body.get("accountId").toString());
        Account a=accountId==null?new Account():accounts.findById(accountId).orElseThrow(()->new BizException("客户不存在"));
        if(accountId==null){a.setName(l.getCompany()==null?l.getName():l.getCompany());a.setType(Enums.AccountType.PROSPECT);a=accounts.save(a);}
        Contact c=new Contact();c.setAccountId(a.getId());c.setName(l.getName());c.setPhone(l.getPhone());c.setEmail(l.getEmail());c.setTitle(l.getTitle());c=contacts.save(c);
        Long oid=null; Boolean create=(Boolean)body.get("createOpportunity");
        if(Boolean.TRUE.equals(create)){Opportunity o=new Opportunity();o.setAccountId(a.getId());o.setName((String)body.getOrDefault("opportunityName",l.getName()+"商机"));o.setAmount(body.get("amount")==null?null:new java.math.BigDecimal(body.get("amount").toString()));o.setExpectedCloseDate(body.get("expectedCloseDate")==null?null:LocalDate.parse(body.get("expectedCloseDate").toString()));o.setStage(Enums.OpportunityStage.QUALIFICATION);o.setProbability(10);oid=opportunities.save(o).getId();}
        l.setStatus(Enums.LeadStatus.CONVERTED);l.setConvertedAccountId(a.getId());l.setConvertedContactId(c.getId());l.setConvertedOpportunityId(oid);l.setConvertedAt(LocalDateTime.now());leads.save(l);
        Map<String,Long> result=new LinkedHashMap<>();result.put("accountId",a.getId());result.put("contactId",c.getId());result.put("opportunityId",oid);return result;
    }
}
