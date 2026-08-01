package com.lopjv.qlhoctap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentAnswerDto {

    @NotNull(message = "ID câu hỏi không được để trống")
    private Long questionId;

    @NotBlank(message = "Đáp án đã chọn không được để trống")
    @Pattern(regexp = "[ABCD]", message = "Đáp án phải là A, B, C hoặc D")
    private String selectedOption;
}
