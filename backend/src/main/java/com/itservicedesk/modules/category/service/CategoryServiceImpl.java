package com.itservicedesk.modules.category.service;

import com.itservicedesk.common.exception.ConflictException;
import com.itservicedesk.common.exception.ResourceNotFoundException;
import com.itservicedesk.modules.category.dto.CategoryRequest;
import com.itservicedesk.modules.category.dto.CategoryResponse;
import com.itservicedesk.modules.category.entity.Category;
import com.itservicedesk.modules.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findAll().stream()
                .filter(c -> "ACTIVE".equalsIgnoreCase(c.getStatus()))
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return CategoryResponse.fromEntity(category);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        String code = request.getCode().trim().toUpperCase();
        if (categoryRepository.existsByCode(code)) {
            throw new ConflictException("Mã danh mục đã tồn tại: " + code);
        }

        Category category = Category.builder()
                .code(code)
                .name(request.getName().trim())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus().toUpperCase() : "ACTIVE")
                .build();

        return CategoryResponse.fromEntity(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        String newCode = request.getCode().trim().toUpperCase();
        if (!category.getCode().equalsIgnoreCase(newCode) && categoryRepository.existsByCode(newCode)) {
            throw new ConflictException("Mã danh mục đã tồn tại: " + newCode);
        }

        category.setCode(newCode);
        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            category.setStatus(request.getStatus().toUpperCase());
        }

        return CategoryResponse.fromEntity(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void changeCategoryStatus(Long id, String status) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        category.setStatus(status.toUpperCase());
        categoryRepository.save(category);
    }
}
