package com.example.librarymanage_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class LibrarymanageBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibrarymanageBeApplication.class, args);
    }

}
