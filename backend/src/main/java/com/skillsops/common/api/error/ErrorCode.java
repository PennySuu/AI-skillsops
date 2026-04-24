package com.skillsops.common.api.error;

import org.springframework.http.HttpStatus;

/**
 * 业务错误码，命名对齐 {@code openspec/config.yaml}（MODULE_ERROR_TYPE）。
 */
public enum ErrorCode {

    OK("OK", HttpStatus.OK),

    VALIDATION_FAILED("VALIDATION_FAILED", HttpStatus.UNPROCESSABLE_ENTITY),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND),
    PERMISSION_DENIED("PERMISSION_DENIED", HttpStatus.FORBIDDEN),
    OPERATION_FAILED("OPERATION_FAILED", HttpStatus.BAD_REQUEST),
    RATE_LIMIT_EXCEEDED("RATE_LIMIT_EXCEEDED", HttpStatus.TOO_MANY_REQUESTS),

    AUTH_INVALID_CREDENTIALS("AUTH_INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED),
    AUTH_TOKEN_EXPIRED("AUTH_TOKEN_EXPIRED", HttpStatus.UNAUTHORIZED),
    AUTH_CSRF_INVALID("AUTH_CSRF_INVALID", HttpStatus.FORBIDDEN),

    USER_NOT_FOUND("USER_NOT_FOUND", HttpStatus.NOT_FOUND),

    SYSTEM_INTERNAL_ERROR("SYSTEM_INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final HttpStatus httpStatus;

    ErrorCode(String code, HttpStatus httpStatus) {
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
