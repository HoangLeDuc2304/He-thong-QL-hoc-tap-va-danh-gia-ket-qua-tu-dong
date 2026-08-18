package com.lopjv.qlhoctap.service;

import com.lopjv.qlhoctap.dto.ExamResultResponseDto;
import com.lopjv.qlhoctap.dto.StudentQuestionAnswerDto;
import com.lopjv.qlhoctap.dto.SubmitExamRequestDto;
import com.lopjv.qlhoctap.entity.Exam;
import com.lopjv.qlhoctap.entity.ExamQuestion;
import com.lopjv.qlhoctap.entity.Question;
import com.lopjv.qlhoctap.entity.QuestionOption;
import com.lopjv.qlhoctap.entity.StudentAnswer;
import com.lopjv.qlhoctap.entity.StudentAnswerOption;
import com.lopjv.qlhoctap.entity.StudentExam;
import com.lopjv.qlhoctap.entity.User;
import com.lopjv.qlhoctap.exception.ResourceNotFoundException;
import com.lopjv.qlhoctap.repository.ExamQuestionRepository;
import com.lopjv.qlhoctap.repository.ExamRepository;
import com.lopjv.qlhoctap.repository.QuestionOptionRepository;
import com.lopjv.qlhoctap.repository.QuestionRepository;
import com.lopjv.qlhoctap.repository.StudentAnswerOptionRepository;
import com.lopjv.qlhoctap.repository.StudentAnswerRepository;
import com.lopjv.qlhoctap.repository.StudentExamRepository;
import com.lopjv.qlhoctap.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AssessmentService {

    private final ExamRepository examRepository;
    private final UserRepository userRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final StudentExamRepository studentExamRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final StudentAnswerOptionRepository studentAnswerOptionRepository;

    public AssessmentService(
            ExamRepository examRepository,
            UserRepository userRepository,
            ExamQuestionRepository examQuestionRepository,
            QuestionRepository questionRepository,
            QuestionOptionRepository questionOptionRepository,
            StudentExamRepository studentExamRepository,
            StudentAnswerRepository studentAnswerRepository,
            StudentAnswerOptionRepository studentAnswerOptionRepository) {
        this.examRepository = examRepository;
        this.userRepository = userRepository;
        this.examQuestionRepository = examQuestionRepository;
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.studentExamRepository = studentExamRepository;
        this.studentAnswerRepository = studentAnswerRepository;
        this.studentAnswerOptionRepository = studentAnswerOptionRepository;
    }

    /**
     * Sinh viên bắt đầu làm bài thi — tạo bản ghi StudentExam. Kiểm tra: đề thi
     * đã PUBLISHED, trong thời gian thi, chưa thi lần nào.
     */
    @Transactional
    public StudentExam startExam(Long examId, User student) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề thi với ID: " + examId));

        if (!"PUBLISHED".equals(exam.getStatus())) {
            throw new IllegalArgumentException("Đề thi chưa được mở hoặc đã kết thúc.");
        }

        OffsetDateTime now = OffsetDateTime.now();
        if (now.isBefore(exam.getStartTime())) {
            throw new IllegalArgumentException("Chưa đến thời gian thi. Bắt đầu lúc: " + exam.getStartTime());
        }
        if (now.isAfter(exam.getEndTime())) {
            throw new IllegalArgumentException("Đề thi đã hết thời gian.");
        }

        // Kiểm tra sinh viên chưa thi lần nào
        studentExamRepository.findByExamIdAndStudentId(examId, student.getId())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Bạn đã vào phòng thi này rồi.");
                });

        StudentExam studentExam = StudentExam.builder()
                .exam(exam)
                .student(student)
                .startTime(now)
                .status("IN_PROGRESS")
                .build();

        return studentExamRepository.save(studentExam);
    }

    /**
     * Ghi nhận sự kiện chuyển tab của sinh viên (chống gian lận).
     */
    @Transactional
    public StudentExam recordTabSwitch(Long examId, User student) {
        StudentExam studentExam = studentExamRepository.findByExamIdAndStudentId(examId, student.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Bạn chưa vào phòng thi này."));

        if (!"IN_PROGRESS".equals(studentExam.getStatus())) {
            throw new IllegalArgumentException("Bài thi đã kết thúc, không thể ghi nhận thêm.");
        }

        studentExam.setTabSwitchCount(studentExam.getTabSwitchCount() + 1);
        return studentExamRepository.save(studentExam);
    }

    @Transactional
    public ExamResultResponseDto submitAndGradeExam(SubmitExamRequestDto requestDto) {
        Exam exam = examRepository.findById(requestDto.getExamId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề thi với ID: " + requestDto.getExamId()));

        User student = userRepository.findById(requestDto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên với ID: " + requestDto.getStudentId()));

        OffsetDateTime now = OffsetDateTime.now();
        int tabSwitchCount = requestDto.getTabSwitchCount() != null ? requestDto.getTabSwitchCount() : 0;

        // 1. Kiểm tra Anti-cheat và thời hạn nộp bài
        String status = "SUBMITTED";
        StringBuilder noteBuilder = new StringBuilder();

        if (now.isAfter(exam.getEndTime())) {
            status = "AUTO_SUBMITTED";
            noteBuilder.append("Bài thi được nộp tự động do quá thời hạn quy định. ");
        }

        if (tabSwitchCount > exam.getMaxTabSwitches()) {
            status = "AUTO_SUBMITTED";
            noteBuilder.append(String.format("Phát hiện gian lận: Chuyển tab %d/%d lần vượt ngưỡng cho phép! ",
                    tabSwitchCount, exam.getMaxTabSwitches()));
        }

        // 2. Lấy danh sách câu hỏi trong đề thi (Snapshot từ exam_questions)
        List<ExamQuestion> examQuestions = examQuestionRepository.findByExamIdOrderByOrderIndex(exam.getId());
        if (examQuestions.isEmpty()) {
            throw new IllegalStateException("Đề thi chưa được khởi tạo danh sách câu hỏi!");
        }

        int totalQuestions = examQuestions.size();
        BigDecimal scorePerQuestion = BigDecimal.TEN.divide(BigDecimal.valueOf(totalQuestions), 4, RoundingMode.HALF_UP);

        // Map câu trả lời của sinh viên từ DTO request (questionId -> StudentQuestionAnswerDto)
        Map<Long, StudentQuestionAnswerDto> studentAnswerMap = requestDto.getAnswers() != null
                ? requestDto.getAnswers().stream()
                        .filter(a -> a.getQuestionId() != null)
                        .collect(Collectors.toMap(StudentQuestionAnswerDto::getQuestionId, Function.identity(), (existing, replacement) -> existing))
                : Collections.emptyMap();

        // 3. Tạo/Cập nhật bản ghi phiếu làm bài StudentExam
        StudentExam studentExam = studentExamRepository.findByExamIdAndStudentId(exam.getId(), student.getId())
                .orElseGet(() -> StudentExam.builder()
                .exam(exam)
                .student(student)
                .startTime(now.minusMinutes(exam.getDurationMinutes()))
                .build());

        studentExam.setSubmitTime(now);
        studentExam.setTabSwitchCount(tabSwitchCount);
        studentExam.setStatus(status);

        studentExam = studentExamRepository.save(studentExam);

        // 4. Tiến hành đối chiếu đáp án và chấm điểm từng câu hỏi
        BigDecimal totalScore = BigDecimal.ZERO;
        int correctCount = 0;

        List<StudentAnswer> studentAnswersToSave = new ArrayList<>();
        Map<Long, List<QuestionOption>> answerOptionsToSaveMap = new java.util.HashMap<>();

        for (ExamQuestion eq : examQuestions) {
            Question question = eq.getQuestion();
            Long questionId = question.getId();

            // Lấy danh sách đáp án đúng từ DB
            List<QuestionOption> allOptions = questionOptionRepository.findByQuestionId(questionId);
            Set<Long> correctOptionIds = allOptions.stream()
                    .filter(QuestionOption::getIsCorrect)
                    .map(QuestionOption::getId)
                    .collect(Collectors.toSet());

            // Lấy đáp án sinh viên đã chọn từ Request DTO
            StudentQuestionAnswerDto studentAnswerDto = studentAnswerMap.get(questionId);
            List<Long> selectedOptionIdsList = (studentAnswerDto != null && studentAnswerDto.getSelectedOptionIds() != null)
                    ? studentAnswerDto.getSelectedOptionIds()
                    : Collections.emptyList();

            Set<Long> selectedOptionIds = new HashSet<>(selectedOptionIdsList);

            // Logic chấm điểm: Single Choice vs Multiple Choice
            boolean isCorrect = evaluateQuestionAnswer(question.getQuestionType(), correctOptionIds, selectedOptionIds);

            System.out.println("[GRADE-DEBUG] questionId=" + questionId
                    + " type=" + question.getQuestionType()
                    + " correctOptionIds=" + correctOptionIds
                    + " selectedOptionIds=" + selectedOptionIds
                    + " isCorrect=" + isCorrect);

            BigDecimal scoreGiven = BigDecimal.ZERO;
            if (isCorrect) {
                scoreGiven = scorePerQuestion;
                totalScore = totalScore.add(scoreGiven);
                correctCount++;
            }

            // make final copies for use inside lambdas/builder
            final boolean isCorrectFinal = isCorrect;
            final BigDecimal scoreGivenFinal = scoreGiven.setScale(2, RoundingMode.HALF_UP);
            final StudentExam studentExamFinal = studentExam;

            // Update existing StudentAnswer if present, otherwise create new
            StudentAnswer studentAnswer = studentAnswerRepository
                    .findByStudentExamIdAndQuestionId(studentExamFinal.getId(), questionId)
                    .map(existing -> {
                        existing.setIsCorrect(isCorrectFinal);
                        existing.setScoreGiven(scoreGivenFinal);
                        existing.setUpdatedAt(now);
                        return existing;
                    })
                    .orElseGet(() -> StudentAnswer.builder()
                    .studentExam(studentExamFinal)
                    .question(question)
                    .isCorrect(isCorrectFinal)
                    .scoreGiven(scoreGivenFinal)
                    .updatedAt(now)
                    .build());

            studentAnswersToSave.add(studentAnswer);

            // Thu thập các QuestionOption thực tế mà sinh viên đã chọn để lưu vào student_answer_options
            Map<Long, QuestionOption> optionMap = allOptions.stream()
                    .collect(Collectors.toMap(QuestionOption::getId, Function.identity()));

            List<QuestionOption> selectedQuestionOptions = selectedOptionIds.stream()
                    .map(optionMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            answerOptionsToSaveMap.put(questionId, selectedQuestionOptions);
        }

        // 5. Lưu điểm tổng cộng vào phiếu làm bài student_exams
        BigDecimal finalScore = totalScore.setScale(2, RoundingMode.HALF_UP);
        if (finalScore.compareTo(BigDecimal.TEN) > 0) {
            finalScore = BigDecimal.TEN.setScale(2, RoundingMode.HALF_UP);
        }
        studentExam.setScore(finalScore);
        studentExamRepository.save(studentExam);

        // 6. Lưu chi tiết student_answers và student_answer_options
        // Save or update student answers
        List<StudentAnswer> savedAnswers = studentAnswerRepository.saveAll(studentAnswersToSave);

        for (StudentAnswer savedAnswer : savedAnswers) {
            // Remove any previously stored selected options for this answer to avoid duplicates
            List<StudentAnswerOption> existingOptions = studentAnswerOptionRepository.findByStudentAnswerId(savedAnswer.getId());
            if (existingOptions != null && !existingOptions.isEmpty()) {
                studentAnswerOptionRepository.deleteAll(existingOptions);
            }

            List<QuestionOption> selectedOptions = answerOptionsToSaveMap.get(savedAnswer.getQuestion().getId());
            if (selectedOptions != null && !selectedOptions.isEmpty()) {
                for (QuestionOption option : selectedOptions) {
                    StudentAnswerOption.StudentAnswerOptionId optionId
                            = new StudentAnswerOption.StudentAnswerOptionId(savedAnswer.getId(), option.getId());

                    StudentAnswerOption answerOption = StudentAnswerOption.builder()
                            .id(optionId)
                            .studentAnswer(savedAnswer)
                            .option(option)
                            .build();

                    studentAnswerOptionRepository.save(answerOption);
                }
            }
        }

        return ExamResultResponseDto.builder()
                .studentExamId(studentExam.getId())
                .examId(exam.getId())
                .studentId(student.getId())
                .examTitle(exam.getTitle())
                .score(finalScore)
                .totalQuestions(totalQuestions)
                .correctCount(correctCount)
                .tabSwitchCount(tabSwitchCount)
                .status(status)
                .submitTime(now)
                .note(noteBuilder.toString().trim())
                .build();
    }

    private boolean evaluateQuestionAnswer(String questionType, Set<Long> correctOptionIds, Set<Long> selectedOptionIds) {
        if (selectedOptionIds.isEmpty() || correctOptionIds.isEmpty()) {
            return false;
        }

        if ("SINGLE_CHOICE".equalsIgnoreCase(questionType)) {
            // Đúng khi chọn duy nhất 1 đáp án và đáp án đó nằm trong tập đáp án đúng
            return selectedOptionIds.size() == 1 && correctOptionIds.equals(selectedOptionIds);
        } else if ("MULTIPLE_CHOICE".equalsIgnoreCase(questionType)) {
            // Đúng khi tập lựa chọn của sinh viên trùng khớp 100% với tập đáp án đúng (không thừa, không thiếu)
            return correctOptionIds.equals(selectedOptionIds);
        }

        return false;
    }
}
