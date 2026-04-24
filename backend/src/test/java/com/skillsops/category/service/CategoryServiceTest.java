package com.skillsops.category.service;

import com.skillsops.category.dto.CategoryItemDTO;
import com.skillsops.category.dto.CreateCategoryRequest;
import com.skillsops.common.api.error.ErrorCode;
import com.skillsops.common.exception.BusinessException;
import com.skillsops.category.mapper.CategoryMapper;
import com.skillsops.skill.service.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void shouldRejectDuplicateCategoryName() {
        when(categoryMapper.findByName("Java")).thenReturn(new CategoryItemDTO(1L, "Java", true));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> categoryService.createCategory(new CreateCategoryRequest("Java", true), request));
        assertEquals(ErrorCode.OPERATION_FAILED, ex.getErrorCode());
        verify(categoryMapper, never()).insert("Java", true);
    }

    @Test
    void shouldCreateCategoryWhenNameAvailable() {
        when(categoryMapper.findByName("Backend")).thenReturn(null);

        categoryService.createCategory(new CreateCategoryRequest("Backend", true), request);

        verify(categoryMapper).insert("Backend", true);
    }
}
