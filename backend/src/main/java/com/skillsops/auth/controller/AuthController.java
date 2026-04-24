package com.skillsops.auth.controller;

import com.skillsops.auth.dto.AuthLoginRequest;
import com.skillsops.auth.dto.AuthRegisterRequest;
import com.skillsops.common.api.doc.OpenApiExamples;
import com.skillsops.common.api.dto.ApiResponse;
import com.skillsops.common.api.error.ErrorCode;
import com.skillsops.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@Validated
public class AuthController {

    @PostMapping("/register")
    @Operation(summary = "注册")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "注册成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.OK_EMPTY))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "参数校验失败", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.VALIDATION_FAILED))),
    })
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody AuthRegisterRequest request) {
        throw todo();
    }

    @PostMapping("/login")
    @Operation(summary = "登录")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "登录成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.OK_EMPTY))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "账号或密码错误", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"success\":false,\"code\":\"AUTH_INVALID_CREDENTIALS\",\"message\":\"账号或密码错误\",\"data\":null}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "参数校验失败", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.VALIDATION_FAILED))),
    })
    public ResponseEntity<ApiResponse<Void>> login(@Valid @RequestBody AuthLoginRequest request) {
        throw todo();
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "退出成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.OK_EMPTY))),
    })
    public ResponseEntity<ApiResponse<Void>> logout() {
        throw todo();
    }

    private BusinessException todo() {
        return new BusinessException(ErrorCode.OPERATION_FAILED, "接口壳层已就绪，业务实现将在后续任务补齐");
    }
}
