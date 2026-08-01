package com.lopjv.qlhoctap.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "exam_results",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_exam_student_unique",
        columnNames = {"exam_id", "student_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @NotNull(message = "Điểm số không được để trống")
    @DecimalMin(value = "0.00", message = "Điểm số phải >= 0")
    @DecimalMax(value = "10.00", message = "Điểm số phải <= 10")
    @Column(name = "score", nullable = false, precision = 5, scale = 2)
    private BigDecimal score;

    @Min(value = 0, message = "Số câu đúng phải >= 0")
    @Column(name = "total_correct", nullable = false)
    private Integer totalCorrect;

    @Min(value = 0, message = "Tổng số câu hỏi phải >= 0")
    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @Column(name = "answers_json", columnDefinition = "TEXT")
    private String answersJson;

    @Min(value = 0, message = "Số lần chuyển tab phải >= 0")
    @Column(name = "tab_switch_count", nullable = false)
    private Integer tabSwitchCount;

    @Column(name = "is_auto_submitted", nullable = false)
    private Boolean isAutoSubmitted;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.submittedAt == null) {
            this.submittedAt = LocalDateTime.now();
        }
        if (this.score == null) {
            this.score = BigDecimal.ZERO;
        }
        if (this.totalCorrect == null) {
            this.totalCorrect = 0;
        }
        if (this.totalQuestions == null) {
            this.totalQuestions = 0;
        }
        if (this.tabSwitchCount == null) {
            this.tabSwitchCount = 0;
        }
        if (this.isAutoSubmitted == null) {
            this.isAutoSubmitted = false;
        }
    }
}
