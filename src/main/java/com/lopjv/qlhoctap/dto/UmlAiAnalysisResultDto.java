package com.lopjv.qlhoctap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO đại diện cho dữ liệu phản hồi JSON cấu trúc từ AI / LLM API sau khi phân tích bài nộp UML.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UmlAiAnalysisResultDto {

    private BigDecimal aiSuggestedScore;
    private String aiFeedback;
}
