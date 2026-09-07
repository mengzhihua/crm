package com.mengzhihua.crm.sales.entity;

import com.mengzhihua.crm.common.BaseEntity;
import com.mengzhihua.crm.common.enums.QuoteStatus;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "crm_quote")
public class Quote extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String quoteNo;
    @Column(nullable = false)
    private Long opportunityId;
    private Long accountId;
    private Long contactId;
    @Column(nullable = false)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuoteStatus status = QuoteStatus.DRAFT;
    private Long priceBookId;
    private LocalDate validUntil;
    private BigDecimal discountRate = BigDecimal.ZERO;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    @Column(length = 2000)
    private String remark;
    @Column(nullable = false)
    private boolean primaryQuote;
}
