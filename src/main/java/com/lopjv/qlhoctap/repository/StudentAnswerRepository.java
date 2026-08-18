package com.lopjv.qlhoctap.repository;

import com.lopjv.qlhoctap.entity.StudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {

    List<StudentAnswer> findByStudentExamId(Long studentExamId);

    Optional<StudentAnswer> findByStudentExamIdAndQuestionId(Long studentExamId, Long questionId);
}
