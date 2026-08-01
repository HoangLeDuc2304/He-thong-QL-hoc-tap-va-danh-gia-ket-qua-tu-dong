package com.lopjv.qlhoctap.service;

import com.lopjv.qlhoctap.dto.ExamResultResponse;
import com.lopjv.qlhoctap.dto.ExamSubmissionRequest;
import com.lopjv.qlhoctap.dto.StudentAnswerDto;
import com.lopjv.qlhoctap.entity.Exam;
import com.lopjv.qlhoctap.entity.ExamQuestion;
import com.lopjv.qlhoctap.entity.ExamResult;
import com.lopjv.qlhoctap.entity.User;
import com.lopjv.qlhoctap.repository.ExamQuestionRepository;
import com.lopjv.qlhoctap.repository.ExamRepository;
import com.lopjv.qlhoctap.repository.ExamResultRepository;
import com.lopjv.qlhoctap.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExamGradingService {

    private static final Logger logger = LoggerFactory.getLogger(ExamGradingService.class);

    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamResultRepository examResultRepository;
    private final UserRepository userRepository;

    public ExamGradingService(
            ExamRepository examRepository,
            ExamQuestionRepository examQuestionRepository,
            ExamResultRepository examResultRepository,
            UserRepository userRepository) {
        this.examRepository = examRepository;
        this.examQuestionRepository = examQuestionRepository;
        this.examResultRepository = examResultRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ExamResultResponse gradeAndSaveResult(ExamSubmissionRequest submissionRequest) {
        String studentEmail = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy sinh viên với email: " + studentEmail));

        logger.info("Sinh viên {} (ID={}) nộp bài thi examId={}",
                student.getFullName(), student.getId(), submissionRequest.getExamId());

        Exam exam = examRepository.findById(submissionRequest.getExamId())
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy đề thi với ID: " + submissionRequest.getExamId()));

        validateSubmissionDeadline(exam);

        validateNotAlreadySubmitted(exam.getId(), student.getId());

        Map<Long, String> correctAnswerMap = buildCorrectAnswerMap(exam.getId());

        int totalQuestions = correctAnswerMap.size();
        int totalCorrect = 0;

        for (StudentAnswerDto studentAnswer : submissionRequest.getAnswers()) {
            String correctOption = correctAnswerMap.get(studentAnswer.getQuestionId());
            if (correctOption != null && correctOption.equals(studentAnswer.getSelectedOption())) {
                totalCorrect++;
            }
        }

        BigDecimal score = calculateScore(totalCorrect, totalQuestions);

        boolean isAutoSubmitted = submissionRequest.getIsAutoSubmitted() != null
                && submissionRequest.getIsAutoSubmitted();

        String answersJson = convertAnswersToJson(submissionRequest.getAnswers());

        ExamResult examResult = ExamResult.builder()
                .exam(exam)
                .student(student)
                .score(score)
                .totalCorrect(totalCorrect)
                .totalQuestions(totalQuestions)
                .answersJson(answersJson)
                .tabSwitchCount(submissionRequest.getTabSwitchCount())
                .isAutoSubmitted(isAutoSubmitted)
                .submittedAt(LocalDateTime.now())
                .build();

        ExamResult savedResult = examResultRepository.save(examResult);

        logger.info("Chấm điểm hoàn tất: studentId={}, examId={}, score={}, đúng={}/{}",
                student.getId(), exam.getId(), score, totalCorrect, totalQuestions);

        String resultMessage = buildResultMessage(score, submissionRequest.getTabSwitchCount(),
                exam.getMaxTabSwitches(), isAutoSubmitted);

        return ExamResultResponse.builder()
                .examResultId(savedResult.getId())
                .examId(exam.getId())
                .examTitle(exam.getExamTitle())
                .score(score)
                .totalCorrect(totalCorrect)
                .totalQuestions(totalQuestions)
                .tabSwitchCount(submissionRequest.getTabSwitchCount())
                .isAutoSubmitted(isAutoSubmitted)
                .submittedAt(savedResult.getSubmittedAt())
                .message(resultMessage)
                .build();
    }

    private void validateSubmissionDeadline(Exam exam) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(exam.getEndTime())) {
            throw new RuntimeException(
                    "Đã quá hạn nộp bài thi. Hạn chót: " + exam.getEndTime());
        }
    }

    private void validateNotAlreadySubmitted(Long examId, Long studentId) {
        if (examResultRepository.existsByExamIdAndStudentId(examId, studentId)) {
            throw new RuntimeException(
                    "Sinh viên đã nộp bài thi này trước đó. Mỗi sinh viên chỉ được nộp 1 lần.");
        }
    }

    private Map<Long, String> buildCorrectAnswerMap(Long examId) {
        List<ExamQuestion> examQuestions = examQuestionRepository.findByExamId(examId);

        if (examQuestions.isEmpty()) {
            throw new RuntimeException(
                    "Đề thi chưa có câu hỏi. Vui lòng liên hệ giáo viên.");
        }

        return examQuestions.stream()
                .collect(Collectors.toMap(
                        examQuestion -> examQuestion.getQuestion().getId(),
                        examQuestion -> examQuestion.getQuestion().getCorrectOption()
                ));
    }

    private BigDecimal calculateScore(int totalCorrect, int totalQuestions) {
        if (totalQuestions == 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(totalCorrect)
                .multiply(BigDecimal.TEN)
                .divide(BigDecimal.valueOf(totalQuestions), 2, RoundingMode.HALF_UP);
    }

    private String convertAnswersToJson(List<StudentAnswerDto> answers) {
        StringBuilder jsonBuilder = new StringBuilder("[");
        for (int index = 0; index < answers.size(); index++) {
            StudentAnswerDto answer = answers.get(index);
            jsonBuilder.append(String.format(
                    "{\"questionId\":%d,\"selectedOption\":\"%s\"}",
                    answer.getQuestionId(),
                    answer.getSelectedOption()));
            if (index < answers.size() - 1) {
                jsonBuilder.append(",");
            }
        }
        jsonBuilder.append("]");
        return jsonBuilder.toString();
    }

    private String buildResultMessage(BigDecimal score, int tabSwitchCount,
                                       int maxTabSwitches, boolean isAutoSubmitted) {
        StringBuilder message = new StringBuilder();
        message.append(String.format("Điểm của bạn: %s/10. ", score));

        if (isAutoSubmitted) {
            message.append("Bài thi đã được hệ thống tự động nộp. ");
        }

        if (tabSwitchCount > 0) {
            message.append(String.format("Số lần rời khỏi tab: %d/%d. ",
                    tabSwitchCount, maxTabSwitches));
        }

        if (tabSwitchCount >= maxTabSwitches) {
            message.append("⚠️ CẢNH BÁO: Bạn đã vượt quá số lần chuyển tab cho phép.");
        }

        return message.toString().trim();
    }
}
