package com.mengzhihua.crm.sales.entity;

import com.mengzhihua.crm.common.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "crm_contact")
public class Contact extends BaseEntity {
    private Long accountId;
    private String name;
    private String title;
    private String department;
    private String phone;
    private String email;
    private Boolean isPrimary;

    @Column(length = 1000)
    private String remark;
}
