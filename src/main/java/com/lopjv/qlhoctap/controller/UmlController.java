package com.lopjv.qlhoctap.controller;

import com.lopjv.qlhoctap.dto.GradeUmlSubmissionRequest;
import com.lopjv.qlhoctap.entity.Subject;
import com.lopjv.qlhoctap.entity.UmlAssignment;
import com.lopjv.qlhoctap.entity.UmlSubmission;
import com.lopjv.qlhoctap.entity.User;
import com.lopjv.qlhoctap.exception.ResourceNotFoundException;
import com.lopjv.qlhoctap.repository.SubjectRepository;
import com.lopjv.qlhoctap.repository.UmlAssignmentRepository;
import com.lopjv.qlhoctap.repository.UmlSubmissionRepository;
import com.lopjv.qlhoctap.repository.UserRepository;
import com.lopjv.qlhoctap.security.SecurityUtils;
import com.lopjv.qlhoctap.service.UmlAiGradingService;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class UmlController {

    private final UmlAssignmentRepository umlAssignmentRepository;
    private final UmlSubmissionRepository umlSubmissionRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final UmlAiGradingService umlAiGradingService;

    public UmlController(UmlAssignmentRepository umlAssignmentRepository,
            UmlSubmissionRepository umlSubmissionRepository,
            SubjectRepository subjectRepository,
            UserRepository userRepository,
            UmlAiGradingService umlAiGradingService) {
        this.umlAssignmentRepository = umlAssignmentRepository;
        this.umlSubmissionRepository = umlSubmissionRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.umlAiGradingService = umlAiGradingService;
    }

    @GetMapping("/subjects/{subjectId}/uml-assignments")
    public ResponseEntity<List<UmlAssignment>> getAssignmentsBySubject(@PathVariable Long subjectId) {
        return ResponseEntity.ok(umlAssignmentRepository.findBySubjectId(subjectId));
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @PostMapping("/teacher/uml-assignments")
    public ResponseEntity<UmlAssignment> createAssignment(@RequestParam Long subjectId,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String dueDate,
            @RequestParam(required = false) String rubricCriteria,
            @RequestParam(defaultValue = "10.00") String maxScore) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học với ID: " + subjectId));

        User createdBy = SecurityUtils.getCurrentUser(userRepository);

        UmlAssignment assignment = UmlAssignment.builder()
                .subject(subject)
                .createdBy(createdBy)
                .title(title)
                .description(description)
                .rubricCriteria(rubricCriteria)
                .maxScore(new BigDecimal(maxScore))
                .dueDate(OffsetDateTime.parse(dueDate))
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(umlAssignmentRepository.save(assignment));
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @GetMapping("/student/uml-submissions")
    public ResponseEntity<List<UmlSubmission>> getStudentSubmissions() {
        User student = SecurityUtils.getCurrentUser(userRepository);
        return ResponseEntity.ok(umlSubmissionRepository.findByStudentId(student.getId()));
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @PostMapping("/student/uml-submissions")
    public ResponseEntity<UmlSubmission> submitAssignment(@RequestParam Long assignmentId,
            @RequestParam(required = false) String fileUrl,
            @RequestParam(required = false) String fileType,
            @RequestParam(name = "file", required = false) MultipartFile file,
            @RequestParam(required = false) String plantumlSource) throws IOException {
        var logger = org.slf4j.LoggerFactory.getLogger(UmlController.class);
        logger.info("Submit UML request: assignmentId={}, fileUrlPresent={}, fileType={}, filePresent={}", assignmentId, fileUrl != null, fileType, file != null);

        boolean hasPlantuml = plantumlSource != null && !plantumlSource.isBlank();
        if ((file == null || file.isEmpty()) && (fileUrl == null || fileUrl.isBlank()) && !hasPlantuml) {
            logger.warn("Submission missing file, fileUrl and plantumlSource");
            return ResponseEntity.badRequest().build();
        }
        UmlAssignment assignment = umlAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài tập UML với ID: " + assignmentId));

        User student = SecurityUtils.getCurrentUser(userRepository);

        umlSubmissionRepository.findByAssignmentIdAndStudentId(assignmentId, student.getId())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Sinh viên đã nộp bài tập này rồi.");
                });

        String finalFileUrl = fileUrl;
        String finalFileType = fileType;

        try {
            if (plantumlSource != null && !plantumlSource.isBlank()) {
                // student submitted PlantUML source directly
                finalFileUrl = null;
                finalFileType = "PLANTUML";
            } else if (file != null && !file.isEmpty()) {
                // server-side validation: only allow png/jpg/jpeg/pdf and limit size
                long maxBytes = 20L * 1024L * 1024L; // 20 MB
                String ct = file.getContentType() != null ? file.getContentType().toLowerCase() : "";
                String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
                boolean isPdf = ct.contains("pdf") || originalName.endsWith(".pdf");
                boolean isPng = ct.contains("png") || originalName.endsWith(".png");
                boolean isJpeg = ct.contains("jpeg") || ct.contains("jpg") || originalName.endsWith(".jpg") || originalName.endsWith(".jpeg");

                if (!isPdf && !isPng && !isJpeg) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(UmlSubmission.builder()
                            .fileUrl("invalid-type")
                            .fileType("INVALID")
                            .build());
                }

                if (file.getSize() > maxBytes) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(UmlSubmission.builder()
                            .fileUrl("too-large")
                            .fileType("INVALID")
                            .build());
                }
                // ensure upload dir exists
                Path uploadRoot = Paths.get("uploads", "uml");
                Files.createDirectories(uploadRoot);

                String original = file.getOriginalFilename();
                String ext = "";
                if (original != null && original.contains(".")) {
                    ext = original.substring(original.lastIndexOf('.'));
                }
                String filename = UUID.randomUUID().toString() + ext;
                Path dest = uploadRoot.resolve(filename);
                Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

                finalFileUrl = "/uploads/uml/" + filename;
                String contentType = file.getContentType() != null ? file.getContentType().toLowerCase() : "";
                String extLower = ext.replace(".", "").toLowerCase();
                if (contentType.contains("pdf") || extLower.equals("pdf")) {
                    finalFileType = "PDF";
                } else {
                    finalFileType = "IMAGE";
                }
            }
        } catch (Exception e) {
            // log and return a clear error for frontend
            org.slf4j.LoggerFactory.getLogger(UmlController.class).error("Error saving uploaded file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(UmlSubmission.builder().fileUrl("error").fileType("ERROR").build());
        }

        if (!"PLANTUML".equalsIgnoreCase(finalFileType)) {
            if (finalFileType == null || !finalFileType.equalsIgnoreCase("PDF")) {
                finalFileType = "IMAGE";
            } else {
                finalFileType = "PDF";
            }
        }

        UmlSubmission.UmlSubmissionBuilder submissionBuilder = UmlSubmission.builder()
                .assignment(assignment)
                .student(student)
                .fileUrl(finalFileUrl)
                .fileType(finalFileType)
                .submittedAt(OffsetDateTime.now())
                .status("SUBMITTED");

        if (plantumlSource != null && !plantumlSource.isBlank()) {
            submissionBuilder.plantumlSource(plantumlSource);
        }

        UmlSubmission submission = submissionBuilder.build();

        return ResponseEntity.status(HttpStatus.CREATED).body(umlSubmissionRepository.save(submission));
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    @PostMapping("/student/uml-submissions/{id}/analyze")
    public ResponseEntity<UmlSubmission> analyzeSubmission(@PathVariable Long id) {
        return ResponseEntity.ok(umlAiGradingService.analyzeAndGradeUmlSubmission(id));
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @GetMapping("/teacher/uml-assignments/{assignmentId}/submissions")
    public ResponseEntity<List<UmlSubmission>> getSubmissionsByAssignment(@PathVariable Long assignmentId) {
        return ResponseEntity.ok(umlSubmissionRepository.findByAssignmentId(assignmentId));
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @PutMapping("/teacher/uml-submissions/{id}/grade")
    public ResponseEntity<UmlSubmission> gradeSubmission(@PathVariable Long id,
            @Valid @RequestBody GradeUmlSubmissionRequest request) {
        UmlSubmission submission = umlSubmissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài nộp UML với ID: " + id));

        User teacher = SecurityUtils.getCurrentUser(userRepository);

        submission.setFinalScore(request.getFinalScore());
        submission.setTeacherFeedback(request.getTeacherFeedback());
        submission.setGradedBy(teacher);
        submission.setGradedAt(OffsetDateTime.now());
        submission.setStatus("GRADED");

        return ResponseEntity.ok(umlSubmissionRepository.save(submission));
    }
}
