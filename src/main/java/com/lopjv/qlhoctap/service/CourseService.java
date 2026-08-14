package com.lopjv.qlhoctap.service;

import com.lopjv.qlhoctap.dto.CourseDto;
import com.lopjv.qlhoctap.dto.CreateCourseRequest;
import com.lopjv.qlhoctap.entity.Course;
import com.lopjv.qlhoctap.entity.User;
import com.lopjv.qlhoctap.exception.ResourceNotFoundException;
import com.lopjv.qlhoctap.repository.CourseRepository;
import com.lopjv.qlhoctap.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public CourseService(CourseRepository courseRepository, UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    public List<CourseDto> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public CourseDto getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + id));
        return mapToDto(course);
    }

    @Transactional
    public CourseDto createCourse(Long createdByUserId, CreateCourseRequest request) {
        User createdBy = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + createdByUserId));

        if (courseRepository.findAll().stream().anyMatch(c -> c.getCode().equalsIgnoreCase(request.getCode()))) {
            throw new IllegalArgumentException("Mã khóa học đã tồn tại: " + request.getCode());
        }

        Course course = Course.builder()
                .code(request.getCode().trim())
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .createdBy(createdBy)
                .build();

        return mapToDto(courseRepository.save(course));
    }

    @Transactional
    public CourseDto updateCourse(Long id, CreateCourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + id));

        course.setCode(request.getCode().trim());
        course.setTitle(request.getTitle().trim());
        course.setDescription(request.getDescription());

        return mapToDto(courseRepository.save(course));
    }

    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + id);
        }
        courseRepository.deleteById(id);
    }

    private CourseDto mapToDto(Course course) {
        return CourseDto.builder()
                .id(course.getId())
                .code(course.getCode())
                .title(course.getTitle())
                .description(course.getDescription())
                .createdById(course.getCreatedBy() != null ? course.getCreatedBy().getId() : null)
                .createdByUsername(course.getCreatedBy() != null ? course.getCreatedBy().getUsername() : null)
                .build();
    }
}
