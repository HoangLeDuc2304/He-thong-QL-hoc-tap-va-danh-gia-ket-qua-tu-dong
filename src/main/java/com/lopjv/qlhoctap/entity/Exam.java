package com.lopjv.qlhoctap.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "exams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @NotBlank(message = "Tiêu đề bài thi không được để trống")
    @Size(max = 255, message = "Tiêu đề bài thi không được vượt quá 255 ký tự")
    @Column(name = "exam_title", nullable = false, length = 255)
    private String examTitle;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Thời lượng làm bài không được để trống")
    @Min(value = 1, message = "Thời lượng làm bài phải ít nhất 1 phút")
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @NotNull(message = "Số lần chuyển tab tối đa không được để trống")
    @Min(value = 0, message = "Số lần chuyển tab tối đa phải >= 0")
    @Column(name = "max_tab_switches", nullable = false)
    private Integer maxTabSwitches;

    @NotNull(message = "Thời gian bắt đầu thi không được để trống")
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @NotNull(message = "Thời gian kết thúc thi không được để trống")
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "show_answers_after_submit", nullable = false)
    @Builder.Default
    private Boolean showAnswersAfterSubmit = false;

    @DecimalMin(value = "0.0", message = "Điểm đỗ phải >= 0")
    @DecimalMax(value = "10.0", message = "Điểm đỗ phải <= 10")
    @Column(name = "pass_score", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal passScore = new BigDecimal("5.00");

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "exam", fetch = FetchType.LAZY)
    @Builder.Default
    private List<ExamQuestion> examQuestions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.durationMinutes == null) {
            this.durationMinutes = 60;
        }
        if (this.maxTabSwitches == null) {
            this.maxTabSwitches = 3;
        }
        if (this.showAnswersAfterSubmit == null) {
            this.showAnswersAfterSubmit = false;
        }
        if (this.passScore == null) {
            this.passScore = new BigDecimal("5.00");
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
