package com.lopjv.qlhoctap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamResultDto {

    private Long resultId;
    private String examTitle;
    private String subjectTitle;
    private Double score;
    private Integer durationMinutes;
    private String status;
    private OffsetDateTime startTime;
    private OffsetDateTime submitTime;
}
