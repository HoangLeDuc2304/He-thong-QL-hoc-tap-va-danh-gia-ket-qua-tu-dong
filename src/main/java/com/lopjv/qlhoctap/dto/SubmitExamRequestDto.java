package com.lopjv.qlhoctap.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO chứa thông tin nộp bài thi trắc nghiệm của sinh viên và dữ liệu
 * anti-cheat.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitExamRequestDto {

    @NotNull(message = "ID đề thi không được để trống")
    private Long examId;

    // Không nhận từ client — được gán trong controller từ thông tin JWT sau khi validate
    private Long studentId;

    @Min(value = 0, message = "Số lần chuyển tab không được nhỏ hơn 0")
    @Builder.Default
    private Integer tabSwitchCount = 0;

    private List<StudentQuestionAnswerDto> answers;
}
