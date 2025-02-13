package com.dahye.wms.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Schema(description = "주문 요청 모델")
@Getter
@Setter
public class OrderRequest {

    @Schema(description = "우편번호", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "REQUIRED_POSTCODE")
    private String postcode;

    @Schema(description = "주소", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "REQUIRED_ADDRESS")
    private String address;

    @NotNull(message = "REQUIRED_ORDER_PRODUCT")
    private List<OrderProductRequest> orderProductList;
}
