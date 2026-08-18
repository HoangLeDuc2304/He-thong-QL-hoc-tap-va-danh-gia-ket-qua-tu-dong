package com.lopjv.qlhoctap.controller;

import com.lopjv.qlhoctap.dto.EnrollmentDto;
import com.lopjv.qlhoctap.entity.User;
import com.lopjv.qlhoctap.repository.UserRepository;
import com.lopjv.qlhoctap.security.SecurityUtils;
import com.lopjv.qlhoctap.service.EnrollmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final UserRepository userRepository;

    public EnrollmentController(EnrollmentService enrollmentService, UserRepository userRepository) {
        this.enrollmentService = enrollmentService;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @PostMapping("/student/enrollments")
    public ResponseEntity<EnrollmentDto> enrollCourse(@RequestParam Long courseId) {
        User student = SecurityUtils.getCurrentUser(userRepository);
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.enrollCourse(courseId, student));
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @DeleteMapping("/student/enrollments/{courseId}")
    public ResponseEntity<Void> unenrollCourse(@PathVariable Long courseId) {
        User student = SecurityUtils.getCurrentUser(userRepository);
        enrollmentService.unenrollCourse(courseId, student);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    @GetMapping("/student/enrollments")
    public ResponseEntity<List<EnrollmentDto>> getMyEnrollments() {
        User student = SecurityUtils.getCurrentUser(userRepository);
        return ResponseEntity.ok(enrollmentService.getMyEnrollments(student));
    }

    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    @GetMapping("/teacher/courses/{courseId}/students")
    public ResponseEntity<List<EnrollmentDto>> getStudentsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentService.getStudentsByCourse(courseId));
    }
}
