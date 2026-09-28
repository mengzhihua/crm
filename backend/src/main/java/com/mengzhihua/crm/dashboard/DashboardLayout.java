package com.mengzhihua.crm.dashboard;

import com.mengzhihua.crm.common.BaseEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "crm_dashboard_layout")
public class DashboardLayout extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String username;

    @Lob
    @Column(length = 4000, nullable = false)
    private String widgetsJson;
}
