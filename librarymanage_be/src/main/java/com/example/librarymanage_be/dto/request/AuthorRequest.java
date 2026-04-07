package com.example.librarymanage_be.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AuthorRequest {
    @NotBlank(message = "Không được để trống tên tác giả")
    private String authorName;
    private String description;
}
