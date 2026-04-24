package com.skillsops.common.api.doc;

/** OpenAPI 注解用 JSON 示例常量（需为编译期常量）。 */
public final class OpenApiExamples {

    private OpenApiExamples() {
    }

    public static final String OK_EMPTY = "{\"success\":true,\"code\":\"OK\",\"message\":\"success\",\"data\":null}";
    public static final String OK_PAGED = "{\"success\":true,\"code\":\"OK\",\"message\":\"success\",\"data\":{\"page\":0,\"size\":10,\"total\":0,\"items\":[]}}";
    public static final String VALIDATION_FAILED = "{\"success\":false,\"code\":\"VALIDATION_FAILED\",\"message\":\"参数校验失败\",\"data\":null}";
    public static final String RESOURCE_NOT_FOUND = "{\"success\":false,\"code\":\"RESOURCE_NOT_FOUND\",\"message\":\"资源不存在\",\"data\":null}";
    public static final String PERMISSION_DENIED = "{\"success\":false,\"code\":\"PERMISSION_DENIED\",\"message\":\"权限不足\",\"data\":null}";
    public static final String OPERATION_FAILED = "{\"success\":false,\"code\":\"OPERATION_FAILED\",\"message\":\"接口壳层已就绪，业务实现将在后续任务补齐\",\"data\":null}";
}
