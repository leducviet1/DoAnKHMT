package com.example.librarymanage_be.mapper;

import com.example.librarymanage_be.dto.request.BookRequest;
import com.example.librarymanage_be.dto.response.BookResponse;
import com.example.librarymanage_be.Entity.Book;
import com.example.librarymanage_be.Entity.BookAuthor;
import org.mapstruct.*;

import java.util.List;
@Mapper(componentModel = "spring")
public interface BookMapper {
    @Mapping(target = "category",ignore = true)
    @Mapping(target = "publisher",ignore = true)
    @Mapping(target = "bookAuthors",ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "availableQuantity", ignore = true)
    Book toEntity(BookRequest bookRequest);

    @Mapping(target = "categoryName" , source = "category.categoryName")
    @Mapping(target = "publisherName" , source = "publisher.publisherName")
    @Mapping(target = "authorNames" , source = "bookAuthors",qualifiedByName = "mapAuthors")
    BookResponse toResponse(Book book);

    //custom List<Author> -> List<String>
    @Named("mapAuthors")
    default List<String> mapAuthors(List<BookAuthor> bookAuthors) {
        if (bookAuthors == null) return List.of();

        return bookAuthors.stream()
                .map(ba -> ba.getAuthor().getAuthorName())
                .toList();

    }
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateBook(@MappingTarget Book book,BookRequest bookRequest);
}
