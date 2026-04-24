package com.skillsops.ops.controller;

import com.skillsops.common.api.doc.OpenApiExamples;
import com.skillsops.common.api.dto.ApiResponse;
import com.skillsops.ops.dto.Granularity;
import com.skillsops.ops.dto.OpsDashboardDTO;
import com.skillsops.ops.service.OpsDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/ops")
@Validated
public class AdminOpsController {

    private final OpsDashboardService opsDashboardService;

    public AdminOpsController(OpsDashboardService opsDashboardService) {
        this.opsDashboardService = opsDashboardService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "运营看板")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.OK_EMPTY)))
    public ResponseEntity<ApiResponse<OpsDashboardDTO>> dashboard(
            @RequestParam(defaultValue = "day") Granularity granularity,
            @RequestParam(defaultValue = "7") @Min(1) @Max(365) int days,
            jakarta.servlet.http.HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(opsDashboardService.dashboard(granularity, days, request)));
    }
}
