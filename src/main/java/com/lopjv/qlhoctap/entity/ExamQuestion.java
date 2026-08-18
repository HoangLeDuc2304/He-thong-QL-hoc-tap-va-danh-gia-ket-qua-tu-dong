package com.lopjv.qlhoctap.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Snapshot câu hỏi trong đề thi (Composite PK: exam_id + question_id).
 */
@Entity
@Table(name = "exam_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamQuestion {

    @EmbeddedId
    private ExamQuestionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("examId")
    @JoinColumn(name = "exam_id")
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("questionId")
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class ExamQuestionId implements Serializable {
        private Long examId;
        private Long questionId;
    }
}
