package com.skillsops.rating.controller;

import com.skillsops.common.api.doc.OpenApiExamples;
import com.skillsops.common.api.dto.ApiResponse;
import com.skillsops.rating.dto.UpsertRatingRequest;
import com.skillsops.rating.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/skills")
@Validated
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PutMapping("/{skillId}/ratings")
    @Operation(summary = "提交或更新评分")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "提交成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.OK_EMPTY))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "未安装不可评分", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"success\":false,\"code\":\"RATING_REQUIRES_INSTALL\",\"message\":\"请先安装后再评分\",\"data\":null}"))),
    })
    public ResponseEntity<ApiResponse<Void>> upsertRating(
            @PathVariable @Positive Long skillId,
            @Valid @RequestBody UpsertRatingRequest request,
            jakarta.servlet.http.HttpServletRequest httpServletRequest) {
        ratingService.upsertRating(skillId, request, httpServletRequest);
        return ResponseEntity.ok(ApiResponse.okEmpty());
    }
}
