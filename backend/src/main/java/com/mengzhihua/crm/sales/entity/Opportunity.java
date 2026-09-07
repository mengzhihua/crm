package com.mengzhihua.crm.sales.entity;
import com.mengzhihua.crm.common.*;
import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity @Table(name="crm_opportunity") @Data
public class Opportunity extends BaseEntity {
    private Long accountId; private Long contactId; private String name; private BigDecimal amount;
    @Enumerated(EnumType.STRING) private Enums.OpportunityStage stage;
    private Integer probability; private LocalDate expectedCloseDate;
    @Enumerated(EnumType.STRING) private Enums.LeadSource source;
    private String competitor; private String lostReason;
    @Column(length=2000) private String description;
    private LocalDateTime closedAt;
}
