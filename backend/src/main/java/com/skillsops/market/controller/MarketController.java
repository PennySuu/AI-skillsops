package com.skillsops.market.controller;

import com.skillsops.common.api.doc.OpenApiExamples;
import com.skillsops.common.api.dto.ApiResponse;
import com.skillsops.common.api.dto.PageResponse;
import com.skillsops.market.dto.MarketSkillDetailDTO;
import com.skillsops.market.dto.MarketSkillSummaryDTO;
import com.skillsops.market.service.MarketQueryService;
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

    private final MarketQueryService marketQueryService;

    public MarketController(MarketQueryService marketQueryService) {
        this.marketQueryService = marketQueryService;
    }

    @GetMapping("/skills")
    @Operation(summary = "市场列表（仅 published）")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.OK_PAGED))))
    public ResponseEntity<ApiResponse<PageResponse<MarketSkillSummaryDTO>>> listSkills(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) @Pattern(regexp = "^(updatedAt|name),(asc|desc)$") String sort) {
        return ResponseEntity.ok(ApiResponse.ok(marketQueryService.listPublished(page, size, category, q, sort)));
    }

    @GetMapping("/skills/{skillId}")
    @Operation(summary = "市场详情")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.OK_EMPTY))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "资源不存在", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.RESOURCE_NOT_FOUND))),
    })
    public ResponseEntity<ApiResponse<MarketSkillDetailDTO>> getSkillDetail(@PathVariable @Positive Long skillId) {
        return ResponseEntity.ok(ApiResponse.ok(marketQueryService.getPublishedSkillDetail(skillId)));
    }
}
