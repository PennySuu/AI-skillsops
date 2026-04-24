package com.skillsops.common.api.dto;

import com.skillsops.common.api.error.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 统一 API 响应 envelope，与 {@code openspec/config.yaml} 约定一致：
 * {@code success}、{@code code}、{@code message}、{@code data}。
 *
 * @param <T> 业务数据类型
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, String code, String message, T data) {

    /**
     * 成功响应，业务数据置于 {@code data}。
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, ErrorCode.OK.getCode(), "success", data);
    }

    /**
     * 成功且无业务体（例如 204 场景仍可用 200 + 空 data 表示，由调用方选择 HTTP 状态）。
     */
    public static ApiResponse<Void> okEmpty() {
        return new ApiResponse<>(true, ErrorCode.OK.getCode(), "success", null);
    }

    /**
     * 失败响应，{@code data} 为空，{@code code} 为错误码。
     */
    public static <T> ApiResponse<T> fail(ErrorCode errorCode, String message) {
        return new ApiResponse<>(false, errorCode.getCode(), message, null);
    }
}
