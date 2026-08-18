package com.lopjv.qlhoctap.repository;

import com.lopjv.qlhoctap.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findBySubjectIdOrderByOrderIndex(Long subjectId);
}
