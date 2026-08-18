package com.lopjv.qlhoctap.entity;

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
 * Đáp án sinh viên đã chọn (hỗ trợ MULTIPLE_CHOICE).
 */
@Entity
@Table(name = "student_answer_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentAnswerOption {

    @EmbeddedId
    private StudentAnswerOptionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("studentAnswerId")
    @JoinColumn(name = "student_answer_id")
    private StudentAnswer studentAnswer;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("optionId")
    @JoinColumn(name = "option_id")
    private QuestionOption option;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class StudentAnswerOptionId implements Serializable {
        private Long studentAnswerId;
        private Long optionId;
    }
}
