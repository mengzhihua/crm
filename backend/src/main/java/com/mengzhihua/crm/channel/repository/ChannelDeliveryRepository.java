package com.mengzhihua.crm.channel.repository;

import com.mengzhihua.crm.channel.entity.ChannelDelivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelDeliveryRepository extends JpaRepository<ChannelDelivery, Long> {
    Page<ChannelDelivery> findByChannelId(Long channelId, Pageable pageable);
}
