package com.skillsops.auth.controller;

import com.skillsops.auth.dto.AuthLoginRequest;
import com.skillsops.auth.dto.AuthProfileResponse;
import com.skillsops.auth.dto.AuthRegisterRequest;
import com.skillsops.auth.security.CsrfTokenService;
import com.skillsops.auth.service.AuthService;
import com.skillsops.common.api.doc.OpenApiExamples;
import com.skillsops.common.api.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@Validated
public class AuthController {

    private final AuthService authService;
    private final CsrfTokenService csrfTokenService;

    public AuthController(AuthService authService, CsrfTokenService csrfTokenService) {
        this.authService = authService;
        this.csrfTokenService = csrfTokenService;
    }

    @GetMapping("/csrf-token")
    @Operation(summary = "获取 CSRF Token（双提交 Cookie）")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"success\":true,\"code\":\"OK\",\"message\":\"success\",\"data\":{\"csrfToken\":\"xxxx\"}}"))),
    })
    public ResponseEntity<ApiResponse<String>> issueCsrfToken(HttpServletResponse response) {
        String token = csrfTokenService.issueToken(response);
        return ResponseEntity.ok(ApiResponse.ok(token));
    }

    @PostMapping("/register")
    @Operation(summary = "注册")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "注册成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"success\":true,\"code\":\"OK\",\"message\":\"success\",\"data\":{\"userId\":1,\"username\":\"demo\",\"role\":\"USER\",\"expiresInSeconds\":1800}}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "参数校验失败", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.VALIDATION_FAILED))),
    })
    public ResponseEntity<ApiResponse<AuthProfileResponse>> register(
            @Valid @RequestBody AuthRegisterRequest request,
            HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(ApiResponse.ok(authService.register(request, httpServletRequest)));
    }

    @PostMapping("/login")
    @Operation(summary = "登录")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "登录成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.OK_EMPTY))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "账号或密码错误", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"success\":false,\"code\":\"AUTH_INVALID_CREDENTIALS\",\"message\":\"账号或密码错误\",\"data\":null}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "参数校验失败", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.VALIDATION_FAILED))),
    })
    public ResponseEntity<ApiResponse<AuthProfileResponse>> login(
            @Valid @RequestBody AuthLoginRequest request,
            HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request, httpServletRequest)));
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "退出成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.OK_EMPTY))),
    })
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        authService.logout(request);
        ResponseCookie clearSessionCookie = ResponseCookie.from("SKILLSOPS_SESSION", "")
                .path("/")
                .httpOnly(true)
                .maxAge(0)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearSessionCookie.toString())
                .body(ApiResponse.okEmpty());
    }
}
