package com.lopjv.qlhoctap.repository;

import com.lopjv.qlhoctap.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByTeacherId(Long teacherId);

    List<Course> findByIsPublished(Boolean isPublished);

    List<Course> findByTeacherIdAndIsPublished(Long teacherId, Boolean isPublished);
}
