package com.example.librarymanage_be.repo;

import com.example.librarymanage_be.entity.Book;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Integer>, PagingAndSortingRepository<Book, Integer>,
        JpaSpecificationExecutor<Book> {
    @Query("select coalesce(sum(b.totalQuantity), 0) from Book b")
    Long sumTotalQuantity();

    @Query("select coalesce(sum(b.availableQuantity), 0) from Book b")
    Long sumAvailableQuantity();

    @EntityGraph(attributePaths = {"category", "bookAuthors", "bookAuthors.author"})
    @Query("""
            select distinct b
            from Book b
            left join b.category c
            left join b.bookAuthors ba
            left join ba.author a
            where lower(b.title) like concat('%', :keyword, '%')
               or lower(c.categoryName) like concat('%', :keyword, '%')
               or lower(a.authorName) like concat('%', :keyword, '%')
            order by b.title asc
            """)
    List<Book> findSuggestions(@Param("keyword") String keyword, Pageable pageable);
}
