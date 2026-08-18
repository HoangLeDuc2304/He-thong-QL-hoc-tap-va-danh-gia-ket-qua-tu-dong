package com.lopjv.qlhoctap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateQuestionRequest {

    @NotNull(message = "ID môn học không được để trống")
    private Long subjectId;

    private String chapterTopic;

    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    private String content;

    @Builder.Default
    private String questionType = "SINGLE_CHOICE";

    @NotBlank(message = "Độ khó không được để trống")
    private String difficulty;

    @Builder.Default
    private List<QuestionOptionInput> options = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionOptionInput {
        @NotBlank(message = "Nội dung đáp án không được để trống")
        private String content;

        @Builder.Default
        private Boolean isCorrect = false;
    }
}
