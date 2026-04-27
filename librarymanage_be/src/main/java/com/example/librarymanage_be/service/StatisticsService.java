package com.example.librarymanage_be.service;

import com.example.librarymanage_be.dto.response.DashboardStatisticsResponse;
import com.example.librarymanage_be.enums.StatisticsGranularity;

import java.time.LocalDate;

public interface StatisticsService {
    DashboardStatisticsResponse getDashboardStatistics();

    DashboardStatisticsResponse getDashboardStatistics(LocalDate startDate,
                                                       LocalDate endDate,
                                                       StatisticsGranularity granularity);
}
