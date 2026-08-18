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
public class ExamForStudentDto {

    private Long examId;
    private String title;
    private String subjectCode;
    private String subjectTitle;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private Integer durationMinutes;
    private Integer questionCount;
    private String status; // DRAFT, PUBLISHED, ARCHIVED
    private Integer maxTabSwitches;
}
