package com.lopjv.qlhoctap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionDto {
    private Long id;
    private Long subjectId;
    private String subjectTitle;
    private String chapterTopic;
    private String content;
    private String questionType;
    private String difficulty;
    private List<QuestionOptionDto> options;
}
