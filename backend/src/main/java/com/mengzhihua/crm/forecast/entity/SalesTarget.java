package com.mengzhihua.crm.forecast.entity;

import com.mengzhihua.crm.common.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.math.BigDecimal;

@Data
@Entity
@Table(
        name = "crm_sales_target",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"owner", "target_year", "target_month"}
        )
)
public class SalesTarget extends BaseEntity {
    @Column(name = "target_year")
    private Integer year;
    @Column(name = "target_month")
    private Integer month;
    private BigDecimal targetAmount = BigDecimal.ZERO;
}
