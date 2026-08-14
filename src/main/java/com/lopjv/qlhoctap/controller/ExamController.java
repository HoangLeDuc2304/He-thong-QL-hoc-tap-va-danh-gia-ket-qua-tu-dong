package com.lopjv.qlhoctap.controller;

import com.lopjv.qlhoctap.dto.ExamMatrixConfigDto;
import com.lopjv.qlhoctap.entity.Exam;
import com.lopjv.qlhoctap.entity.ExamQuestion;
import com.lopjv.qlhoctap.entity.User;
import com.lopjv.qlhoctap.exception.ResourceNotFoundException;
import com.lopjv.qlhoctap.repository.ExamRepository;
import com.lopjv.qlhoctap.repository.SubjectRepository;
import com.lopjv.qlhoctap.repository.UserRepository;
import com.lopjv.qlhoctap.security.SecurityUtils;
import com.lopjv.qlhoctap.service.ExamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ExamController {

    private final ExamRepository examRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final ExamService examService;

    public ExamController(ExamRepository examRepository,
                         SubjectRepository subjectRepository,
                         UserRepository userRepository,
                         ExamService examService) {
        this.examRepository = examRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.examService = examService;
    }

    @GetMapping("/subjects/{subjectId}/exams")
    public ResponseEntity<List<Exam>> getExamsBySubject(@PathVariable Long subjectId) {
        return ResponseEntity.ok(examRepository.findBySubjectId(subjectId));
    }

    @GetMapping("/exams/{id}")
    public ResponseEntity<Exam> getExamById(@PathVariable Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề thi với ID: " + id));
        return ResponseEntity.ok(exam);
    }

    /**
     * Tạo đề thi mới. Người tạo được lấy từ JWT token (không nhận từ client).
     */
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @PostMapping("/teacher/exams")
    public ResponseEntity<Exam> createExam(@RequestParam Long subjectId,
                                          @RequestParam String title,
                                          @RequestParam Integer durationMinutes,
                                          @RequestParam String startTime,
                                          @RequestParam String endTime,
                                          @RequestParam(defaultValue = "3") Integer maxTabSwitches) {
        // Lấy giáo viên từ JWT thay vì nhận từ client
        User teacher = SecurityUtils.getCurrentUser(userRepository);

        if (!subjectRepository.existsById(subjectId)) {
            throw new ResourceNotFoundException("Không tìm thấy môn học với ID: " + subjectId);
        }

        Exam exam = Exam.builder()
                .subject(subjectRepository.getReferenceById(subjectId))
                .createdBy(teacher)
                .title(title)
                .durationMinutes(durationMinutes)
                .startTime(OffsetDateTime.parse(startTime))
                .endTime(OffsetDateTime.parse(endTime))
                .maxTabSwitches(maxTabSwitches)
                .status("DRAFT")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(examRepository.save(exam));
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @PostMapping("/teacher/exams/generate")
    public ResponseEntity<List<ExamQuestion>> generateExam(@Valid @RequestBody ExamMatrixConfigDto configDto) {
        return ResponseEntity.ok(examService.generateExamFromMatrix(configDto));
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @DeleteMapping("/teacher/exams/{id}")
    public ResponseEntity<Void> deleteExam(@PathVariable Long id) {
        if (!examRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy đề thi với ID: " + id);
        }
        examRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Thay đổi trạng thái đề thi thủ công: DRAFT → PUBLISHED → ARCHIVED.
     */
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @PutMapping("/teacher/exams/{id}/status")
    public ResponseEntity<Exam> updateExamStatus(@PathVariable Long id,
                                                 @RequestParam String status) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề thi với ID: " + id));

        if (!status.equals("DRAFT") && !status.equals("PUBLISHED") && !status.equals("ARCHIVED")) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ. Chỉ chấp nhận: DRAFT, PUBLISHED, ARCHIVED");
        }

        exam.setStatus(status);
        return ResponseEntity.ok(examRepository.save(exam));
    }
}
