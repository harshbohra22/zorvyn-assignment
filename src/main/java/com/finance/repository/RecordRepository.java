package com.finance.repository;

import com.finance.entity.FinancialRecord;
import com.finance.enums.RecordType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecordRepository extends JpaRepository<FinancialRecord, Long> {

    Optional<FinancialRecord> findByIdAndIsDeletedFalse(Long id);

    @Query("SELECT r FROM FinancialRecord r WHERE r.isDeleted = false " +
           "AND (:type IS NULL OR r.type = :type) " +
           "AND (:category IS NULL OR r.category = :category) " +
           "AND (:startDate IS NULL OR r.date >= :startDate) " +
           "AND (:endDate IS NULL OR r.date <= :endDate)")
    Page<FinancialRecord> findWithFilters(
            @Param("type") RecordType type,
            @Param("category") String category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r WHERE r.type = :type AND r.isDeleted = false")
    BigDecimal sumByType(@Param("type") RecordType type);

    @Query("SELECT r.category, SUM(r.amount) FROM FinancialRecord r " +
           "WHERE r.isDeleted = false AND r.type = :type GROUP BY r.category ORDER BY SUM(r.amount) DESC")
    List<Object[]> sumByCategoryAndType(@Param("type") RecordType type);

    @Query("SELECT FUNCTION('YEAR', r.date), FUNCTION('MONTH', r.date), r.type, SUM(r.amount) " +
           "FROM FinancialRecord r WHERE r.isDeleted = false " +
           "GROUP BY FUNCTION('YEAR', r.date), FUNCTION('MONTH', r.date), r.type " +
           "ORDER BY FUNCTION('YEAR', r.date) DESC, FUNCTION('MONTH', r.date) DESC")
    List<Object[]> getMonthlyTrends();

    List<FinancialRecord> findTop10ByIsDeletedFalseOrderByDateDescCreatedAtDesc();
}
