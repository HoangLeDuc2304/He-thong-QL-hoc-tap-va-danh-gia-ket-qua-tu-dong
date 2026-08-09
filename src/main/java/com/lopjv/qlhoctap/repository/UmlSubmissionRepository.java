package com.lopjv.qlhoctap.repository;

import com.lopjv.qlhoctap.entity.UmlSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UmlSubmissionRepository extends JpaRepository<UmlSubmission, Long> {
    List<UmlSubmission> findByAssignmentId(Long assignmentId);
    List<UmlSubmission> findByStudentId(Long studentId);
    Optional<UmlSubmission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);
    List<UmlSubmission> findByStatus(String status);
}
