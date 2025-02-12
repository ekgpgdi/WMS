package com.dahye.wms.common.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Schema(description = "페이지 응답 모델")
@Getter
@Setter
@SuperBuilder
public class AbstractPageResponse {

    @Schema(description = "총 페이지 수")
    private int totalPages;

    @Schema(description = "마지막 페이지 여부")
    private Boolean isLast;

    @Schema(description = "총 개수")
    private long totalElements;
}
