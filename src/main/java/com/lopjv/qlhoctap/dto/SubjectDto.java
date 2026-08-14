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
public class SubjectDto {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String code;
    private String title;
    private String description;
    private Long teacherId;
    private String teacherName;
    private Integer orderIndex;
}
