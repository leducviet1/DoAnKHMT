package com.example.librarymanage_be.controller;

import com.example.librarymanage_be.dto.request.CategoryRequest;
import com.example.librarymanage_be.dto.response.CategoryResponse;
import com.example.librarymanage_be.Entity.Category;
import com.example.librarymanage_be.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public Page<CategoryResponse> getCategories(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return categoryService.getCategories(pageable);
    }

    @GetMapping("{id}")
    public Category findById(@PathVariable Integer id) {
        return categoryService.findById(id);
    }

    @PostMapping("/create")
    public CategoryResponse create(@RequestBody CategoryRequest categoryRequest) {
        return categoryService.create(categoryRequest);
    }

    @PutMapping("/update/{id}")
    public CategoryResponse update(@PathVariable int id, @RequestBody CategoryRequest categoryRequest) {
        return categoryService.update(id, categoryRequest);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable int id) {
        categoryService.deleteById(id);
    }

}
