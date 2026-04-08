package com.example.librarymanage_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
public class CategoryRequest {
    @NotBlank(message = "Không được để trống")
    private String categoryName;
}
