package com.example.librarymanage_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class DashboardStatisticsResponse {
    private long totalTitles;
    private long totalUsers;
    private long totalBorrowRecords;
    private long currentlyBorrowedBooks;
    private long currentlyBorrowingRecords;
    private long overdueBorrowItems;
    private long pendingFines;
    private long paidFines;
    private long totalBookCopies;
    private long availableBookCopies;
    private BigDecimal fineRevenue;
    private BigDecimal pendingFineAmount;
    private List<TopBorrowedBookResponse> topBorrowedBooks;
    private List<TopBorrowingUserResponse> topBorrowingUsers;
    private List<StatisticsChartPointResponse> chart;
}
