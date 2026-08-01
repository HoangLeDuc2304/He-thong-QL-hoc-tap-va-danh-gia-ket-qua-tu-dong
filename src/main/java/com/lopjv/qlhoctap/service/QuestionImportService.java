package com.lopjv.qlhoctap.service;

import com.lopjv.qlhoctap.dto.QuestionImportResult;
import com.lopjv.qlhoctap.entity.Course;
import com.lopjv.qlhoctap.entity.Question;
import com.lopjv.qlhoctap.entity.User;
import com.lopjv.qlhoctap.enums.QuestionDifficulty;
import com.lopjv.qlhoctap.repository.CourseRepository;
import com.lopjv.qlhoctap.repository.QuestionRepository;
import com.lopjv.qlhoctap.repository.UserRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class QuestionImportService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionImportService.class);

    private static final int COLUMN_CONTENT = 0;
    private static final int COLUMN_OPTION_A = 1;
    private static final int COLUMN_OPTION_B = 2;
    private static final int COLUMN_OPTION_C = 3;
    private static final int COLUMN_OPTION_D = 4;
    private static final int COLUMN_CORRECT_OPTION = 5;
    private static final int COLUMN_DIFFICULTY = 6;
    private static final int COLUMN_CHAPTER = 7;
    private static final int MINIMUM_COLUMNS = 7;

    private static final Set<String> VALID_OPTIONS = Set.of("A", "B", "C", "D");
    private static final Set<String> VALID_DIFFICULTIES = Set.of("EASY", "MEDIUM", "HARD");

    private final QuestionRepository questionRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public QuestionImportService(
            QuestionRepository questionRepository,
            CourseRepository courseRepository,
            UserRepository userRepository) {
        this.questionRepository = questionRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public QuestionImportResult importQuestionsFromExcel(MultipartFile file, Long courseId) {
        String currentUserEmail = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy người dùng với email: " + currentUserEmail));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy khóa học với ID: " + courseId));

        logger.info("Bắt đầu import câu hỏi từ Excel cho courseId={}, bởi userId={}",
                courseId, currentUser.getId());

        QuestionImportResult importResult = QuestionImportResult.builder()
                .totalRows(0)
                .successCount(0)
                .errorCount(0)
                .errorDetails(new ArrayList<>())
                .build();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNumber = sheet.getLastRowNum();

            logger.info("File Excel có {} dòng (không tính header)", lastRowNumber);

            List<Question> validQuestions = new ArrayList<>();

            for (int rowIndex = 1; rowIndex <= lastRowNumber; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                importResult.setTotalRows(importResult.getTotalRows() + 1);

                if (row == null || isRowEmpty(row)) {
                    importResult.addError(rowIndex + 1, "Dòng rỗng, bỏ qua.");
                    continue;
                }

                int excelRowNumber = rowIndex + 1;

                try {
                    Question question = parseAndValidateRow(row, excelRowNumber, course, currentUser);
                    validQuestions.add(question);
                } catch (IllegalArgumentException validationException) {
                    importResult.addError(excelRowNumber, validationException.getMessage());
                }
            }

            if (!validQuestions.isEmpty()) {
                questionRepository.saveAll(validQuestions);
                importResult.setSuccessCount(validQuestions.size());
                logger.info("Import thành công {} câu hỏi", validQuestions.size());
            }

        } catch (IOException ioException) {
            logger.error("Lỗi đọc file Excel: {}", ioException.getMessage());
            throw new RuntimeException(
                    "Không thể đọc file Excel. Vui lòng kiểm tra định dạng file (.xlsx).",
                    ioException);
        }

        logger.info("Kết quả import: tổng={}, thành công={}, lỗi={}",
                importResult.getTotalRows(),
                importResult.getSuccessCount(),
                importResult.getErrorCount());

        return importResult;
    }

    private Question parseAndValidateRow(Row row, int excelRowNumber,
                                          Course course, User createdBy) {
        if (row.getLastCellNum() < MINIMUM_COLUMNS) {
            throw new IllegalArgumentException(
                    "Không đủ số cột. Yêu cầu tối thiểu 7 cột (nội dung, 4 đáp án, đáp án đúng, độ khó).");
        }

        String content = getCellStringValue(row.getCell(COLUMN_CONTENT));
        String optionA = getCellStringValue(row.getCell(COLUMN_OPTION_A));
        String optionB = getCellStringValue(row.getCell(COLUMN_OPTION_B));
        String optionC = getCellStringValue(row.getCell(COLUMN_OPTION_C));
        String optionD = getCellStringValue(row.getCell(COLUMN_OPTION_D));
        String correctOption = getCellStringValue(row.getCell(COLUMN_CORRECT_OPTION));
        String difficultyString = getCellStringValue(row.getCell(COLUMN_DIFFICULTY));

        String chapter = null;
        if (row.getLastCellNum() > COLUMN_CHAPTER) {
            chapter = getCellStringValue(row.getCell(COLUMN_CHAPTER));
        }

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Nội dung câu hỏi không được để trống.");
        }

        if (optionA == null || optionA.isBlank()) {
            throw new IllegalArgumentException("Đáp án A không được để trống.");
        }

        if (optionB == null || optionB.isBlank()) {
            throw new IllegalArgumentException("Đáp án B không được để trống.");
        }

        if (optionC == null || optionC.isBlank()) {
            throw new IllegalArgumentException("Đáp án C không được để trống.");
        }

        if (optionD == null || optionD.isBlank()) {
            throw new IllegalArgumentException("Đáp án D không được để trống.");
        }

        if (correctOption == null || !VALID_OPTIONS.contains(correctOption.toUpperCase().trim())) {
            throw new IllegalArgumentException(
                    "Đáp án đúng phải là A, B, C hoặc D. Giá trị hiện tại: '" + correctOption + "'.");
        }

        if (difficultyString == null || !VALID_DIFFICULTIES.contains(difficultyString.toUpperCase().trim())) {
            throw new IllegalArgumentException(
                    "Độ khó phải là EASY, MEDIUM hoặc HARD. Giá trị hiện tại: '" + difficultyString + "'.");
        }

        QuestionDifficulty difficulty = QuestionDifficulty.valueOf(
                difficultyString.toUpperCase().trim());

        return Question.builder()
                .course(course)
                .chapter(chapter != null && !chapter.isBlank() ? chapter.trim() : null)
                .content(content.trim())
                .optionA(optionA.trim())
                .optionB(optionB.trim())
                .optionC(optionC.trim())
                .optionD(optionD.trim())
                .correctOption(correctOption.toUpperCase().trim())
                .difficulty(difficulty)
                .createdBy(createdBy)
                .build();
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double numericValue = cell.getNumericCellValue();
                if (numericValue == Math.floor(numericValue)) {
                    yield String.valueOf((long) numericValue);
                }
                yield String.valueOf(numericValue);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case BLANK -> null;
            default -> null;
        };
    }

    private boolean isRowEmpty(Row row) {
        for (int cellIndex = 0; cellIndex < row.getLastCellNum(); cellIndex++) {
            Cell cell = row.getCell(cellIndex);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellStringValue(cell);
                if (value != null && !value.isBlank()) {
                    return false;
                }
            }
        }
        return true;
    }
}
