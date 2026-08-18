package com.lopjv.qlhoctap.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO chứa câu trả lời của sinh viên cho từng câu hỏi trong bài thi.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentQuestionAnswerDto {

    @NotNull(message = "ID câu hỏi không được để trống")
    private Long questionId;

    private List<Long> selectedOptionIds;
}
