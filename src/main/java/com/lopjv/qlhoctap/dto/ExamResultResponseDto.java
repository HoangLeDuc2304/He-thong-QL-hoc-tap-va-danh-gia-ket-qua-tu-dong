package com.lopjv.qlhoctap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * DTO phản hồi kết quả chấm điểm thi cho sinh viên.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamResultResponseDto {

    private Long studentExamId;
    private Long examId;
    private Long studentId;
    private String examTitle;
    private BigDecimal score;
    private Integer totalQuestions;
    private Integer correctCount;
    private Integer tabSwitchCount;
    private String status;
    private OffsetDateTime submitTime;
    private String note;
}
