package com.example.librarymanage_be.mapper;

import com.example.librarymanage_be.dto.request.CategoryRequest;
import com.example.librarymanage_be.dto.response.CategoryResponse;
import com.example.librarymanage_be.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(source = "categoryName", target = "categoryName")
    Category toEntity(CategoryRequest categoryRequest);

    @Mapping(source = "categoryId",target = "categoryId")
    @Mapping(source = "categoryName",target = "categoryName")
    CategoryResponse toResponse(Category category);
}
