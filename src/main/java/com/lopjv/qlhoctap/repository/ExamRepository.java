package com.lopjv.qlhoctap.repository;

import com.lopjv.qlhoctap.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findByCourseId(Long courseId);

    List<Exam> findByCreatedById(Long createdById);

    @Query("SELECT e FROM Exam e " +
           "WHERE e.course.id = :courseId " +
           "AND e.isActive = true " +
           "AND e.startTime <= :currentTime " +
           "AND e.endTime >= :currentTime")
    List<Exam> findActiveExamsByCourseId(
        @Param("courseId") Long courseId,
        @Param("currentTime") LocalDateTime currentTime
    );
}
