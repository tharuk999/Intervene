package com.interactout.repository;

import com.interactout.model.UsageSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UsageSessionRepository extends JpaRepository<UsageSessionEntity, Long> {

    List<UsageSessionEntity> findByPackageNameAndSessionDate(String packageName, LocalDate date);

    /** Total ms used per package on a given date — for dashboard breakdown */
    @Query("SELECT u.packageName, SUM(u.durationMs) FROM UsageSessionEntity u " +
           "WHERE u.sessionDate = :date GROUP BY u.packageName")
    List<Object[]> dailyTotalsByPackage(@Param("date") LocalDate date);

    /** Sum ms for a single package on a date */
    @Query("SELECT COALESCE(SUM(u.durationMs), 0) FROM UsageSessionEntity u " +
           "WHERE u.packageName = :pkg AND u.sessionDate = :date")
    long sumDurationByPackageAndDate(@Param("pkg") String packageName, @Param("date") LocalDate date);

    /** Total resisted urges across all packages on a date */
    @Query("SELECT COALESCE(SUM(u.resistedUrges), 0) FROM UsageSessionEntity u " +
           "WHERE u.sessionDate = :date")
    long totalResistedUrgesOnDate(@Param("date") LocalDate date);

    /** Sessions in a date range — for weekly history chart */
    @Query("SELECT u FROM UsageSessionEntity u WHERE u.sessionDate BETWEEN :from AND :to ORDER BY u.sessionDate")
    List<UsageSessionEntity> findByDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
