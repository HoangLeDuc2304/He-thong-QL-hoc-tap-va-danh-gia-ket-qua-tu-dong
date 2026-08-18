package com.lopjv.qlhoctap.controller;

import com.lopjv.qlhoctap.dto.CreateSubjectRequest;
import com.lopjv.qlhoctap.dto.SubjectDto;
import com.lopjv.qlhoctap.service.SubjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class SubjectController {

    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping("/courses/{courseId}/subjects")
    public ResponseEntity<List<SubjectDto>> getSubjectsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(subjectService.getSubjectsByCourse(courseId));
    }

    @GetMapping("/subjects/{id}")
    public ResponseEntity<SubjectDto> getSubjectById(@PathVariable Long id) {
        return ResponseEntity.ok(subjectService.getSubjectById(id));
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @PostMapping("/teacher/subjects")
    public ResponseEntity<SubjectDto> createSubject(@Valid @RequestBody CreateSubjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subjectService.createSubject(request));
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @PutMapping("/teacher/subjects/{id}")
    public ResponseEntity<SubjectDto> updateSubject(@PathVariable Long id,
                                                 @Valid @RequestBody CreateSubjectRequest request) {
        return ResponseEntity.ok(subjectService.updateSubject(id, request));
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @DeleteMapping("/teacher/subjects/{id}")
    public ResponseEntity<Void> deleteSubject(@PathVariable Long id) {
        subjectService.deleteSubject(id);
        return ResponseEntity.noContent().build();
    }
}
