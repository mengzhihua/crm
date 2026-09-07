package com.mengzhihua.crm.sales.entity;
import com.mengzhihua.crm.common.*;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="crm_lead") @Data
public class Lead extends BaseEntity {
    private String name; private String company; private String title; private String phone; private String email; private String industry;
    @Enumerated(EnumType.STRING) private Enums.LeadSource source;
    @Enumerated(EnumType.STRING) private Enums.LeadStatus status;
    @Enumerated(EnumType.STRING) private Enums.Rating rating;
    @Column(length=2000) private String remark;
    private Long convertedAccountId; private Long convertedContactId; private Long convertedOpportunityId; private LocalDateTime convertedAt;
}
