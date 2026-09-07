package com.mengzhihua.crm.marketing.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
public class CampaignMemberRequest {
    @NotNull
    private List<Long> leadIds = new ArrayList<>();

    @NotNull
    private List<Long> contactIds = new ArrayList<>();
}
