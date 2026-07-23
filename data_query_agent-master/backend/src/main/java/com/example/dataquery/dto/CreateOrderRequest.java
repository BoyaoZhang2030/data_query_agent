package com.example.dataquery.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {
    private String orderNo;
    private Long userId;
    private String status;
    private String orderType;
    private String remark;
    private List<OrderProductRequest> items;
}
