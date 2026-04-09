package com.example.librarymanage_be.repo;

import com.example.librarymanage_be.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRepository extends JpaRepository<Author,Integer>, PagingAndSortingRepository<Author,Integer> {
}
