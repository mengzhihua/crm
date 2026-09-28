package com.mengzhihua.crm.channel.entity;

import com.mengzhihua.crm.common.BaseEntity;
import com.mengzhihua.crm.common.enums.DeliveryStatus;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "crm_channel_delivery")
public class ChannelDelivery extends BaseEntity {
    @Column(nullable = false)
    private Long channelId;

    @Column(nullable = false)
    private Long notificationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    @Column(length = 1000)
    private String response;

    private int attempts;
}
