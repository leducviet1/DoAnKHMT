package com.example.librarymanage_be.repo;

import com.example.librarymanage_be.entity.BorrowDetail;
import com.example.librarymanage_be.enums.BorrowDetailStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BorrowDetailRepository extends JpaRepository<BorrowDetail,Integer> {
    List<BorrowDetail> findByBorrow_BorrowId(Integer borrowId);

    @Query("""
            select bd.book.bookId, bd.book.title, coalesce(sum(bd.quantity), 0)
            from BorrowDetail bd
            where (:start is null or bd.borrow.borrowDate >= :start)
              and (:end is null or bd.borrow.borrowDate < :end)
            group by bd.book.bookId, bd.book.title
            order by coalesce(sum(bd.quantity), 0) desc
            """)
    List<Object[]> findTopBorrowedBooks(@Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end,
                                        Pageable pageable);

    @Query("""
            select bd.borrow.user.userId, bd.borrow.user.username, bd.borrow.user.email, coalesce(sum(bd.quantity), 0)
            from BorrowDetail bd
            where (:start is null or bd.borrow.borrowDate >= :start)
              and (:end is null or bd.borrow.borrowDate < :end)
            group by bd.borrow.user.userId, bd.borrow.user.username, bd.borrow.user.email
            order by coalesce(sum(bd.quantity), 0) desc
            """)
    List<Object[]> findTopBorrowingUsers(@Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end,
                                         Pageable pageable);

    @Query("select coalesce(sum(bd.quantity), 0) from BorrowDetail bd where bd.status = :status")
    Long sumQuantityByStatus(BorrowDetailStatus status);

    long countByStatus(BorrowDetailStatus status);
}
