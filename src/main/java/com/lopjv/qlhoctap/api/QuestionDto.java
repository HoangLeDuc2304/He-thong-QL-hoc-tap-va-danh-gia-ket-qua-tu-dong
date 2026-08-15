package com.lopjv.qlhoctap.api;

public record QuestionDto(
        Long id,
        String content,
        String optionA,
        String optionB,
        String optionC,
        String optionD,
        String correctAnswer,
        String difficulty,
        String chapter
) {
    public String searchText() {
        return String.join(" ", content, optionA, optionB, optionC, optionD).toLowerCase();
    }
}
