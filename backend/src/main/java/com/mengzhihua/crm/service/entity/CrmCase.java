package com.mengzhihua.crm.service.entity;
import com.mengzhihua.crm.common.*;
import lombok.Data;
import javax.persistence.*;
import java.time.*;

@Entity @Table(name="crm_case", uniqueConstraints=@UniqueConstraint(columnNames="caseNo")) @Data
public class CrmCase extends BaseEntity {
    @Column(nullable=false) private String caseNo; private Long accountId; private Long contactId; private String subject;
    @Column(length=3000) private String description;
    @Enumerated(EnumType.STRING) private Enums.CaseType type;
    @Enumerated(EnumType.STRING) private Enums.CasePriority priority;
    @Enumerated(EnumType.STRING) private Enums.CaseStatus status;
    @Enumerated(EnumType.STRING) private Enums.CaseOrigin origin;
    private LocalDateTime slaDueAt; private Boolean escalated;
    @Column(length=3000) private String solution; private LocalDateTime resolvedAt; private LocalDateTime closedAt;
}
