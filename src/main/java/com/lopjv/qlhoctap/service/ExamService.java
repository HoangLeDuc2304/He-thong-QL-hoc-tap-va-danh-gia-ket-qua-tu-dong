package com.lopjv.qlhoctap.service;

import com.lopjv.qlhoctap.dto.ExamMatrixConfigDto;
import com.lopjv.qlhoctap.entity.Exam;
import com.lopjv.qlhoctap.entity.ExamConfiguration;
import com.lopjv.qlhoctap.entity.ExamQuestion;
import com.lopjv.qlhoctap.entity.Question;
import com.lopjv.qlhoctap.exception.InsufficientQuestionsException;
import com.lopjv.qlhoctap.exception.ResourceNotFoundException;
import com.lopjv.qlhoctap.repository.ExamConfigurationRepository;
import com.lopjv.qlhoctap.repository.ExamQuestionRepository;
import com.lopjv.qlhoctap.repository.ExamRepository;
import com.lopjv.qlhoctap.repository.QuestionRepository;
import com.lopjv.qlhoctap.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final SubjectRepository subjectRepository;
    private final QuestionRepository questionRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamConfigurationRepository examConfigurationRepository;

    public ExamService(
            ExamRepository examRepository,
            SubjectRepository subjectRepository,
            QuestionRepository questionRepository,
            ExamQuestionRepository examQuestionRepository,
            ExamConfigurationRepository examConfigurationRepository) {
        this.examRepository = examRepository;
        this.subjectRepository = subjectRepository;
        this.questionRepository = questionRepository;
        this.examQuestionRepository = examQuestionRepository;
        this.examConfigurationRepository = examConfigurationRepository;
    }

    @Transactional
    public List<ExamQuestion> generateExamFromMatrix(ExamMatrixConfigDto configDto) {
        Exam exam = examRepository.findById(configDto.getExamId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề thi với ID: " + configDto.getExamId()));

        if (!subjectRepository.existsById(configDto.getSubjectId())) {
            throw new ResourceNotFoundException("Không tìm thấy môn học với ID: " + configDto.getSubjectId());
        }

        Long subjectId = configDto.getSubjectId();
        String chapterTopic = configDto.getChapterTopic();

        int easyNeeded = configDto.getEasyCount() != null ? configDto.getEasyCount() : 0;
        int mediumNeeded = configDto.getMediumCount() != null ? configDto.getMediumCount() : 0;
        int hardNeeded = configDto.getHardCount() != null ? configDto.getHardCount() : 0;

        // 1. Kiểm tra số lượng câu hỏi khả dụng trong Database
        validateQuestionAvailability(subjectId, chapterTopic, "EASY", easyNeeded);
        validateQuestionAvailability(subjectId, chapterTopic, "MEDIUM", mediumNeeded);
        validateQuestionAvailability(subjectId, chapterTopic, "HARD", hardNeeded);

        // 2. Lấy danh sách câu hỏi ngẫu nhiên theo từng độ khó từ PostgreSQL
        List<Question> easyQuestions = fetchRandomQuestions(subjectId, chapterTopic, "EASY", easyNeeded);
        List<Question> mediumQuestions = fetchRandomQuestions(subjectId, chapterTopic, "MEDIUM", mediumNeeded);
        List<Question> hardQuestions = fetchRandomQuestions(subjectId, chapterTopic, "HARD", hardNeeded);

        // 3. Tổng hợp và trộn ngẫu nhiên thứ tự danh sách tất cả các câu hỏi
        List<Question> selectedQuestions = new ArrayList<>();
        selectedQuestions.addAll(easyQuestions);
        selectedQuestions.addAll(mediumQuestions);
        selectedQuestions.addAll(hardQuestions);

        Collections.shuffle(selectedQuestions);

        // 4. Lưu Ma trận cấu hình tạo đề (ExamConfiguration)
        saveExamConfigurations(exam, chapterTopic, easyNeeded, mediumNeeded, hardNeeded);

        // 5. Lưu Snapshot danh sách câu hỏi cố định của đề thi (ExamQuestion) với order_index từ 1 đến N
        List<ExamQuestion> examQuestions = new ArrayList<>();
        for (int index = 0; index < selectedQuestions.size(); index++) {
            Question question = selectedQuestions.get(index);
            ExamQuestion.ExamQuestionId examQuestionId = new ExamQuestion.ExamQuestionId(exam.getId(), question.getId());

            ExamQuestion examQuestion = ExamQuestion.builder()
                    .id(examQuestionId)
                    .exam(exam)
                    .question(question)
                    .orderIndex(index + 1)
                    .build();

            examQuestions.add(examQuestion);
        }

        List<ExamQuestion> savedExamQuestions = examQuestionRepository.saveAll(examQuestions);

        // 6. Cập nhật trạng thái đề thi thành PUBLISHED nếu trộn đề thành công
        exam.setStatus("PUBLISHED");
        examRepository.save(exam);

        return savedExamQuestions;
    }

    private void validateQuestionAvailability(Long subjectId, String chapterTopic, String difficulty, int countNeeded) {
        if (countNeeded <= 0) {
            return;
        }

        long availableCount;
        if (chapterTopic != null && !chapterTopic.trim().isEmpty()) {
            availableCount = questionRepository.countBySubjectIdAndChapterTopicAndDifficulty(subjectId, chapterTopic, difficulty);
        } else {
            availableCount = questionRepository.countBySubjectIdAndDifficulty(subjectId, difficulty);
        }

        if (availableCount < countNeeded) {
            String topicInfo = (chapterTopic != null && !chapterTopic.trim().isEmpty()) ? " thuộc chủ đề '" + chapterTopic + "'" : "";
            throw new InsufficientQuestionsException(
                    String.format("Không đủ câu hỏi độ khó %s%s trong Ngân hàng! Yêu cầu: %d câu, khả dụng: %d câu.",
                            difficulty, topicInfo, countNeeded, availableCount)
            );
        }
    }

    private List<Question> fetchRandomQuestions(Long subjectId, String chapterTopic, String difficulty, int countNeeded) {
        if (countNeeded <= 0) {
            return Collections.emptyList();
        }

        if (chapterTopic != null && !chapterTopic.trim().isEmpty()) {
            return questionRepository.findRandomBySubjectIdAndChapterAndDifficulty(subjectId, chapterTopic, difficulty, countNeeded);
        } else {
            return questionRepository.findRandomBySubjectIdAndDifficulty(subjectId, difficulty, countNeeded);
        }
    }

    private void saveExamConfigurations(Exam exam, String chapterTopic, int easyNeeded, int mediumNeeded, int hardNeeded) {
        if (easyNeeded > 0) {
            examConfigurationRepository.save(ExamConfiguration.builder()
                    .exam(exam)
                    .chapterTopic(chapterTopic)
                    .difficulty("EASY")
                    .questionCount(easyNeeded)
                    .build());
        }
        if (mediumNeeded > 0) {
            examConfigurationRepository.save(ExamConfiguration.builder()
                    .exam(exam)
                    .chapterTopic(chapterTopic)
                    .difficulty("MEDIUM")
                    .questionCount(mediumNeeded)
                    .build());
        }
        if (hardNeeded > 0) {
            examConfigurationRepository.save(ExamConfiguration.builder()
                    .exam(exam)
                    .chapterTopic(chapterTopic)
                    .difficulty("HARD")
                    .questionCount(hardNeeded)
                    .build());
        }
    }
}
