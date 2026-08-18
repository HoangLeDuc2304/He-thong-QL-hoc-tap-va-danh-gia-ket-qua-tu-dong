package com.lopjv.qlhoctap.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lopjv.qlhoctap.entity.ExamQuestion;

@Repository
public interface ExamQuestionRepository extends JpaRepository<ExamQuestion, ExamQuestion.ExamQuestionId> {

    List<ExamQuestion> findByExamIdOrderByOrderIndex(Long examId);

    void deleteByExamId(Long examId);
}
