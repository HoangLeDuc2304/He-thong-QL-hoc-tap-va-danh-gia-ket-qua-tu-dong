package com.lopjv.qlhoctap.api;

public record ResultDto(
        Long id,
        String testName,
        double score,
        String duration,
        int correctCount,
        int wrongCount,
        int totalQuestions,
        boolean answersVisible,
        String correctAnswers
) {
}
