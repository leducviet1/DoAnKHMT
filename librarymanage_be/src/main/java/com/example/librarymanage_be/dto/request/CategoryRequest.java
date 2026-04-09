package com.example.librarymanage_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryRequest {
    @NotBlank(message = "Không được để trống")
    private String categoryName;
}
