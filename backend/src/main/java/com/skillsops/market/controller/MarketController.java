package com.skillsops.market.controller;

import com.skillsops.common.api.doc.OpenApiExamples;
import com.skillsops.common.api.dto.ApiResponse;
import com.skillsops.common.api.error.ErrorCode;
import com.skillsops.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/market")
@Validated
public class MarketController {

    @GetMapping("/skills")
    @Operation(summary = "市场列表（仅 published）")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.OK_PAGED))))
    public ResponseEntity<ApiResponse<Void>> listSkills(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) @Pattern(regexp = "^[A-Za-z0-9_]+,(asc|desc)$") String sort) {
        throw todo();
    }

    @GetMapping("/skills/{skillId}")
    @Operation(summary = "市场详情")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.OK_EMPTY))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "资源不存在", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.RESOURCE_NOT_FOUND))),
    })
    public ResponseEntity<ApiResponse<Void>> getSkillDetail(@PathVariable @Positive Long skillId) {
        throw todo();
    }

    private BusinessException todo() {
        return new BusinessException(ErrorCode.OPERATION_FAILED, "接口壳层已就绪，业务实现将在后续任务补齐");
    }
}
