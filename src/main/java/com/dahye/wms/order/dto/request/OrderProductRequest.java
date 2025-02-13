package com.dahye.wms.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;


@Schema(description = "상품 주문 요청 모델")
@Getter
@Setter
public class OrderProductRequest {
    @Schema(description = "상품 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "REQUIRED_PRODUCT_ID")
    private long productId;

    @Schema(description = "주문 수량", requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(value = 1, message = "INVALID_QUANTITY")
    private int quantity;
}
