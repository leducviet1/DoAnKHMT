package com.example.librarymanage_be.service.Impl;

import com.example.librarymanage_be.dto.response.DashboardStatisticsResponse;
import com.example.librarymanage_be.dto.response.StatisticsChartPointResponse;
import com.example.librarymanage_be.dto.response.TopBorrowedBookResponse;
import com.example.librarymanage_be.dto.response.TopBorrowingUserResponse;
import com.example.librarymanage_be.enums.BorrowDetailStatus;
import com.example.librarymanage_be.enums.BorrowStatus;
import com.example.librarymanage_be.enums.FineStatus;
import com.example.librarymanage_be.enums.StatisticsGranularity;
import com.example.librarymanage_be.entity.Borrow;
import com.example.librarymanage_be.entity.Fine;
import com.example.librarymanage_be.repo.BookRepository;
import com.example.librarymanage_be.repo.BorrowDetailRepository;
import com.example.librarymanage_be.repo.BorrowRepository;
import com.example.librarymanage_be.repo.FineRepository;
import com.example.librarymanage_be.repo.UserRepository;
import com.example.librarymanage_be.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsServiceImpl implements StatisticsService {
    private static final int TOP_LIMIT = 5;
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MM/yyyy");

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BorrowRepository borrowRepository;
    private final BorrowDetailRepository borrowDetailRepository;
    private final FineRepository fineRepository;

    @Override
    public DashboardStatisticsResponse getDashboardStatistics() {
        return getDashboardStatistics(null, null, StatisticsGranularity.DAY);
    }

    @Override
    public DashboardStatisticsResponse getDashboardStatistics(LocalDate startDate,
                                                              LocalDate endDate,
                                                              StatisticsGranularity granularity) {
        LocalDateTime startDateTime = startDate == null ? null : startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate == null ? null : endDate.plusDays(1).atStartOfDay();

        List<TopBorrowedBookResponse> topBorrowedBooks = borrowDetailRepository.findTopBorrowedBooks(
                        startDateTime, endDateTime, PageRequest.of(0, TOP_LIMIT))
                .stream()
                .map(this::mapTopBook)
                .toList();

        List<TopBorrowingUserResponse> topBorrowingUsers = borrowDetailRepository.findTopBorrowingUsers(
                        startDateTime, endDateTime, PageRequest.of(0, TOP_LIMIT))
                .stream()
                .map(this::mapTopUser)
                .toList();

        List<Borrow> filteredBorrows = getFilteredBorrows(startDateTime, endDateTime);
        List<Fine> filteredFines = getFilteredFines(startDateTime, endDateTime);

        long totalTitles = bookRepository.count();
        long totalUsers = userRepository.count();
        long totalBorrowRecords = filteredBorrows.size();
        long currentlyBorrowedBooks = nullSafeLong(borrowDetailRepository.sumQuantityByStatus(BorrowDetailStatus.BORROWING));
        long currentlyBorrowingRecords = borrowRepository.countByStatus(BorrowStatus.BORROWING);
        long overdueBorrowItems = borrowDetailRepository.countByStatus(BorrowDetailStatus.OVERDATE);
        long pendingFines = filteredFines.stream().filter(fine -> fine.getStatus() == FineStatus.PENDING).count();
        long paidFines = filteredFines.stream().filter(fine -> fine.getStatus() == FineStatus.PAIDED).count();
        long totalBookCopies = nullSafeLong(bookRepository.sumTotalQuantity());
        long availableBookCopies = nullSafeLong(bookRepository.sumAvailableQuantity());
        BigDecimal fineRevenue = filteredFines.stream()
                .filter(fine -> fine.getStatus() == FineStatus.PAIDED)
                .map(Fine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pendingFineAmount = filteredFines.stream()
                .filter(fine -> fine.getStatus() == FineStatus.PENDING)
                .map(Fine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<StatisticsChartPointResponse> chart = buildChart(filteredBorrows, filteredFines, granularity);

        return new DashboardStatisticsResponse(
                totalTitles,
                totalUsers,
                totalBorrowRecords,
                currentlyBorrowedBooks,
                currentlyBorrowingRecords,
                overdueBorrowItems,
                pendingFines,
                paidFines,
                totalBookCopies,
                availableBookCopies,
                fineRevenue,
                pendingFineAmount,
                topBorrowedBooks,
                topBorrowingUsers,
                chart
        );
    }

    private TopBorrowedBookResponse mapTopBook(Object[] row) {
        return new TopBorrowedBookResponse(
                (Integer) row[0],
                (String) row[1],
                ((Number) row[2]).longValue()
        );
    }

    private TopBorrowingUserResponse mapTopUser(Object[] row) {
        return new TopBorrowingUserResponse(
                (Integer) row[0],
                (String) row[1],
                (String) row[2],
                ((Number) row[3]).longValue()
        );
    }

    private long nullSafeLong(Long value) {
        return value == null ? 0L : value;
    }

    private BigDecimal nullSafeDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private List<Borrow> getFilteredBorrows(LocalDateTime start, LocalDateTime end) {
        List<Borrow> borrows = (start != null && end != null)
                ? borrowRepository.findAllByBorrowDateBetween(start, end)
                : borrowRepository.findAll();
        return borrows.stream()
                .filter(borrow -> isWithinRange(borrow.getBorrowDate(), start, end))
                .toList();
    }

    private List<Fine> getFilteredFines(LocalDateTime start, LocalDateTime end) {
        List<Fine> fines = (start != null && end != null)
                ? fineRepository.findAllByCreatedAtBetween(start, end)
                : fineRepository.findAll();
        return fines.stream()
                .filter(fine -> isWithinRange(fine.getCreatedAt(), start, end))
                .toList();
    }

    private List<StatisticsChartPointResponse> buildChart(List<Borrow> borrows,
                                                          List<Fine> fines,
                                                          StatisticsGranularity granularity) {
        Map<String, StatisticsAccumulator> buckets = new LinkedHashMap<>();

        for (Borrow borrow : borrows) {
            if (borrow.getBorrowDate() == null) {
                continue;
            }
            String key = formatKey(borrow.getBorrowDate(), granularity);
            StatisticsAccumulator accumulator = buckets.computeIfAbsent(key, ignored -> new StatisticsAccumulator());
            accumulator.borrowCount++;
            long quantity = borrow.getDetails() == null ? 0L : borrow.getDetails().stream()
                    .map(detail -> detail.getQuantity() == null ? 0 : detail.getQuantity())
                    .mapToLong(Integer::longValue)
                    .sum();
            accumulator.borrowedBookQuantity += quantity;
        }

        for (Fine fine : fines) {
            if (fine.getCreatedAt() == null) {
                continue;
            }
            String key = formatKey(fine.getCreatedAt(), granularity);
            StatisticsAccumulator accumulator = buckets.computeIfAbsent(key, ignored -> new StatisticsAccumulator());
            if (fine.getStatus() == FineStatus.PAIDED) {
                accumulator.fineRevenue = accumulator.fineRevenue.add(nullSafeDecimal(fine.getAmount()));
            }
            if (fine.getStatus() == FineStatus.PENDING) {
                accumulator.pendingFineAmount = accumulator.pendingFineAmount.add(nullSafeDecimal(fine.getAmount()));
            }
        }

        return buckets.entrySet().stream()
                .map(entry -> new StatisticsChartPointResponse(
                        entry.getKey(),
                        entry.getValue().borrowCount,
                        entry.getValue().borrowedBookQuantity,
                        entry.getValue().fineRevenue,
                        entry.getValue().pendingFineAmount
                ))
                .toList();
    }

    private String formatKey(LocalDateTime dateTime, StatisticsGranularity granularity) {
        if (granularity == StatisticsGranularity.MONTH) {
            return YearMonth.from(dateTime).format(MONTH_FORMATTER);
        }
        return dateTime.toLocalDate().format(DAY_FORMATTER);
    }

    private boolean isWithinRange(LocalDateTime value, LocalDateTime start, LocalDateTime end) {
        if (value == null) {
            return false;
        }
        boolean afterStart = start == null || !value.isBefore(start);
        boolean beforeEnd = end == null || value.isBefore(end);
        return afterStart && beforeEnd;
    }

    private static class StatisticsAccumulator {
        private long borrowCount;
        private long borrowedBookQuantity;
        private BigDecimal fineRevenue = BigDecimal.ZERO;
        private BigDecimal pendingFineAmount = BigDecimal.ZERO;
    }
}
