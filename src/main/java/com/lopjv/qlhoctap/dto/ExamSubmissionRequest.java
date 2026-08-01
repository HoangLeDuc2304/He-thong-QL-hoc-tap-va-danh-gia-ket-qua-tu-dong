package com.lopjv.qlhoctap.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamSubmissionRequest {

    @NotNull(message = "ID đề thi không được để trống")
    private Long examId;

    @NotEmpty(message = "Danh sách câu trả lời không được rỗng")
    @Valid
    private List<StudentAnswerDto> answers;

    @NotNull(message = "Số lần chuyển tab không được để trống")
    @Min(value = 0, message = "Số lần chuyển tab phải >= 0")
    private Integer tabSwitchCount;

    private Boolean isAutoSubmitted;
}
