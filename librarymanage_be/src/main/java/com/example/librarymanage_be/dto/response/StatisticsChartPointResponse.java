package com.example.librarymanage_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class StatisticsChartPointResponse {
    private String label;
    private long borrowCount;
    private long borrowedBookQuantity;
    private BigDecimal fineRevenue;
    private BigDecimal pendingFineAmount;
}
