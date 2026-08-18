package com.lopjv.qlhoctap.service;

import com.lopjv.qlhoctap.dto.CreateSubjectRequest;
import com.lopjv.qlhoctap.dto.SubjectDto;
import com.lopjv.qlhoctap.entity.Course;
import com.lopjv.qlhoctap.entity.Subject;
import com.lopjv.qlhoctap.entity.User;
import com.lopjv.qlhoctap.exception.ResourceNotFoundException;
import com.lopjv.qlhoctap.repository.CourseRepository;
import com.lopjv.qlhoctap.repository.SubjectRepository;
import com.lopjv.qlhoctap.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public SubjectService(SubjectRepository subjectRepository,
                          CourseRepository courseRepository,
                          UserRepository userRepository) {
        this.subjectRepository = subjectRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    public List<SubjectDto> getSubjectsByCourse(Long courseId) {
        return subjectRepository.findByCourseIdOrderByOrderIndex(courseId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public SubjectDto getSubjectById(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học với ID: " + id));
        return mapToDto(subject);
    }

    @Transactional
    public SubjectDto createSubject(CreateSubjectRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + request.getCourseId()));

        User teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên với ID: " + request.getTeacherId()));

        if (subjectRepository.findByCourseIdOrderByOrderIndex(course.getId()).stream()
                .anyMatch(s -> s.getCode().equalsIgnoreCase(request.getCode().trim()))) {
            throw new IllegalArgumentException("Mã môn học đã tồn tại trong khóa học này: " + request.getCode());
        }

        Subject subject = Subject.builder()
                .course(course)
                .teacher(teacher)
                .code(request.getCode().trim())
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .orderIndex(request.getOrderIndex() == null ? 1 : request.getOrderIndex())
                .build();

        return mapToDto(subjectRepository.save(subject));
    }

    @Transactional
    public SubjectDto updateSubject(Long id, CreateSubjectRequest request) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học với ID: " + id));

        if (request.getTeacherId() != null) {
            User teacher = userRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên với ID: " + request.getTeacherId()));
            subject.setTeacher(teacher);
        }

        if (request.getCourseId() != null) {
            Course course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + request.getCourseId()));
            subject.setCourse(course);
        }

        subject.setCode(request.getCode().trim());
        subject.setTitle(request.getTitle().trim());
        subject.setDescription(request.getDescription());
        subject.setOrderIndex(request.getOrderIndex() == null ? subject.getOrderIndex() : request.getOrderIndex());

        return mapToDto(subjectRepository.save(subject));
    }

    @Transactional
    public void deleteSubject(Long id) {
        if (!subjectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy môn học với ID: " + id);
        }
        subjectRepository.deleteById(id);
    }

    private SubjectDto mapToDto(Subject subject) {
        return SubjectDto.builder()
                .id(subject.getId())
                .courseId(subject.getCourse() != null ? subject.getCourse().getId() : null)
                .courseTitle(subject.getCourse() != null ? subject.getCourse().getTitle() : null)
                .code(subject.getCode())
                .title(subject.getTitle())
                .description(subject.getDescription())
                .teacherId(subject.getTeacher() != null ? subject.getTeacher().getId() : null)
                .teacherName(subject.getTeacher() != null ? subject.getTeacher().getFullName() : null)
                .orderIndex(subject.getOrderIndex())
                .build();
    }
}
