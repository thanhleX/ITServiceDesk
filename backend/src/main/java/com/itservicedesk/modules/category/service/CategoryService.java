package com.itservicedesk.modules.category.service;

import com.itservicedesk.modules.category.dto.CategoryRequest;
import com.itservicedesk.modules.category.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getAllCategories();

    List<CategoryResponse> getActiveCategories();

    CategoryResponse getCategoryById(Long id);

    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse updateCategory(Long id, CategoryRequest request);

    void changeCategoryStatus(Long id, String status);
}
