package com.example.librarymanage_be.service.Impl;

import com.example.librarymanage_be.entity.Borrow;
import com.example.librarymanage_be.entity.BorrowDetail;
import com.example.librarymanage_be.service.BorrowContractService;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class BorrowContractServiceImpl implements BorrowContractService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final List<String> DEFAULT_FONT_PATHS = List.of(
            "C:/Windows/Fonts/arial.ttf",
            "C:/Windows/Fonts/segoeui.ttf",
            "C:/Windows/Fonts/tahoma.ttf",
            "C:/Windows/Fonts/times.ttf"
    );

    private final Path storageDir;
    private final String configuredFontPath;

    public BorrowContractServiceImpl(
            @Value("${app.borrow-contract.storage-dir:contracts}") String storageDir,
            @Value("${app.borrow-contract.font-path:}") String configuredFontPath) throws IOException {
        this.storageDir = Paths.get(storageDir).toAbsolutePath().normalize();
        this.configuredFontPath = configuredFontPath == null ? "" : configuredFontPath.trim();
        Files.createDirectories(this.storageDir);
    }

    @Override
    public void generateContract(Borrow borrow, List<BorrowDetail> details) throws IOException {
        Path filePath = storageDir.resolve(getContractFileName(borrow.getBorrowId()));
        try (OutputStream outputStream = Files.newOutputStream(filePath)) {
            writePdf(outputStream, borrow, details);
        }
    }

    @Override
    public byte[] getContractContent(Integer borrowId) throws IOException {
        return Files.readAllBytes(storageDir.resolve(getContractFileName(borrowId)));
    }

    @Override
    public String getContractFileName(Integer borrowId) {
        return "borrow_contract_" + borrowId + ".pdf";
    }

    @Override
    public boolean exists(Integer borrowId) {
        return Files.exists(storageDir.resolve(getContractFileName(borrowId)));
    }

    private void writePdf(OutputStream outputStream, Borrow borrow, List<BorrowDetail> details) throws IOException {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            BaseFont baseFont = loadUnicodeBaseFont();
            Font titleFont = new Font(baseFont, 18, Font.BOLD);
            Font sectionFont = new Font(baseFont, 12, Font.BOLD);
            Font normalFont = new Font(baseFont, 11);

            document.add(new Paragraph("Phiếu mượn sách", titleFont));
            document.add(new Paragraph(" ", normalFont));
            document.add(new Paragraph("Mã phiếu: " + borrow.getBorrowId(), sectionFont));
            document.add(new Paragraph("Ngày lập phiếu: " + borrow.getBorrowDate().format(DATE_TIME_FORMATTER), normalFont));
            document.add(new Paragraph("Hạn trả: " + borrow.getDueDate().format(DATE_FORMATTER), normalFont));
            document.add(new Paragraph(" ", normalFont));

            document.add(new Paragraph("Thông tin người mượn", sectionFont));
            document.add(new Paragraph("Họ và tên: " + nullSafe(borrow.getUser().getUsername()), normalFont));
            document.add(new Paragraph("Email: " + nullSafe(borrow.getUser().getEmail()), normalFont));
            document.add(new Paragraph("Số điện thoại: " + nullSafe(borrow.getUser().getPhoneNumber()), normalFont));
            document.add(new Paragraph("Địa chỉ: " + nullSafe(borrow.getUser().getAddress()), normalFont));
            document.add(new Paragraph(" ", normalFont));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.2f, 4.5f, 1.2f, 3.1f});
            addHeaderCell(table, "STT", sectionFont);
            addHeaderCell(table, "Tên sách", sectionFont);
            addHeaderCell(table, "Số lượng", sectionFont);
            addHeaderCell(table, "Ghi chú", sectionFont);

            int index = 1;
            for (BorrowDetail detail : details) {
                table.addCell(new Phrase(String.valueOf(index++), normalFont));
                table.addCell(new Phrase(detail.getBook().getTitle(), normalFont));
                table.addCell(new Phrase(String.valueOf(detail.getQuantity()), normalFont));
                table.addCell(new Phrase(nullSafe(detail.getNote()), normalFont));
            }

            document.add(table);
            document.add(new Paragraph(" ", normalFont));
            document.add(new Paragraph(
                    "Người mượn xác nhận đã nhận sách và chấp nhận quy định của thư viện.",
                    normalFont
            ));
        } catch (DocumentException e) {
            throw new IOException("Cannot generate borrow contract pdf", e);
        } finally {
            document.close();
        }
    }

    private BaseFont loadUnicodeBaseFont() throws IOException {
        if (!configuredFontPath.isBlank() && Files.exists(Paths.get(configuredFontPath))) {
            return createBaseFont(configuredFontPath);
        }

        for (String path : DEFAULT_FONT_PATHS) {
            if (Files.exists(Paths.get(path))) {
                return createBaseFont(path);
            }
        }
        throw new IOException("Cannot find a Unicode font for Vietnamese PDF rendering");
    }

    private BaseFont createBaseFont(String fontPath) throws IOException {
        try {
            return BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (DocumentException e) {
            throw new IOException("Cannot load font: " + fontPath, e);
        }
    }

    private void addHeaderCell(PdfPTable table, String value, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        table.addCell(cell);
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
