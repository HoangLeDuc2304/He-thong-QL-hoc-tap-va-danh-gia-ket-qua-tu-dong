package com.lopjv.qlhoctap.controller;

import com.lopjv.qlhoctap.dto.LessonDto;
import com.lopjv.qlhoctap.service.LessonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @GetMapping("/subjects/{subjectId}/lessons")
    public ResponseEntity<List<LessonDto>> getLessonsBySubject(@PathVariable Long subjectId) {
        return ResponseEntity.ok(lessonService.getLessonsBySubject(subjectId));
    }

    @GetMapping("/lessons/{id}")
    public ResponseEntity<LessonDto> getLessonById(@PathVariable Long id) {
        return ResponseEntity.ok(lessonService.getLessonById(id));
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @PostMapping("/teacher/subjects/{subjectId}/lessons")
    public ResponseEntity<LessonDto> createLesson(@PathVariable Long subjectId,
                                                @Valid @RequestBody LessonDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonService.createLesson(subjectId, dto));
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @PutMapping("/teacher/lessons/{id}")
    public ResponseEntity<LessonDto> updateLesson(@PathVariable Long id,
                                                @Valid @RequestBody LessonDto dto) {
        return ResponseEntity.ok(lessonService.updateLesson(id, dto));
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @DeleteMapping("/teacher/lessons/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id) {
        lessonService.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }
}
