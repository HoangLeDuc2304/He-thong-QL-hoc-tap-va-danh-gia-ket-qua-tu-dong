package com.lopjv.qlhoctap.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record QuestionRequest(
        @NotBlank String content,
        @NotBlank String optionA,
        @NotBlank String optionB,
        @NotBlank String optionC,
        @NotBlank String optionD,
        @NotBlank @Pattern(regexp = "A|B|C|D") String correctAnswer,
        @NotBlank String difficulty,
        @NotBlank String chapter
) {
    public QuestionDto toDto(Long id) {
        return new QuestionDto(id, content, optionA, optionB, optionC, optionD, correctAnswer, difficulty, chapter);
    }
}
