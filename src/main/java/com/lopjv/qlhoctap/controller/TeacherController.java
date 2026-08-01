package com.lopjv.qlhoctap.controller;

import com.lopjv.qlhoctap.dto.ExamGenerationRequest;
import com.lopjv.qlhoctap.dto.QuestionImportResult;
import com.lopjv.qlhoctap.entity.ExamQuestion;
import com.lopjv.qlhoctap.service.ExamGenerationService;
import com.lopjv.qlhoctap.service.QuestionImportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    private final ExamGenerationService examGenerationService;
    private final QuestionImportService questionImportService;

    public TeacherController(
            ExamGenerationService examGenerationService,
            QuestionImportService questionImportService) {
        this.examGenerationService = examGenerationService;
        this.questionImportService = questionImportService;
    }

    @PostMapping("/exams/generate")
    public ResponseEntity<Map<String, Object>> generateExam(
            @Valid @RequestBody ExamGenerationRequest request) {
        List<ExamQuestion> generatedQuestions = examGenerationService.generateExamQuestions(request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Trộn đề thành công!");
        response.put("examId", request.getExamId());
        response.put("totalQuestions", generatedQuestions.size());
        response.put("easyCount", request.getEasyCount());
        response.put("mediumCount", request.getMediumCount());
        response.put("hardCount", request.getHardCount());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/questions/import/{courseId}")
    public ResponseEntity<QuestionImportResult> importQuestions(
            @RequestParam("file") MultipartFile file,
            @PathVariable("courseId") Long courseId) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    QuestionImportResult.builder()
                            .totalRows(0)
                            .successCount(0)
                            .errorCount(1)
                            .errorDetails(List.of("File rỗng. Vui lòng chọn file Excel (.xlsx) có dữ liệu."))
                            .build());
        }

        QuestionImportResult importResult = questionImportService.importQuestionsFromExcel(file, courseId);
        return ResponseEntity.status(HttpStatus.CREATED).body(importResult);
    }
}
