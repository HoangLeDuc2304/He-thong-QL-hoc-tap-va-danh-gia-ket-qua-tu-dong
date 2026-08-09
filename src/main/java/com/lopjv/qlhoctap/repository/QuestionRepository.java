package com.lopjv.qlhoctap.repository;

import com.lopjv.qlhoctap.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findBySubjectId(Long subjectId);

    @Query(value = "SELECT * FROM question_bank WHERE subject_id = :subjectId AND difficulty = :difficulty ORDER BY RANDOM() LIMIT :count", nativeQuery = true)
    List<Question> findRandomBySubjectIdAndDifficulty(Long subjectId, String difficulty, int count);

    @Query(value = "SELECT * FROM question_bank WHERE subject_id = :subjectId AND chapter_topic = :chapterTopic AND difficulty = :difficulty ORDER BY RANDOM() LIMIT :count", nativeQuery = true)
    List<Question> findRandomBySubjectIdAndChapterAndDifficulty(Long subjectId, String chapterTopic, String difficulty, int count);
}
