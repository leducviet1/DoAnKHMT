package com.example.librarymanage_be.specification;

import com.example.librarymanage_be.entity.Author;
import com.example.librarymanage_be.entity.Book;
import com.example.librarymanage_be.entity.BookAuthor;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
public class BookSpecification {
    public static Specification<Book> hasTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
        // LIKE '%title%'
    }
    public static Specification<Book> hasCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }
        return((root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("category").get("categoryName")), "%" + category.toLowerCase() + "%"));
    }
    public static Specification<Book> hasAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }
        return ((root, query, criteriaBuilder) ->{
            query.distinct(true);
            Join<Book, BookAuthor> bookAuthorJoin = root.join("bookAuthors");
            Join<BookAuthor, Author> authorJoin = bookAuthorJoin.join("author");

            return criteriaBuilder.like(criteriaBuilder.lower(authorJoin.get("authorName")), "%" + author.toLowerCase() + "%");
        });
    }
}
