package com.lopjv.qlhoctap.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lopjv.qlhoctap.dto.CourseMaterialDto;
import com.lopjv.qlhoctap.dto.CreateCourseMaterialRequest;
import com.lopjv.qlhoctap.entity.Course;
import com.lopjv.qlhoctap.entity.CourseMaterial;
import com.lopjv.qlhoctap.entity.User;
import com.lopjv.qlhoctap.exception.ResourceNotFoundException;
import com.lopjv.qlhoctap.repository.CourseMaterialRepository;
import com.lopjv.qlhoctap.repository.CourseRepository;
import com.lopjv.qlhoctap.repository.UserRepository;

@Service
public class CourseMaterialService {

    private final CourseMaterialRepository courseMaterialRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public CourseMaterialService(CourseMaterialRepository courseMaterialRepository,
            CourseRepository courseRepository,
            UserRepository userRepository) {
        this.courseMaterialRepository = courseMaterialRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    /**
     * Lấy tất cả tài liệu đang hoạt động
     */
    public List<CourseMaterialDto> getAllActiveMaterials() {
        return courseMaterialRepository.findAllActive().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả tài liệu của một khóa học
     */
    public List<CourseMaterialDto> getMaterialsByCourseId(Long courseId) {
        courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + courseId));

        return courseMaterialRepository.findByCourseIdAndActive(courseId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả tài liệu của một giảng viên
     */
    public List<CourseMaterialDto> getMaterialsByUploadedBy(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        return courseMaterialRepository.findByUploadedByAndActive(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tài liệu theo ID
     */
    public CourseMaterialDto getMaterialById(Long id) {
        CourseMaterial material = courseMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài liệu với ID: " + id));
        return mapToDto(material);
    }

    /**
     * Tạo tài liệu mới
     */
    @Transactional
    public CourseMaterialDto createMaterial(Long uploadedByUserId, CreateCourseMaterialRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + request.getCourseId()));

        User uploadedBy = userRepository.findById(uploadedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + uploadedByUserId));

        CourseMaterial material = CourseMaterial.builder()
                .course(course)
                .fileName(request.getFileName().trim())
                .fileType(request.getFileType().trim())
                .fileSize(request.getFileSize())
                .filePath(request.getFilePath().trim())
                .description(request.getDescription())
                .uploadedBy(uploadedBy)
                .isActive(true)
                .build();

        return mapToDto(courseMaterialRepository.save(material));
    }

    /**
     * Cập nhật tài liệu
     */
    @Transactional
    public CourseMaterialDto updateMaterial(Long id, CreateCourseMaterialRequest request) {
        CourseMaterial material = courseMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài liệu với ID: " + id));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + request.getCourseId()));

        material.setCourse(course);
        material.setFileName(request.getFileName().trim());
        material.setFileType(request.getFileType().trim());
        material.setFileSize(request.getFileSize());
        material.setFilePath(request.getFilePath().trim());
        material.setDescription(request.getDescription());

        return mapToDto(courseMaterialRepository.save(material));
    }

    /**
     * Xóa tài liệu (soft delete)
     */
    @Transactional
    public void deleteMaterial(Long id) {
        CourseMaterial material = courseMaterialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài liệu với ID: " + id));

        material.setIsActive(false);
        courseMaterialRepository.save(material);
    }

    /**
     * Ánh xạ Entity sang DTO
     */
    private CourseMaterialDto mapToDto(CourseMaterial material) {
        return CourseMaterialDto.builder()
                .id(material.getId())
                .courseId(material.getCourse().getId())
                .courseCode(material.getCourse().getCode())
                .courseName(material.getCourse().getTitle())
                .fileName(material.getFileName())
                .fileType(material.getFileType())
                .fileSize(material.getFileSize())
                .filePath(material.getFilePath())
                .description(material.getDescription())
                .uploadedById(material.getUploadedBy().getId())
                .uploadedByName(material.getUploadedBy().getFullName())
                .uploadedByEmail(material.getUploadedBy().getEmail())
                .createdAt(material.getCreatedAt())
                .updatedAt(material.getUpdatedAt())
                .isActive(material.getIsActive())
                .build();
    }
}
