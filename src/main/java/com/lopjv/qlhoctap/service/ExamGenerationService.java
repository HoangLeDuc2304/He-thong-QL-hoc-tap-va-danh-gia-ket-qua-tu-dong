package com.lopjv.qlhoctap.service;

import com.lopjv.qlhoctap.dto.ExamGenerationRequest;
import com.lopjv.qlhoctap.entity.Exam;
import com.lopjv.qlhoctap.entity.ExamQuestion;
import com.lopjv.qlhoctap.entity.Question;
import com.lopjv.qlhoctap.enums.QuestionDifficulty;
import com.lopjv.qlhoctap.repository.ExamQuestionRepository;
import com.lopjv.qlhoctap.repository.ExamRepository;
import com.lopjv.qlhoctap.repository.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExamGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(ExamGenerationService.class);

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final ExamQuestionRepository examQuestionRepository;

    public ExamGenerationService(
            ExamRepository examRepository,
            QuestionRepository questionRepository,
            ExamQuestionRepository examQuestionRepository) {
        this.examRepository = examRepository;
        this.questionRepository = questionRepository;
        this.examQuestionRepository = examQuestionRepository;
    }

    @Transactional
    public List<ExamQuestion> generateExamQuestions(ExamGenerationRequest request) {
        logger.info("Bắt đầu trộn đề cho examId={}, courseId={}, chapter={}",
                request.getExamId(), request.getCourseId(), request.getChapter());

        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy đề thi với ID: " + request.getExamId()));

        validateQuestionBankAvailability(request);

        examQuestionRepository.deleteByExamId(exam.getId());
        logger.info("Đã xóa đề thi cũ (nếu có) cho examId={}", exam.getId());

        List<Question> allSelectedQuestions = new ArrayList<>();

        if (request.getEasyCount() > 0) {
            List<Question> easyQuestions = questionRepository
                    .findRandomQuestionsByCourseAndChapterAndDifficulty(
                            request.getCourseId(),
                            request.getChapter(),
                            QuestionDifficulty.EASY.name(),
                            request.getEasyCount());
            allSelectedQuestions.addAll(easyQuestions);
            logger.info("Đã lấy {} câu hỏi DỄ", easyQuestions.size());
        }

        if (request.getMediumCount() > 0) {
            List<Question> mediumQuestions = questionRepository
                    .findRandomQuestionsByCourseAndChapterAndDifficulty(
                            request.getCourseId(),
                            request.getChapter(),
                            QuestionDifficulty.MEDIUM.name(),
                            request.getMediumCount());
            allSelectedQuestions.addAll(mediumQuestions);
            logger.info("Đã lấy {} câu hỏi TRUNG BÌNH", mediumQuestions.size());
        }

        if (request.getHardCount() > 0) {
            List<Question> hardQuestions = questionRepository
                    .findRandomQuestionsByCourseAndChapterAndDifficulty(
                            request.getCourseId(),
                            request.getChapter(),
                            QuestionDifficulty.HARD.name(),
                            request.getHardCount());
            allSelectedQuestions.addAll(hardQuestions);
            logger.info("Đã lấy {} câu hỏi KHÓ", hardQuestions.size());
        }

        List<ExamQuestion> examQuestions = new ArrayList<>();
        for (int index = 0; index < allSelectedQuestions.size(); index++) {
            ExamQuestion examQuestion = ExamQuestion.builder()
                    .exam(exam)
                    .question(allSelectedQuestions.get(index))
                    .questionOrder(index + 1)
                    .build();
            examQuestions.add(examQuestion);
        }

        List<ExamQuestion> savedExamQuestions = examQuestionRepository.saveAll(examQuestions);

        logger.info("Trộn đề thành công cho examId={}. Tổng số câu hỏi: {}",
                exam.getId(), savedExamQuestions.size());

        return savedExamQuestions;
    }

    /**
     * Kiểm tra ngân hàng đề có đủ câu hỏi cho từng mức độ khó hay không.
     *
     * Nếu số câu hỏi hiện có trong ngân hàng ít hơn số lượng yêu cầu,
     * ném RuntimeException kèm thông báo chi tiết mức độ nào thiếu bao nhiêu câu.
     *
     * @param request DTO chứa thông tin trộn đề
     * @throws RuntimeException nếu ngân hàng đề không đủ câu hỏi
     */
    private void validateQuestionBankAvailability(ExamGenerationRequest request) {
        List<String> insufficientMessages = new ArrayList<>();

        if (request.getEasyCount() > 0) {
            long availableEasy = questionRepository.countByCourseIdAndChapterAndDifficulty(
                    request.getCourseId(), request.getChapter(), QuestionDifficulty.EASY);
            if (availableEasy < request.getEasyCount()) {
                insufficientMessages.add(
                        String.format("Câu hỏi DỄ: cần %d, hiện có %d",
                                request.getEasyCount(), availableEasy));
            }
        }

        if (request.getMediumCount() > 0) {
            long availableMedium = questionRepository.countByCourseIdAndChapterAndDifficulty(
                    request.getCourseId(), request.getChapter(), QuestionDifficulty.MEDIUM);
            if (availableMedium < request.getMediumCount()) {
                insufficientMessages.add(
                        String.format("Câu hỏi TRUNG BÌNH: cần %d, hiện có %d",
                                request.getMediumCount(), availableMedium));
            }
        }

        if (request.getHardCount() > 0) {
            long availableHard = questionRepository.countByCourseIdAndChapterAndDifficulty(
                    request.getCourseId(), request.getChapter(), QuestionDifficulty.HARD);
            if (availableHard < request.getHardCount()) {
                insufficientMessages.add(
                        String.format("Câu hỏi KHÓ: cần %d, hiện có %d",
                                request.getHardCount(), availableHard));
            }
        }

        if (!insufficientMessages.isEmpty()) {
            String errorMessage = "Ngân hàng câu hỏi không đủ cho chương '"
                    + request.getChapter() + "': " + String.join("; ", insufficientMessages);
            logger.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }
}
