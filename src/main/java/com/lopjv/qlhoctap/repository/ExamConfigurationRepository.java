package com.lopjv.qlhoctap.repository;

import com.lopjv.qlhoctap.entity.ExamConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExamConfigurationRepository extends JpaRepository<ExamConfiguration, Long> {
    List<ExamConfiguration> findByExamId(Long examId);
}
