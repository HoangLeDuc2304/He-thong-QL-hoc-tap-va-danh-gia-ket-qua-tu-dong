package com.lopjv.qlhoctap.repository;

import com.lopjv.qlhoctap.entity.UmlAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UmlAssignmentRepository extends JpaRepository<UmlAssignment, Long> {
    List<UmlAssignment> findBySubjectId(Long subjectId);
}
