package com.example.librarymanage_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookRequest {
    @NotBlank(message = "Không được để trống tên sách")
    @Size(max = 255,message = "Không được quá 255 kí tự")
    private String title;
    @NotNull(message = "Phải có tổng số lượng sách")
    private Integer totalQuantity;

    private BigDecimal price;

    private Integer availableQuantity;

    private String description;

    private List<Integer> authorIds;

    private Integer categoryId;

    private Integer publisherId;
}
