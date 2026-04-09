package com.example.librarymanage_be.repo;

import com.example.librarymanage_be.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Integer>, PagingAndSortingRepository<Category,Integer> {
}
