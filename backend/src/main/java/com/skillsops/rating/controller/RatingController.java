package com.skillsops.rating.controller;

import com.skillsops.common.api.doc.OpenApiExamples;
import com.skillsops.common.api.dto.ApiResponse;
import com.skillsops.common.api.error.ErrorCode;
import com.skillsops.common.exception.BusinessException;
import com.skillsops.rating.dto.UpsertRatingRequest;
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

    @PutMapping("/{skillId}/ratings")
    @Operation(summary = "提交或更新评分")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "提交成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.OK_EMPTY))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "未安装不可评分", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\"success\":false,\"code\":\"RATING_REQUIRES_INSTALL\",\"message\":\"请先安装后再评分\",\"data\":null}"))),
    })
    public ResponseEntity<ApiResponse<Void>> upsertRating(
            @PathVariable @Positive Long skillId,
            @Valid @RequestBody UpsertRatingRequest request) {
        throw todo();
    }

    private BusinessException todo() {
        return new BusinessException(ErrorCode.OPERATION_FAILED, "接口壳层已就绪，业务实现将在后续任务补齐");
    }
}
