package com.example.dataquery.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderProductRequest {
    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;
}
