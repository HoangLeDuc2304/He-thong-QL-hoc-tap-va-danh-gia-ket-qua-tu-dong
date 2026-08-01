package com.lopjv.qlhoctap.repository;

import com.lopjv.qlhoctap.entity.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {

    List<CourseEnrollment> findByStudentId(Long studentId);

    List<CourseEnrollment> findByCourseId(Long courseId);

    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);

    Optional<CourseEnrollment> findByCourseIdAndStudentId(Long courseId, Long studentId);

    long countByCourseId(Long courseId);

    @Query("SELECT ce.student.id FROM CourseEnrollment ce WHERE ce.course.id = :courseId")
    List<Long> findStudentIdsByCourseId(@Param("courseId") Long courseId);
}
