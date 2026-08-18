package com.lopjv.qlhoctap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrolledCourseDto {

    private Long courseId;
    private String code;
    private String title;
    private String description;
    private Long createdById;
    private String createdByName;
    private Integer totalStudents;
}
