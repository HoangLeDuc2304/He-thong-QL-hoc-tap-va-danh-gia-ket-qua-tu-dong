package com.lopjv.qlhoctap.service;

import com.lopjv.qlhoctap.dto.LessonDto;
import com.lopjv.qlhoctap.entity.Lesson;
import com.lopjv.qlhoctap.entity.Subject;
import com.lopjv.qlhoctap.exception.ResourceNotFoundException;
import com.lopjv.qlhoctap.repository.LessonRepository;
import com.lopjv.qlhoctap.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;
    private final SubjectRepository subjectRepository;

    public LessonService(LessonRepository lessonRepository, SubjectRepository subjectRepository) {
        this.lessonRepository = lessonRepository;
        this.subjectRepository = subjectRepository;
    }

    public List<LessonDto> getLessonsBySubject(Long subjectId) {
        return lessonRepository.findBySubjectIdOrderByOrderIndex(subjectId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public LessonDto getLessonById(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài học với ID: " + id));
        return mapToDto(lesson);
    }

    @Transactional
    public LessonDto createLesson(Long subjectId, LessonDto dto) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học với ID: " + subjectId));

        Lesson lesson = Lesson.builder()
                .subject(subject)
                .title(dto.getTitle().trim())
                .content(dto.getContent())
                .videoUrl(dto.getVideoUrl())
                .attachmentUrl(dto.getAttachmentUrl())
                .orderIndex(dto.getOrderIndex() == null ? 1 : dto.getOrderIndex())
                .build();

        return mapToDto(lessonRepository.save(lesson));
    }

    @Transactional
    public LessonDto updateLesson(Long id, LessonDto dto) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài học với ID: " + id));

        lesson.setTitle(dto.getTitle().trim());
        lesson.setContent(dto.getContent());
        lesson.setVideoUrl(dto.getVideoUrl());
        lesson.setAttachmentUrl(dto.getAttachmentUrl());
        if (dto.getOrderIndex() != null) {
            lesson.setOrderIndex(dto.getOrderIndex());
        }

        return mapToDto(lessonRepository.save(lesson));
    }

    @Transactional
    public void deleteLesson(Long id) {
        if (!lessonRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy bài học với ID: " + id);
        }
        lessonRepository.deleteById(id);
    }

    private LessonDto mapToDto(Lesson lesson) {
        return LessonDto.builder()
                .id(lesson.getId())
                .subjectId(lesson.getSubject() != null ? lesson.getSubject().getId() : null)
                .title(lesson.getTitle())
                .content(lesson.getContent())
                .videoUrl(lesson.getVideoUrl())
                .attachmentUrl(lesson.getAttachmentUrl())
                .orderIndex(lesson.getOrderIndex())
                .build();
    }
}
