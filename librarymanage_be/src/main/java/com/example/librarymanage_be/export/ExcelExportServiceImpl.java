package com.example.librarymanage_be.export;

import com.example.librarymanage_be.dto.response.BookResponse;
import com.example.librarymanage_be.dto.response.ExcelFileResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ExcelExportServiceImpl implements ExcelExportService {
    @Override
    public ExcelFileResponse exportBooks(Page<BookResponse> books) throws IOException {
        String time = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        String fileName = "book_" + time + ".xlsx";
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Books");

            Row rowHeader = sheet.createRow(0);
            rowHeader.createCell(0).setCellValue("ID");
            rowHeader.createCell(1).setCellValue("Title");
            rowHeader.createCell(2).setCellValue("Author");
            rowHeader.createCell(3).setCellValue("Price");
            rowHeader.createCell(4).setCellValue("Category");
            rowHeader.createCell(5).setCellValue("AvailableQuantity");
            rowHeader.createCell(6).setCellValue("Publisher");
            rowHeader.createCell(7).setCellValue("Status");

            int rowIndex = 1;
            for (BookResponse book : books) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(book.getBookId());
                row.createCell(1).setCellValue(book.getTitle());
                row.createCell(2).setCellValue(String.join(", ", book.getAuthorNames()));
                row.createCell(3).setCellValue(String.valueOf(book.getPrice()));
                row.createCell(4).setCellValue(book.getCategoryName());
                row.createCell(5).setCellValue(String.valueOf(book.getAvailableQuantity()));
                row.createCell(6).setCellValue(book.getPublisherName());
                row.createCell(7).setCellValue(book.getBookStatus().toString());
            }

            workbook.write(outputStream);
            workbook.dispose();
            return new ExcelFileResponse(fileName, outputStream.toByteArray());
        }
    }
}
