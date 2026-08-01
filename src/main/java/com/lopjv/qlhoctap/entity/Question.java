package com.lopjv.qlhoctap.entity;

import com.lopjv.qlhoctap.enums.QuestionDifficulty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Table(name = "question_bank")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Size(max = 100, message = "Tên chương không được vượt quá 100 ký tự")
    @Column(name = "chapter", length = 100)
    private String chapter;

    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @NotBlank(message = "Đáp án A không được để trống")
    @Size(max = 500, message = "Đáp án A không được vượt quá 500 ký tự")
    @Column(name = "option_a", nullable = false, length = 500)
    private String optionA;

    @NotBlank(message = "Đáp án B không được để trống")
    @Size(max = 500, message = "Đáp án B không được vượt quá 500 ký tự")
    @Column(name = "option_b", nullable = false, length = 500)
    private String optionB;

    @NotBlank(message = "Đáp án C không được để trống")
    @Size(max = 500, message = "Đáp án C không được vượt quá 500 ký tự")
    @Column(name = "option_c", nullable = false, length = 500)
    private String optionC;

    @NotBlank(message = "Đáp án D không được để trống")
    @Size(max = 500, message = "Đáp án D không được vượt quá 500 ký tự")
    @Column(name = "option_d", nullable = false, length = 500)
    private String optionD;

    @NotBlank(message = "Đáp án đúng không được để trống")
    @Pattern(regexp = "[ABCD]", message = "Đáp án đúng phải là A, B, C hoặc D")
    @Column(name = "correct_option", nullable = false, length = 1)
    private String correctOption;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false)
    private QuestionDifficulty difficulty;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.difficulty == null) {
            this.difficulty = QuestionDifficulty.MEDIUM;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
