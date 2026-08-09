package com.lopjv.qlhoctap.repository;

import com.lopjv.qlhoctap.entity.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, ExamQuestion.ExamQuestionId> {
    List<ExamQuestion> findByExamIdOrderByOrderIndex(Long examId);
}
