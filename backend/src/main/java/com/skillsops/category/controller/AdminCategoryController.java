package com.skillsops.category.controller;

import com.skillsops.category.dto.CreateCategoryRequest;
import com.skillsops.category.dto.CategoryItemDTO;
import com.skillsops.category.dto.PatchCategoryStatusRequest;
import com.skillsops.category.dto.UpdateCategoryRequest;
import com.skillsops.category.service.CategoryService;
import com.skillsops.common.api.doc.OpenApiExamples;
import com.skillsops.common.api.dto.ApiResponse;
import com.skillsops.common.api.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/categories")
@Validated
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @Operation(summary = "分类列表")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.OK_PAGED)))
    public ResponseEntity<ApiResponse<PageResponse<CategoryItemDTO>>> listCategories(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            jakarta.servlet.http.HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.listCategories(page, size, request)));
    }

    @PostMapping
    @Operation(summary = "创建分类")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "创建成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.OK_EMPTY)))
    public ResponseEntity<ApiResponse<Void>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request,
            jakarta.servlet.http.HttpServletRequest httpServletRequest) {
        categoryService.createCategory(request, httpServletRequest);
        return ResponseEntity.ok(ApiResponse.okEmpty());
    }

    @PutMapping("/{categoryId}")
    @Operation(summary = "更新分类")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.OK_EMPTY)))
    public ResponseEntity<ApiResponse<Void>> updateCategory(
            @PathVariable @Positive Long categoryId,
            @Valid @RequestBody UpdateCategoryRequest request,
            jakarta.servlet.http.HttpServletRequest httpServletRequest) {
        categoryService.updateCategory(categoryId, request, httpServletRequest);
        return ResponseEntity.ok(ApiResponse.okEmpty());
    }

    @PatchMapping("/{categoryId}/status")
    @Operation(summary = "启停分类")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "操作成功", content = @Content(schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = OpenApiExamples.OK_EMPTY)))
    public ResponseEntity<ApiResponse<Void>> patchCategoryStatus(
            @PathVariable @Positive Long categoryId,
            @Valid @RequestBody PatchCategoryStatusRequest request,
            jakarta.servlet.http.HttpServletRequest httpServletRequest) {
        categoryService.patchCategoryStatus(categoryId, request, httpServletRequest);
        return ResponseEntity.ok(ApiResponse.okEmpty());
    }
}
