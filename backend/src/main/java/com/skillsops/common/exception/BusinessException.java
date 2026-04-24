package com.skillsops.common.exception;

import com.skillsops.common.api.error.ErrorCode;

/**
 * 携带标准错误码的业务异常，供 {@link com.skillsops.common.exception.GlobalExceptionHandler} 映射为统一响应。
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
