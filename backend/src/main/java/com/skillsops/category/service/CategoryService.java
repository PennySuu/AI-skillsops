package com.skillsops.category.service;

import com.skillsops.category.dto.CategoryItemDTO;
import com.skillsops.category.dto.CreateCategoryRequest;
import com.skillsops.category.dto.PatchCategoryStatusRequest;
import com.skillsops.category.dto.UpdateCategoryRequest;
import com.skillsops.category.mapper.CategoryMapper;
import com.skillsops.common.api.dto.PageResponse;
import com.skillsops.common.api.error.ErrorCode;
import com.skillsops.common.exception.BusinessException;
import com.skillsops.skill.service.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final CurrentUserService currentUserService;

    public CategoryService(CategoryMapper categoryMapper, CurrentUserService currentUserService) {
        this.categoryMapper = categoryMapper;
        this.currentUserService = currentUserService;
    }

    public PageResponse<CategoryItemDTO> listCategories(int page, int size, HttpServletRequest request) {
        currentUserService.requireAdmin(request);
        int offset = page * size;
        return new PageResponse<>(
                page,
                size,
                categoryMapper.countAll(),
                categoryMapper.list(offset, size));
    }

    @Transactional
    public void createCategory(CreateCategoryRequest request, HttpServletRequest httpServletRequest) {
        currentUserService.requireAdmin(httpServletRequest);
        ensureNameAvailable(request.name(), null);
        categoryMapper.insert(request.name().trim(), request.enabled());
    }

    @Transactional
    public void updateCategory(Long categoryId, UpdateCategoryRequest request, HttpServletRequest httpServletRequest) {
        currentUserService.requireAdmin(httpServletRequest);
        requireCategory(categoryId);
        ensureNameAvailable(request.name(), categoryId);
        categoryMapper.updateName(categoryId, request.name().trim());
    }

    @Transactional
    public void patchCategoryStatus(Long categoryId, PatchCategoryStatusRequest request, HttpServletRequest httpServletRequest) {
        currentUserService.requireAdmin(httpServletRequest);
        requireCategory(categoryId);
        categoryMapper.updateStatus(categoryId, request.enabled());
    }

    private void requireCategory(Long categoryId) {
        if (categoryMapper.findById(categoryId) == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "分类不存在");
        }
    }

    private void ensureNameAvailable(String rawName, Long selfId) {
        String normalized = rawName.trim();
        CategoryItemDTO existed = categoryMapper.findByName(normalized);
        if (existed != null && !existed.id().equals(selfId)) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "分类名称已存在");
        }
    }
}
