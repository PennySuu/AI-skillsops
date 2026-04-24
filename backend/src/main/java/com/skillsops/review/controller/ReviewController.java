package com.skillsops.review.controller;

import com.skillsops.common.api.doc.OpenApiExamples;
import com.skillsops.common.api.dto.ApiResponse;
import com.skillsops.review.dto.PendingReviewItem;
import com.skillsops.review.dto.RejectReviewRequest;
import com.skillsops.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/reviews")
@Validated
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/pending")
    @Operation(summary = "待审核列表")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.OK_PAGED))))
    public ResponseEntity<ApiResponse<List<PendingReviewItem>>> listPendingReviews(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.listPending(request)));
    }

    @PostMapping("/{reviewId}/approve")
    @Operation(summary = "审核通过")
    @ApiResponses(@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "操作成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.OK_EMPTY))))
    public ResponseEntity<ApiResponse<Void>> approve(
            @PathVariable @Positive Long reviewId,
            HttpServletRequest request) {
        reviewService.approve(reviewId, request);
        return ResponseEntity.ok(ApiResponse.okEmpty());
    }

    @PostMapping("/{reviewId}/reject")
    @Operation(summary = "审核拒绝")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "操作成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.OK_EMPTY))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "拒绝理由长度不合法", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.VALIDATION_FAILED)))} )
    public ResponseEntity<ApiResponse<Void>> reject(
            @PathVariable @Positive Long reviewId,
            @Valid @RequestBody RejectReviewRequest request,
            HttpServletRequest httpServletRequest) {
        reviewService.reject(reviewId, request.reason(), httpServletRequest);
        return ResponseEntity.ok(ApiResponse.okEmpty());
    }
}
