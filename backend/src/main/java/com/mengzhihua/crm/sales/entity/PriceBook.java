package com.mengzhihua.crm.sales.entity;

import com.mengzhihua.crm.common.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "crm_price_book")
public class PriceBook extends BaseEntity {
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private boolean standard;
    @Column(nullable = false)
    private boolean active = true;
    @Column(length = 2000)
    private String description;
}
