package com.example.librarymanage_be.repo;

import com.example.librarymanage_be.entity.Fine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface FineRepository extends JpaRepository<Fine, Integer>, PagingAndSortingRepository<Fine, Integer> {
}
