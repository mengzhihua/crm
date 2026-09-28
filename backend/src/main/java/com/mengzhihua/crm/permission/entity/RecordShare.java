package com.mengzhihua.crm.permission.entity;

import com.mengzhihua.crm.common.BaseEntity;
import com.mengzhihua.crm.common.enums.AccessLevel;
import com.mengzhihua.crm.common.enums.DataObjectType;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "crm_record_share")
public class RecordShare extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DataObjectType objectType;

    @Column(nullable = false)
    private Long recordId;

    @Column(nullable = false)
    private String sharedWith;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessLevel accessLevel;

    @Column(nullable = false)
    private String sharedBy;
}
