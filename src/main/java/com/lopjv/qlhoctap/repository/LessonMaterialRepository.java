package com.lopjv.qlhoctap.repository;

import com.lopjv.qlhoctap.entity.LessonMaterial;
import com.lopjv.qlhoctap.enums.MaterialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonMaterialRepository extends JpaRepository<LessonMaterial, Long> {

    List<LessonMaterial> findByLessonIdOrderByOrderIndexAsc(Long lessonId);

    List<LessonMaterial> findByLessonIdAndMaterialType(Long lessonId, MaterialType materialType);

    long countByLessonId(Long lessonId);

    void deleteByLessonId(Long lessonId);
}
