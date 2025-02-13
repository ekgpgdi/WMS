package com.dahye.wms.order.dto.response;

import com.dahye.wms.order.domain.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Schema(description = "거래 내역 응답")
@Getter
@Setter
@Builder
public class OrderResponse {

    @Schema(description = "거래 번호", example = "ORD-20250206113517-0A58E542")
    private String orderNumber;

    @Schema(description = "거래 상태", example = "SUCCESS")
    private OrderStatus orderStatus;

    @Schema(description = "거래 금액", example = "6000")
    private long totalAmount;

    @Schema(description = "거래 일시", example = "2025-02-06 02:35:17")
    private Instant createdAt;

    @Schema(description = "거래 상품들")
    private List<OrderProductResponse> orderProductList;
}
