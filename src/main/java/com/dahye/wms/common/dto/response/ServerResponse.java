package com.dahye.wms.common.dto.response;

import com.dahye.wms.common.domain.ResponseCode;
import lombok.Getter;

@Getter
public class ServerResponse<T> {

    private final ResponseCode code;
    private final T content;

    private ServerResponse(ResponseCode code) {
        this.code = code;
        this.content = null;
    }

    private ServerResponse(ResponseCode code, final T content) {
        this.code = code;
        this.content = content;
    }

    public static <T> ServerResponse<T> successResponse(final T content) {
        return new ServerResponse<>(ResponseCode.SUCCESS, content);
    }

    public static <T> ServerResponse<T> errorResponse(ResponseCode code) {
        return new ServerResponse<>(code);
    }
}
