package com.lopjv.qlhoctap.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeUmlSubmissionRequest {
    @NotNull(message = "Điểm không được để trống")
    private BigDecimal finalScore;
    private String teacherFeedback;
}
