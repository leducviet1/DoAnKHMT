package com.example.librarymanage_be.service;

import com.example.librarymanage_be.dto.request.CategoryRequest;
import com.example.librarymanage_be.dto.response.CategoryResponse;
import com.example.librarymanage_be.mapper.CategoryMapper;
import com.example.librarymanage_be.Entity.Category;
import com.example.librarymanage_be.repo.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponse create(CategoryRequest categoryRequest) {
        log.info("[CATEGORY] Creating a new category with name={}", categoryRequest.getCategoryName());
        Category categoryMap = categoryMapper.toEntity(categoryRequest);
        Category categorySaved = categoryRepository.save(categoryMap);
        log.info("[CATEGORY] Created successfully with name={}", categorySaved.getCategoryName());
        return categoryMapper.toResponse(categorySaved);
    }

    @Override
    public Category findById(Integer categoryId) {
        log.info("[CATEGORY] Finding a category with id={}", categoryId);
        return categoryRepository.findById(categoryId).orElseThrow(() -> {
            log.error("[CATEGORY] Category not found w  ith id={}", categoryId);
            return new RuntimeException("Category not found");
        });
    }

    @Override
    public Page<CategoryResponse> getCategories(Pageable pageable) {
        log.info("[CATEGORY] Getting categories - page={},size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Category> categories = categoryRepository.findAll(pageable);
        log.info("[CATEGORY] Found {} categories", categories.getTotalElements());
        return categories.map(categoryMapper::toResponse);
    }

    @Override
    public void deleteById(Integer categoryId) {
        log.info("[CATEGORY] Deleting category with id={}", categoryId);
        Category categoryExist = findById(categoryId);
        categoryRepository.delete(categoryExist);
        log.info("[CATEGORY] Deleted successfully with id={}", categoryId);
    }

    @Override
    public CategoryResponse update(Integer categoryId, CategoryRequest category) {
        log.info("[CATEGORY] Updating category with id={}", categoryId);
        Category categoryExist = findById(categoryId);
        categoryExist.setCategoryName(category.getCategoryName());
        Category categoryUpdated = categoryRepository.save(categoryExist);
        log.info("[CATEGORY] Updated successfully with id={}", categoryUpdated.getCategoryId());
        return categoryMapper.toResponse(categoryUpdated);
    }
}
