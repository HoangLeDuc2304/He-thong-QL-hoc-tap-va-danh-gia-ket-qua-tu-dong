package com.lopjv.qlhoctap.controller;

import com.lopjv.qlhoctap.dto.ExamResultResponse;
import com.lopjv.qlhoctap.dto.ExamSubmissionRequest;
import com.lopjv.qlhoctap.service.ExamGradingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller xử lý các API dành cho Sinh viên (Student).
 */
@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final ExamGradingService examGradingService;

    public StudentController(ExamGradingService examGradingService) {
        this.examGradingService = examGradingService;
    }

    @PostMapping("/exams/submit")
    public ResponseEntity<ExamResultResponse> submitExam(
            @Valid @RequestBody ExamSubmissionRequest submissionRequest) {
        ExamResultResponse examResult = examGradingService.gradeAndSaveResult(submissionRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(examResult);
    }
}
