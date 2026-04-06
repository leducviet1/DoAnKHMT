package com.example.librarymanage_be.service;

import com.example.librarymanage_be.dto.request.CategoryRequest;
import com.example.librarymanage_be.dto.response.CategoryResponse;
import com.example.librarymanage_be.mapper.CategoryMapper;
import com.example.librarymanage_be.model.Category;
import com.example.librarymanage_be.repo.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    @Override
    public CategoryResponse create(CategoryRequest categoryRequest) {
        Category categoryMap = categoryMapper.toEntity(categoryRequest);
        Category categorySaved = categoryRepository.save(categoryMap);
        return categoryMapper.toResponse(categorySaved);
    }

    @Override
    public Category findById(Integer categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(()->new RuntimeException("Category not found"));
    }

    @Override
    public Page<CategoryResponse> getCategories(Pageable pageable) {
        Page<Category> categories = categoryRepository.findAll(pageable);
        return categories.map(categoryMapper::toResponse);
    }

    @Override
    public void deleteById(Integer categoryId) {
        Category categoryExist =  findById(categoryId);
        categoryRepository.delete(categoryExist);
    }

    @Override
    public CategoryResponse update(Integer categoryId, CategoryRequest category) {
        Category categoryExist =  findById(categoryId);
        categoryExist.setCategoryName(category.getCategoryName());
        Category categoryUpdated = categoryRepository.save(categoryExist);
        return categoryMapper.toResponse(categoryUpdated);
    }
}
