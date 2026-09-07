package com.mengzhihua.crm.sales.entity;

import com.mengzhihua.crm.common.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "crm_price_book_entry")
public class PriceBookEntry extends BaseEntity {
    @Column(nullable = false)
    private Long priceBookId;
    @Column(nullable = false)
    private Long productId;
    @Column(nullable = false)
    private BigDecimal unitPrice;
    @Column(nullable = false)
    private boolean active = true;
}
