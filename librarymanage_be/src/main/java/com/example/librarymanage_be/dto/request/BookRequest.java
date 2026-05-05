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
    @NotBlank(message = "Khong duoc de trong ten sach")
    @Size(max = 255, message = "Khong duoc qua 255 ky tu")
    private String title;

    @NotNull(message = "Phai co tong so luong sach")
    private Integer totalQuantity;

    private BigDecimal price;
    private Integer availableQuantity;
    private String description;
    private List<Integer> authorIds;
    private List<String> newAuthorNames;
    private Integer categoryId;
    private Integer publisherId;
}
