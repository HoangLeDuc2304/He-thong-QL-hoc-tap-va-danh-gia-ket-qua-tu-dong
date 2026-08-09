package com.lopjv.qlhoctap.repository;

import com.lopjv.qlhoctap.entity.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {
    List<QuestionOption> findByQuestionId(Long questionId);
    List<QuestionOption> findByQuestionIdAndIsCorrectTrue(Long questionId);
}
