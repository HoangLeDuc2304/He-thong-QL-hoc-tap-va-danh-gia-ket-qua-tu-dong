package com.lopjv.qlhoctap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamResultResponse {

    private Long examResultId;
    private Long examId;
    private String examTitle;
    private BigDecimal score;
    private Integer totalCorrect;
    private Integer totalQuestions;
    private Integer tabSwitchCount;
    private Boolean isAutoSubmitted;
    private LocalDateTime submittedAt;
    private String message;
}
