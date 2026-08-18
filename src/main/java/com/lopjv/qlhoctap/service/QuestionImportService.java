package com.lopjv.qlhoctap.service;

import com.lopjv.qlhoctap.dto.QuestionImportResultDto;
import com.lopjv.qlhoctap.entity.Question;
import com.lopjv.qlhoctap.entity.QuestionOption;
import com.lopjv.qlhoctap.entity.Subject;
import com.lopjv.qlhoctap.entity.User;
import com.lopjv.qlhoctap.exception.ResourceNotFoundException;
import com.lopjv.qlhoctap.repository.QuestionOptionRepository;
import com.lopjv.qlhoctap.repository.QuestionRepository;
import com.lopjv.qlhoctap.repository.SubjectRepository;
import com.lopjv.qlhoctap.repository.UserRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuestionImportService {

    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    public QuestionImportService(
            QuestionRepository questionRepository,
            QuestionOptionRepository questionOptionRepository,
            SubjectRepository subjectRepository,
            UserRepository userRepository) {
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public QuestionImportResultDto importQuestionsFromExcel(MultipartFile file, Long subjectId, User createdBy) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học với ID: " + subjectId));

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File Excel tải lên bị rỗng!");
        }

        List<String> errorList = new ArrayList<>();
        int totalProcessed = 0;
        int totalSuccess = 0;
        int totalFailed = 0;

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();

            for (int rowIndex = 1; rowIndex <= lastRowNum; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                totalProcessed++;
                int displayRowNumber = rowIndex + 1;

                try {
                    // 1. Đọc nội dung câu hỏi (Col 0)
                    String content = getCellValueAsString(row.getCell(0));
                    if (content.trim().isEmpty()) {
                        errorList.add(String.format("Dòng %d: Nội dung câu hỏi không được để trống.", displayRowNumber));
                        totalFailed++;
                        continue;
                    }

                    // 2. Đọc các lựa chọn đáp án (Col 1 -> Col 4: Option A, B, C, D)
                    List<String> optionTexts = new ArrayList<>();
                    for (int col = 1; col <= 4; col++) {
                        String opt = getCellValueAsString(row.getCell(col));
                        if (!opt.trim().isEmpty()) {
                            optionTexts.add(opt.trim());
                        }
                    }

                    if (optionTexts.size() < 2) {
                        errorList.add(String.format("Dòng %d: Câu hỏi phải có ít nhất 2 phương án trả lời.", displayRowNumber));
                        totalFailed++;
                        continue;
                    }

                    // 3. Đọc chỉ số đáp án đúng (Col 5)
                    String correctIndicesStr = getCellValueAsString(row.getCell(5));
                    Set<Integer> correctIndices = parseCorrectOptionIndices(correctIndicesStr, optionTexts.size());
                    if (correctIndices.isEmpty()) {
                        errorList.add(String.format("Dòng %d: Chưa chỉ định đáp án đúng hoặc định dạng đáp án đúng không hợp lệ ('%s').",
                                displayRowNumber, correctIndicesStr));
                        totalFailed++;
                        continue;
                    }

                    // 4. Đọc độ khó (Col 6)
                    String difficulty = getCellValueAsString(row.getCell(6)).toUpperCase().trim();
                    if (!Arrays.asList("EASY", "MEDIUM", "HARD").contains(difficulty)) {
                        errorList.add(String.format("Dòng %d: Độ khó '%s' không hợp lệ (Phải là EASY, MEDIUM hoặc HARD).",
                                displayRowNumber, difficulty));
                        totalFailed++;
                        continue;
                    }

                    // 5. Đọc loại câu hỏi (Col 7)
                    String questionType = getCellValueAsString(row.getCell(7)).toUpperCase().trim();
                    if (questionType.isEmpty()) {
                        questionType = correctIndices.size() > 1 ? "MULTIPLE_CHOICE" : "SINGLE_CHOICE";
                    }
                    if (!Arrays.asList("SINGLE_CHOICE", "MULTIPLE_CHOICE").contains(questionType)) {
                        errorList.add(String.format("Dòng %d: Loại câu hỏi '%s' không hợp lệ (Phải là SINGLE_CHOICE hoặc MULTIPLE_CHOICE).",
                                displayRowNumber, questionType));
                        totalFailed++;
                        continue;
                    }

                    // Validate sự tương thích giữa loại câu hỏi và số lượng đáp án đúng
                    if ("SINGLE_CHOICE".equals(questionType) && correctIndices.size() > 1) {
                        errorList.add(String.format("Dòng %d: Câu hỏi loại SINGLE_CHOICE nhưng có %d đáp án đúng.",
                                displayRowNumber, correctIndices.size()));
                        totalFailed++;
                        continue;
                    }

                    // 6. Đọc Chương / Chủ đề (Col 8)
                    String chapterTopic = getCellValueAsString(row.getCell(8)).trim();

                    // 7. Tạo và lưu Entity Question
                    Question question = Question.builder()
                            .subject(subject)
                            .createdBy(createdBy)
                            .chapterTopic(chapterTopic.isEmpty() ? "Chủ đề chung" : chapterTopic)
                            .content(content.trim())
                            .questionType(questionType)
                            .difficulty(difficulty)
                            .build();

                    Question savedQuestion = questionRepository.save(question);

                    // 8. Tạo và lưu các phương án QuestionOption tương ứng
                    List<QuestionOption> optionsToSave = new ArrayList<>();
                    for (int i = 0; i < optionTexts.size(); i++) {
                        boolean isCorrect = correctIndices.contains(i + 1);

                        QuestionOption option = QuestionOption.builder()
                                .question(savedQuestion)
                                .content(optionTexts.get(i))
                                .isCorrect(isCorrect)
                                .build();

                        optionsToSave.add(option);
                    }

                    questionOptionRepository.saveAll(optionsToSave);
                    totalSuccess++;

                } catch (Exception ex) {
                    errorList.add(String.format("Dòng %d: Lỗi xử lý dữ liệu - %s", displayRowNumber, ex.getMessage()));
                    totalFailed++;
                }
            }

        } catch (Exception ex) {
            throw new RuntimeException("Không thể đọc file Excel: " + ex.getMessage(), ex);
        }

        return QuestionImportResultDto.builder()
                .totalProcessed(totalProcessed)
                .totalSuccess(totalSuccess)
                .totalFailed(totalFailed)
                .errors(errorList)
                .build();
    }

    private Set<Integer> parseCorrectOptionIndices(String rawStr, int maxOptions) {
        Set<Integer> indices = new HashSet<>();
        if (rawStr == null || rawStr.trim().isEmpty()) {
            return indices;
        }

        String cleaned = rawStr.toUpperCase().replaceAll("[;\s]+", ",");
        String[] tokens = cleaned.split(",");

        for (String token : tokens) {
            token = token.trim();
            if (token.isEmpty()) {
                continue;
            }

            // Xử lý dạng ký tự A, B, C, D
            if (token.length() == 1 && token.charAt(0) >= 'A' && token.charAt(0) <= 'Z') {
                int index = token.charAt(0) - 'A' + 1;
                if (index <= maxOptions) {
                    indices.add(index);
                }
            } else {
                try {
                    // Xử lý dạng số 1, 2, 3, 4
                    double numVal = Double.parseDouble(token);
                    int index = (int) numVal;
                    if (index >= 1 && index <= maxOptions) {
                        indices.add(index);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return indices;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        CellType cellType = cell.getCellType();
        switch (cellType) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                double num = cell.getNumericCellValue();
                if (num == (long) num) {
                    return String.valueOf((long) num);
                }
                return String.valueOf(num);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    private boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK && !getCellValueAsString(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
