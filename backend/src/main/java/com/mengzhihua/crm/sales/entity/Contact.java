package com.mengzhihua.crm.sales.entity;
import com.mengzhihua.crm.common.*;
import lombok.Data;
import javax.persistence.*;

@Entity @Table(name="crm_contact") @Data
public class Contact extends BaseEntity {
    private Long accountId; private String name; private String title; private String department; private String phone; private String email;
    private Boolean isPrimary;
    @Column(length=1000) private String remark;
}
