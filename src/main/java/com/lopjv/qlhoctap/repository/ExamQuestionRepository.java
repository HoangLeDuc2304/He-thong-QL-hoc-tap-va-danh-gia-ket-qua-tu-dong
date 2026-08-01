package com.lopjv.qlhoctap.repository;

import com.lopjv.qlhoctap.entity.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, Long> {

    List<ExamQuestion> findByExamIdOrderByQuestionOrderAsc(Long examId);

    List<ExamQuestion> findByExamId(Long examId);
    long countByExamId(Long examId);

    void deleteByExamId(Long examId);
}
