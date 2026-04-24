package com.skillsops.category.controller;

import com.skillsops.category.dto.CreateCategoryRequest;
import com.skillsops.category.dto.PatchCategoryStatusRequest;
import com.skillsops.category.dto.UpdateCategoryRequest;
import com.skillsops.common.api.doc.OpenApiExamples;
import com.skillsops.common.api.dto.ApiResponse;
import com.skillsops.common.api.error.ErrorCode;
import com.skillsops.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/categories")
@Validated
public class AdminCategoryController {

    @GetMapping
    @Operation(summary = "分类列表")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.OK_PAGED)))
    public ResponseEntity<ApiResponse<Void>> listCategories() {
        throw todo();
    }

    @PostMapping
    @Operation(summary = "创建分类")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "创建成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.OK_EMPTY)))
    public ResponseEntity<ApiResponse<Void>> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        throw todo();
    }

    @PutMapping("/{categoryId}")
    @Operation(summary = "更新分类")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.OK_EMPTY)))
    public ResponseEntity<ApiResponse<Void>> updateCategory(
            @PathVariable @Positive Long categoryId,
            @Valid @RequestBody UpdateCategoryRequest request) {
        throw todo();
    }

    @PatchMapping("/{categoryId}")
    @Operation(summary = "启停分类")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "操作成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.OK_EMPTY)))
    public ResponseEntity<ApiResponse<Void>> patchCategoryStatus(
            @PathVariable @Positive Long categoryId,
            @Valid @RequestBody PatchCategoryStatusRequest request) {
        throw todo();
    }

    private BusinessException todo() {
        return new BusinessException(ErrorCode.OPERATION_FAILED, "接口壳层已就绪，业务实现将在后续任务补齐");
    }
}
