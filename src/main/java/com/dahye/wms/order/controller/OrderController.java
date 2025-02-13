package com.dahye.wms.order.controller;

import com.dahye.wms.common.domain.ResponseCode;
import com.dahye.wms.common.dto.response.ServerResponse;
import com.dahye.wms.order.dto.request.OrderRequest;
import com.dahye.wms.order.dto.response.OrderListResponse;
import com.dahye.wms.order.facade.OrderFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "ORDER")
@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderFacade orderFacade;

    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "단건 주문 처리",
            description = "사용자의 단건 주문 요청을 처리하고, 성공 시 주문 ID를 반환합니다."
    )
    @ApiResponse(
            responseCode = "201",
            description = "주문 성공, 주문 ID 반환",
            content = @Content(schema = @Schema(implementation = ResponseCode.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "상품 재고 부족",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @PostMapping("")
    public ServerResponse<Long> order(Authentication authentication,
                                      @RequestBody OrderRequest orderRequest,
                                      BindingResult bindingResult) {

        if (bindingResult != null && bindingResult.hasErrors()) {
            throw new IllegalArgumentException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        return ServerResponse.successResponse(orderFacade.order(authentication, orderRequest));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "엑셀로 여러 건 주문 처리",
            description = "사용자의 여러 건 주문 요청을 처리하고, 성공 시 주문 ID를 반환합니다."
    )
    @ApiResponse(
            responseCode = "201",
            description = "주문 성공, 주문 ID 반환",
            content = @Content(schema = @Schema(implementation = ResponseCode.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "상품 재고 부족",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "500",
            description = "엑셀 파일 파싱 에러",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @PostMapping("/excel")
    public ServerResponse<Long> orderByExcelFile(Authentication authentication,
                                                 @RequestParam("file") MultipartFile file) {
        return ServerResponse.successResponse(orderFacade.orderByExcelFile(authentication, file));
    }

    @Operation(
            summary = "주문 목록 조회",
            description = "사용자의 주문 목록을 페이지네이션 방식으로 조회합니다. 기본적으로 30개의 주문을 조회하며, 페이지 번호와 크기를 지정할 수 있습니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "주문 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = OrderListResponse.class))
    )
    @GetMapping("")
    public ServerResponse<OrderListResponse> getOrderList(@Parameter(description = "조회 page") @RequestParam(value = "page", defaultValue = "0")
                                                                  Integer page,
                                                          @Parameter(description = "조회 page size") @RequestParam(value = "size", defaultValue = "30")
                                                                  Integer size, Authentication authentication) {
        return ServerResponse.successResponse(orderFacade.getList(page, size, authentication));
    }
}
