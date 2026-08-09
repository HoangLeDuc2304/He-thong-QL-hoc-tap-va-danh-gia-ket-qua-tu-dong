package com.lopjv.qlhoctap.repository;

import com.lopjv.qlhoctap.entity.StudentExam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentExamRepository extends JpaRepository<StudentExam, Long> {
    Optional<StudentExam> findByExamIdAndStudentId(Long examId, Long studentId);
    List<StudentExam> findByExamId(Long examId);
    List<StudentExam> findByStudentId(Long studentId);
}
