package com.mengzhihua.crm.sales.entity;
import com.mengzhihua.crm.common.*;
import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;

@Entity @Table(name="crm_account", uniqueConstraints=@UniqueConstraint(columnNames="name")) @Data
public class Account extends BaseEntity {
    @Column(nullable=false) private String name; private String industry;
    @Enumerated(EnumType.STRING) private Enums.AccountType type;
    @Enumerated(EnumType.STRING) private Enums.AccountLevel level;
    private String phone; private String website; private String address; private BigDecimal annualRevenue; private Integer employees;
    @Column(length=2000) private String description;
}
