package com.example.librarymanage_be.repo;

import com.example.librarymanage_be.entity.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher,  Integer>, PagingAndSortingRepository<Publisher, Integer> {

}
