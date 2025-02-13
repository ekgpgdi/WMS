package com.dahye.wms.order.dto.response;

import com.dahye.wms.common.dto.response.AbstractPageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Schema(description = "거래 리스트 응답")
@Getter
@Setter
@SuperBuilder
public class OrderListResponse extends AbstractPageResponse {

    @Schema(description = "거래 정보 응답")
    private List<OrderResponse> orderList;
}
