package com.dahye.wms.product.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "상품 공통 응답 모델")
public class ProductSummaryResponse {

    @Schema(description = "상품 ID", example = "0")
    private Long id;

    @Schema(description = "상품명", example = "A상품")
    private String name;

    @Schema(description = "상품 가격", example = "3000")
    private Long price;
}
