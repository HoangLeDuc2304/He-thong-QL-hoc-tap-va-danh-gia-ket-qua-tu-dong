package com.lopjv.qlhoctap.repository;

import com.lopjv.qlhoctap.entity.StudentAnswerOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentAnswerOptionRepository extends JpaRepository<StudentAnswerOption, StudentAnswerOption.StudentAnswerOptionId> {

    List<StudentAnswerOption> findByStudentAnswerId(Long studentAnswerId);
}
