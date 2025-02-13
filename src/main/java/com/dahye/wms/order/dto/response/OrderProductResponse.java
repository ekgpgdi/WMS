package com.dahye.wms.order.dto.response;

import com.dahye.wms.product.dto.response.ProductSummaryResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "거래 상품 응답")
@Getter
@Setter
@Builder
public class OrderProductResponse {

    @Schema(description = "거래 상품 정보")
    private ProductSummaryResponse product;

    @Schema(description = "거래 상품 수량")
    private int quantity;

    @Schema(description = "거래 금액")
    private long price;
}
