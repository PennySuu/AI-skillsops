package com.skillsops.skill.controller;

import com.skillsops.common.api.doc.OpenApiExamples;
import com.skillsops.common.api.dto.ApiResponse;
import com.skillsops.common.api.error.ErrorCode;
import com.skillsops.common.exception.BusinessException;
import com.skillsops.skill.dto.CreateSkillRequest;
import com.skillsops.skill.dto.CreateSkillVersionRequest;
import com.skillsops.skill.dto.OfflineSkillRequest;
import com.skillsops.skill.dto.UpdateSkillRequest;
import com.skillsops.review.service.ReviewService;
import com.skillsops.skill.service.SkillLifecycleService;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/skills")
@Validated
public class SkillController {

    private final SkillLifecycleService skillLifecycleService;
    private final ReviewService reviewService;

    public SkillController(SkillLifecycleService skillLifecycleService, ReviewService reviewService) {
        this.skillLifecycleService = skillLifecycleService;
        this.reviewService = reviewService;
    }

    @PostMapping
    @Operation(summary = "创建 Skill 草稿")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "创建成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.OK_EMPTY))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "参数校验失败", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.VALIDATION_FAILED))),
    })
    public ResponseEntity<ApiResponse<Void>> createSkill(
            @Valid @RequestBody CreateSkillRequest request,
            HttpServletRequest httpServletRequest) {
        skillLifecycleService.createDraft(request, httpServletRequest);
        return ResponseEntity.ok(ApiResponse.okEmpty());
    }

    @PatchMapping("/{skillId}")
    @Operation(summary = "编辑 Skill")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "编辑成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.OK_EMPTY))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "参数校验失败", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.VALIDATION_FAILED))),
    })
    public ResponseEntity<ApiResponse<Void>> updateSkill(
            @PathVariable @Positive Long skillId,
            @Valid @RequestBody UpdateSkillRequest request,
            HttpServletRequest httpServletRequest) {
        skillLifecycleService.updateSkill(skillId, request, httpServletRequest);
        return ResponseEntity.ok(ApiResponse.okEmpty());
    }

    @PostMapping("/{skillId}/submit-review")
    @Operation(summary = "提交审核")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "提交成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.OK_EMPTY))),
    })
    public ResponseEntity<ApiResponse<Void>> submitReview(
            @PathVariable @Positive Long skillId,
            HttpServletRequest httpServletRequest) {
        skillLifecycleService.submitReview(skillId, httpServletRequest);
        return ResponseEntity.ok(ApiResponse.okEmpty());
    }

    @PostMapping("/{skillId}/versions")
    @Operation(summary = "发布新版本")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "发布成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.OK_EMPTY))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "版本冲突", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"success\":false,\"code\":\"SKILL_VERSION_CONFLICT\",\"message\":\"版本号冲突\",\"data\":null}"))),
    })
    public ResponseEntity<ApiResponse<Void>> createVersion(
            @PathVariable @Positive Long skillId,
            @Valid @RequestBody CreateSkillVersionRequest request,
            HttpServletRequest httpServletRequest) {
        skillLifecycleService.createVersion(
                skillId,
                request.version(),
                request.changelog(),
                request.resourceUrl(),
                httpServletRequest);
        return ResponseEntity.ok(ApiResponse.okEmpty());
    }

    @PostMapping("/{skillId}/install-command")
    @Operation(summary = "生成安装命令（短 TTL + 幂等）")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "签发成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"success\":true,\"code\":\"OK\",\"message\":\"success\",\"data\":{\"command\":\"npx skills add <signed-url>\"}}"))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "资源状态冲突", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"success\":false,\"code\":\"SKILL_OFFLINE_NOT_INSTALLABLE\",\"message\":\"该 Skill 已下架，暂不可安装\",\"data\":null}"))),
    })
    public ResponseEntity<ApiResponse<Void>> issueInstallCommand(
            @PathVariable @Positive Long skillId,
            @Parameter(description = "幂等键", required = true) @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey) {
        throw todo();
    }

    @PostMapping("/{skillId}/offline")
    @Operation(summary = "管理员下架 Skill")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "下架成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.OK_EMPTY))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "原因校验失败", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.VALIDATION_FAILED))),
    })
    public ResponseEntity<ApiResponse<Void>> offlineSkill(
            @PathVariable @Positive Long skillId,
            @Valid @RequestBody OfflineSkillRequest request,
            HttpServletRequest httpServletRequest) {
        reviewService.offlineSkill(skillId, request.reason(), httpServletRequest);
        return ResponseEntity.ok(ApiResponse.okEmpty());
    }

    private BusinessException todo() {
        return new BusinessException(ErrorCode.OPERATION_FAILED, "接口壳层已就绪，业务实现将在后续任务补齐");
    }
}
