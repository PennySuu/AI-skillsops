package com.skillsops.user.controller;

import com.skillsops.common.api.doc.OpenApiExamples;
import com.skillsops.common.api.dto.ApiResponse;
import com.skillsops.common.api.error.ErrorCode;
import com.skillsops.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/users/me")
@Validated
public class UserInstallController {

    @GetMapping("/installs")
    @Operation(summary = "我的安装列表")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.OK_PAGED)))
    public ResponseEntity<ApiResponse<Void>> listMyInstalls(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        throw todo();
    }

    private BusinessException todo() {
        return new BusinessException(ErrorCode.OPERATION_FAILED, "接口壳层已就绪，业务实现将在后续任务补齐");
    }
}
