package com.example.librarymanage_be.export;

import com.example.librarymanage_be.dto.response.BookResponse;
import com.example.librarymanage_be.entity.Book;
import org.springframework.data.domain.Page;

import java.io.FileNotFoundException;
import java.io.IOException;

public interface ExcelExportService {
    void exportBooks(Page<BookResponse> books) throws IOException;
}
