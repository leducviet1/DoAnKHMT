package com.example.librarymanage_be.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorResponse {
    private Integer authorId;
    private String authorName;
    private String description;

}
