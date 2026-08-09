package com.lopjv.qlhoctap.repository;

import com.lopjv.qlhoctap.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findBySubjectId(Long subjectId);
    List<Exam> findByStatus(String status);
}
