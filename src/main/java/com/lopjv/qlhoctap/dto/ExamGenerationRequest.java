package com.lopjv.qlhoctap.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamGenerationRequest {

    @NotNull(message = "ID đề thi không được để trống")
    private Long examId;

    @NotNull(message = "ID khóa học không được để trống")
    private Long courseId;

    @NotBlank(message = "Chương không được để trống")
    private String chapter;

    @NotNull(message = "Số câu dễ không được để trống")
    @Min(value = 0, message = "Số câu dễ phải >= 0")
    private Integer easyCount;

    @NotNull(message = "Số câu trung bình không được để trống")
    @Min(value = 0, message = "Số câu trung bình phải >= 0")
    private Integer mediumCount;

    @NotNull(message = "Số câu khó không được để trống")
    @Min(value = 0, message = "Số câu khó phải >= 0")
    private Integer hardCount;

    public int getTotalQuestions() {
        return (easyCount != null ? easyCount : 0)
                + (mediumCount != null ? mediumCount : 0)
                + (hardCount != null ? hardCount : 0);
    }
}
