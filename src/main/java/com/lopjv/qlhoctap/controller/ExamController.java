package com.lopjv.qlhoctap.controller;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lopjv.qlhoctap.entity.Exam;
import com.lopjv.qlhoctap.entity.ExamQuestion;
import com.lopjv.qlhoctap.entity.Question;
import com.lopjv.qlhoctap.entity.User;
import com.lopjv.qlhoctap.exception.ResourceNotFoundException;
import com.lopjv.qlhoctap.repository.ExamQuestionRepository;
import com.lopjv.qlhoctap.repository.ExamRepository;
import com.lopjv.qlhoctap.repository.QuestionRepository;
import com.lopjv.qlhoctap.repository.SubjectRepository;
import com.lopjv.qlhoctap.repository.UserRepository;
import com.lopjv.qlhoctap.security.SecurityUtils;
import com.lopjv.qlhoctap.service.ExamService;

@RestController
@RequestMapping("/api/v1")
public class ExamController {

    private final ExamRepository examRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamService examService;

    public ExamController(ExamRepository examRepository,
            SubjectRepository subjectRepository,
            UserRepository userRepository,
            QuestionRepository questionRepository,
            ExamQuestionRepository examQuestionRepository,
            ExamService examService) {
        this.examRepository = examRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.examQuestionRepository = examQuestionRepository;
        this.examService = examService;
    }

    @GetMapping("/teacher/exams")
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAllExamsForTeacher() {
        List<Exam> exams = examRepository.findAll();
        List<Map<String, Object>> response = exams.stream().map(exam -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", exam.getId());
            map.put("title", exam.getTitle());
            map.put("durationMinutes", exam.getDurationMinutes());
            map.put("startTime", exam.getStartTime());
            map.put("endTime", exam.getEndTime());
            map.put("status", exam.getStatus());
            map.put("subjectId", exam.getSubject().getId());
            map.put("questions", examQuestionRepository.findByExamIdOrderByOrderIndex(exam.getId()).size());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @PostMapping("/teacher/exams")
    @Transactional
    public ResponseEntity<Exam> createExam(@RequestBody Map<String, Object> payload) {
        User teacher = SecurityUtils.getCurrentUser(userRepository);

        Long subjectId = Long.valueOf(payload.get("subjectId").toString());
        String title = payload.get("title").toString();
        Integer durationMinutes = Integer.valueOf(payload.get("durationMinutes").toString());
        String startTimeStr = payload.get("startTime").toString();
        String endTimeStr = payload.get("endTime").toString();
        List<Integer> questionIds = (List<Integer>) payload.get("selectedQuestionIds");

        if (!subjectRepository.existsById(subjectId)) {
            throw new ResourceNotFoundException("Không tìm thấy môn học");
        }

        Exam exam = Exam.builder()
                .subject(subjectRepository.getReferenceById(subjectId))
                .createdBy(teacher)
                .title(title)
                .durationMinutes(durationMinutes)
                .startTime(OffsetDateTime.parse(startTimeStr.contains("Z") || startTimeStr.contains("+") ? startTimeStr : startTimeStr + ":00Z"))
                .endTime(OffsetDateTime.parse(endTimeStr.contains("Z") || endTimeStr.contains("+") ? endTimeStr : endTimeStr + ":00Z"))
                .maxTabSwitches(3)
                .status("PUBLISHED")
                .build();

        Exam savedExam = examRepository.save(exam);

        if (questionIds != null && !questionIds.isEmpty()) {
            for (int i = 0; i < questionIds.size(); i++) {
                Question q = questionRepository.findById(Long.valueOf(questionIds.get(i)))
                        .orElseThrow(() -> new ResourceNotFoundException("Câu hỏi không tồn tại"));

                ExamQuestion eq = new ExamQuestion();
                eq.setId(new ExamQuestion.ExamQuestionId(savedExam.getId(), q.getId()));
                eq.setExam(savedExam);
                eq.setQuestion(q);
                eq.setOrderIndex(i + 1);
                examQuestionRepository.save(eq);
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(savedExam);
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @PutMapping("/teacher/exams/{id}")
    @Transactional
    public ResponseEntity<Exam> updateExam(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài kiểm tra với ID: " + id));

        Long subjectId = Long.valueOf(payload.get("subjectId").toString());
        String title = payload.get("title").toString();
        Integer durationMinutes = Integer.valueOf(payload.get("durationMinutes").toString());
        String startTimeStr = payload.get("startTime").toString();
        String endTimeStr = payload.get("endTime").toString();
        @SuppressWarnings("unchecked")
        List<Integer> questionIds = (List<Integer>) payload.get("selectedQuestionIds");

        if (!subjectRepository.existsById(subjectId)) {
            throw new ResourceNotFoundException("Không tìm thấy môn học");
        }

        exam.setSubject(subjectRepository.getReferenceById(subjectId));
        exam.setTitle(title);
        exam.setDurationMinutes(durationMinutes);
        exam.setStartTime(OffsetDateTime.parse(startTimeStr.contains("Z") || startTimeStr.contains("+") ? startTimeStr : startTimeStr + ":00Z"));
        exam.setEndTime(OffsetDateTime.parse(endTimeStr.contains("Z") || endTimeStr.contains("+") ? endTimeStr : endTimeStr + ":00Z"));

        Exam savedExam = examRepository.save(exam);

        examQuestionRepository.deleteByExamId(id);
        if (questionIds != null && !questionIds.isEmpty()) {
            for (int i = 0; i < questionIds.size(); i++) {
                Question q = questionRepository.findById(Long.valueOf(questionIds.get(i)))
                        .orElseThrow(() -> new ResourceNotFoundException("Câu hỏi không tồn tại"));

                ExamQuestion eq = new ExamQuestion();
                eq.setId(new ExamQuestion.ExamQuestionId(savedExam.getId(), q.getId()));
                eq.setExam(savedExam);
                eq.setQuestion(q);
                eq.setOrderIndex(i + 1);
                examQuestionRepository.save(eq);
            }
        }

        return ResponseEntity.ok(savedExam);
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @DeleteMapping("/teacher/exams/{id}")
    public ResponseEntity<Void> deleteExam(@PathVariable Long id) {
        examRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
