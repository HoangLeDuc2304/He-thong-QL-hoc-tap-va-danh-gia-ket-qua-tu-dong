package com.lopjv.qlhoctap.repository;

import com.lopjv.qlhoctap.entity.Question;
import com.lopjv.qlhoctap.enums.QuestionDifficulty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query(
        value = "SELECT * FROM question_bank " +
                "WHERE course_id = :courseId " +
                "AND chapter = :chapter " +
                "AND difficulty = CAST(:difficulty AS question_difficulty) " +
                "ORDER BY RANDOM() " +
                "LIMIT :limit",
        nativeQuery = true
    )
    List<Question> findRandomQuestionsByCourseAndChapterAndDifficulty(
        @Param("courseId") Long courseId,
        @Param("chapter") String chapter,
        @Param("difficulty") String difficulty,
        @Param("limit") int limit
    );

    @Query(
        value = "SELECT * FROM question_bank " +
                "WHERE course_id = :courseId " +
                "AND difficulty = CAST(:difficulty AS question_difficulty) " +
                "ORDER BY RANDOM() " +
                "LIMIT :limit",
        nativeQuery = true
    )
    List<Question> findRandomQuestionsByCourseAndDifficulty(
        @Param("courseId") Long courseId,
        @Param("difficulty") String difficulty,
        @Param("limit") int limit
    );

    List<Question> findByCourseId(Long courseId);

    List<Question> findByCourseIdAndChapter(Long courseId, String chapter);

    long countByCourseIdAndChapterAndDifficulty(Long courseId, String chapter, QuestionDifficulty difficulty);

    List<Question> findByCreatedById(Long createdById);
}
