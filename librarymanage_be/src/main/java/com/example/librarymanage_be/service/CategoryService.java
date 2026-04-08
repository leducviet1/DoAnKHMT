package com.example.librarymanage_be.service;

import com.example.librarymanage_be.dto.request.CategoryRequest;
import com.example.librarymanage_be.dto.response.CategoryResponse;
import com.example.librarymanage_be.Entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    CategoryResponse create(CategoryRequest categoryRequest);
    Category findById(Integer categoryId);
    Page<CategoryResponse> getCategories(Pageable pageable);
    void deleteById(Integer categoryId);
    CategoryResponse update(Integer categoryId,CategoryRequest categoryRequest);
}
