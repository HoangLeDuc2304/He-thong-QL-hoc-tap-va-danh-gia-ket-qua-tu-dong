package com.lopjv.qlhoctap.service;

import com.lopjv.qlhoctap.dto.EnrollmentDto;
import com.lopjv.qlhoctap.entity.Course;
import com.lopjv.qlhoctap.entity.Enrollment;
import com.lopjv.qlhoctap.entity.User;
import com.lopjv.qlhoctap.exception.ResourceNotFoundException;
import com.lopjv.qlhoctap.repository.CourseRepository;
import com.lopjv.qlhoctap.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository, CourseRepository courseRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public EnrollmentDto enrollCourse(Long courseId, User student) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + courseId));

        if (enrollmentRepository.existsByCourseIdAndStudentId(courseId, student.getId())) {
            throw new IllegalArgumentException("Sinh viên đã đăng ký khóa học này rồi.");
        }

        Enrollment enrollment = Enrollment.builder()
                .course(course)
                .student(student)
                .enrolledAt(OffsetDateTime.now())
                .build();

        return mapToDto(enrollmentRepository.save(enrollment));
    }

    @Transactional
    public void unenrollCourse(Long courseId, User student) {
        Enrollment enrollment = enrollmentRepository.findByCourseIdAndStudentId(courseId, student.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Sinh viên chưa đăng ký khóa học này."));
        enrollmentRepository.delete(enrollment);
    }

    public List<EnrollmentDto> getMyEnrollments(User student) {
        return enrollmentRepository.findByStudentId(student.getId()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<EnrollmentDto> getStudentsByCourse(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private EnrollmentDto mapToDto(Enrollment enrollment) {
        return EnrollmentDto.builder()
                .id(enrollment.getId())
                .courseId(enrollment.getCourse().getId())
                .courseCode(enrollment.getCourse().getCode())
                .courseTitle(enrollment.getCourse().getTitle())
                .studentId(enrollment.getStudent().getId())
                .studentName(enrollment.getStudent().getFullName())
                .studentEmail(enrollment.getStudent().getEmail())
                .enrolledAt(enrollment.getEnrolledAt())
                .build();
    }
}
