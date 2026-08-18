package com.lopjv.qlhoctap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO hiển thị câu hỏi cho sinh viên (không bao gồm đáp án đúng).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamQuestionForStudentDto {
    private Long id;
    private String content;
    private String questionType;
    private List<QuestionOptionForStudentDto> options;
}
