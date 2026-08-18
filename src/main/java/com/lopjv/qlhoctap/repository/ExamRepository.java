package com.lopjv.qlhoctap.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lopjv.qlhoctap.entity.Exam;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findBySubjectId(Long subjectId);

    List<Exam> findByStatus(String status);

    List<Exam> findBySubjectCourseIdAndStatusOrderByStartTime(Long courseId, String status);
}
