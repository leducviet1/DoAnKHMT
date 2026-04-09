package com.example.librarymanage_be.mapper;

import com.example.librarymanage_be.dto.request.AuthorRequest;
import com.example.librarymanage_be.dto.response.AuthorResponse;
import com.example.librarymanage_be.entity.Author;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AuthorMapper {
   //Request -> Entity
    @Mapping(source = "authorName", target = "authorName")
    @Mapping(source = "description", target = "description")
    Author toEntity(AuthorRequest authorRequest);

    //Entity-> Response
    @Mapping(source = "authorId", target = "authorId")
    @Mapping(source = "authorName", target = "authorName")
    @Mapping(source = "description", target = "description")
    AuthorResponse toResponse(Author author);

    @Mapping(source = "authorName", target = "authorName")
    @Mapping(source = "description", target = "description")
    void updateEntity(AuthorRequest authorRequest, @MappingTarget Author author);
}
