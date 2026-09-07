package com.mengzhihua.crm.sales.entity;

import com.mengzhihua.crm.common.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "crm_quote_line_item")
public class QuoteLineItem extends BaseEntity {
    @Column(nullable = false)
    private Long quoteId;
    @Column(nullable = false)
    private Long productId;
    private Long priceBookEntryId;
    @Column(nullable = false)
    private BigDecimal quantity;
    @Column(nullable = false)
    private BigDecimal unitPrice;
    @Column(nullable = false)
    private BigDecimal discountRate = BigDecimal.ZERO;
    @Column(nullable = false)
    private BigDecimal totalPrice;
}
