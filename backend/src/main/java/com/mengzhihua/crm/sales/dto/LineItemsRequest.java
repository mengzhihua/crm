package com.mengzhihua.crm.sales.dto;

import lombok.Data;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Data
public class LineItemsRequest {
    @Valid
    private List<LineItemRequest> items = new ArrayList<>();
}
