package com.dahye.wms.common.domain;

public enum ResponseCode {
    // success
    SUCCESS(false),

    // auth
    UNAUTHORIZED(true),
    FORBIDDEN(true),

    // validation
    REQUIRED_EMAIL(true),
    REQUIRED_PASSWORD(true),
    REQUIRED_NICKNAME(true),
    EMAIL_FORMAT_ERROR(true),
    CHECK_CERT_CODE(false),
    VERIFIED_CODE_ERROR(false),
    REQUIRED_EMAIL_VERIFIED(true),
    REQUIRED_PRODUCT_ID(true),
    INVALID_PASSWORD(false),
    PRODUCT_OUT_OF_STOCK(false),
    INVALID_QUANTITY(true),
    INVALID_PRODUCT_ID(true),

    // not found
    NOT_FOUND_CUSTOMER(false),
    NOT_FOUND_PRODUCT(true),

    // exist
    EXIST_EMAIL(false),

    // sever error
    SERVER_ERROR(true);

    public final boolean isFatality;

    ResponseCode(boolean isFatality) {
        this.isFatality = isFatality;
    }
}
