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
public class EnrollmentDto {
    private Long id;
    private Long courseId;
    private String courseCode;
    private String courseTitle;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private OffsetDateTime enrolledAt;
}
