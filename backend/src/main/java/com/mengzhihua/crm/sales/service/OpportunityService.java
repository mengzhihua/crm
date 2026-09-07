package com.mengzhihua.crm.sales.service;
import com.mengzhihua.crm.common.*; import com.mengzhihua.crm.sales.entity.*; import com.mengzhihua.crm.sales.repository.*;
import org.springframework.stereotype.Service; import java.time.*; import java.util.*;
@Service public class OpportunityService {
    private final OpportunityRepository repo; public OpportunityService(OpportunityRepository r){repo=r;}
    public PageResult<Opportunity> list(int page,int size,String keyword){return DtoUtil.page(keyword==null||keyword.isEmpty()?repo.findAll(DtoUtil.pageable(page,size)):repo.findByNameContainingIgnoreCase(keyword,DtoUtil.pageable(page,size)),x->(Opportunity)x);}
    public Opportunity get(Long id){return repo.findById(id).orElseThrow(()->new BizException("商机不存在"));}
    public Opportunity save(Opportunity x){if(x.getStage()==null)x.setStage(Enums.OpportunityStage.QUALIFICATION);if(x.getProbability()==null)x.setProbability(x.getStage().getProbability());return repo.save(x);}
    public void delete(Long id){repo.deleteById(id);}
    public Opportunity stage(Long id,Map<String,Object> body){Opportunity o=get(id);Enums.OpportunityStage s=Enums.OpportunityStage.valueOf(body.get("stage").toString());if(s==Enums.OpportunityStage.CLOSED_LOST&&(body.get("lostReason")==null||body.get("lostReason").toString().trim().isEmpty()))throw new BizException("丢单必须填写原因");o.setStage(s);o.setProbability(body.get("probability")==null?s.getProbability():Integer.valueOf(body.get("probability").toString()));if(body.get("lostReason")!=null)o.setLostReason(body.get("lostReason").toString());if(s==Enums.OpportunityStage.CLOSED_WON||s==Enums.OpportunityStage.CLOSED_LOST)o.setClosedAt(LocalDateTime.now());return repo.save(o);}
    public List<Map<String,Object>> pipeline(){List<Map<String,Object>> out=new ArrayList<>();for(Enums.OpportunityStage s:Enums.OpportunityStage.values()){List<Opportunity> list=repo.findByStage(s);java.math.BigDecimal amount=list.stream().map(Opportunity::getAmount).filter(Objects::nonNull).reduce(java.math.BigDecimal.ZERO,java.math.BigDecimal::add);Map<String,Object> m=new LinkedHashMap<>();m.put("stage",s);m.put("count",list.size());m.put("sumAmount",amount);out.add(m);}return out;}
}
