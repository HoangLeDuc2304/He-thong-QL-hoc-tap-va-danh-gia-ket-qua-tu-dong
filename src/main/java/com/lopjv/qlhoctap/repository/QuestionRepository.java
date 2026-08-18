package com.lopjv.qlhoctap.repository;

import com.lopjv.qlhoctap.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findBySubjectId(Long subjectId);

    long countBySubjectIdAndDifficulty(Long subjectId, String difficulty);

    long countBySubjectIdAndChapterTopicAndDifficulty(Long subjectId, String chapterTopic, String difficulty);

    @Query(value = "SELECT * FROM question_bank WHERE subject_id = :subjectId AND difficulty = :difficulty ORDER BY RANDOM() LIMIT :count", nativeQuery = true)
    List<Question> findRandomBySubjectIdAndDifficulty(
            @Param("subjectId") Long subjectId,
            @Param("difficulty") String difficulty,
            @Param("count") int count
    );

    @Query(value = "SELECT * FROM question_bank WHERE subject_id = :subjectId AND chapter_topic = :chapterTopic AND difficulty = :difficulty ORDER BY RANDOM() LIMIT :count", nativeQuery = true)
    List<Question> findRandomBySubjectIdAndChapterAndDifficulty(
            @Param("subjectId") Long subjectId,
            @Param("chapterTopic") String chapterTopic,
            @Param("difficulty") String difficulty,
            @Param("count") int count
    );
}
