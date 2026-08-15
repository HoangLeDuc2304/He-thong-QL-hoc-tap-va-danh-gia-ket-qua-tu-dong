package com.lopjv.qlhoctap.api;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ApiDataStore {
    private final AtomicLong questionId = new AtomicLong(6);
    private final List<QuestionDto> questions = new CopyOnWriteArrayList<>(List.of(
            new QuestionDto(1L, "Trong mô hình học tập trực tuyến, LMS có vai trò chính là gì?",
                    "Quản lý nội dung, lớp học và tiến độ học tập", "Chỉ lưu trữ tài liệu dạng PDF",
                    "Chỉ dùng để gửi email thông báo", "Thay thế hoàn toàn giảng viên",
                    "A", "Dễ", "Chương 1"),
            new QuestionDto(2L, "Thuật toán nào phù hợp để tự động tính điểm trắc nghiệm theo đáp án chuẩn?",
                    "Sắp xếp nổi bọt", "So khớp đáp án", "Nén dữ liệu", "Mã hóa bất đối xứng",
                    "B", "Dễ", "Chương 2"),
            new QuestionDto(3L, "Khi đánh giá tự động bài tự luận ngắn, hệ thống cần tiền xử lý dữ liệu văn bản như thế nào?",
                    "Xóa toàn bộ câu trả lời", "Chỉ đếm số ký tự", "Chuẩn hóa, tách từ và loại bỏ nhiễu",
                    "Chỉ kiểm tra thời gian nộp bài", "C", "Trung bình", "Chương 3"),
            new QuestionDto(4L, "Chỉ số nào thường dùng để đánh giá mức độ hoàn thành học phần của người học?",
                    "Màu nền giao diện", "Tỷ lệ hoàn thành, điểm trung bình, số lần làm bài",
                    "Kích thước màn hình", "Tên trình duyệt", "B", "Trung bình", "Chương 4"),
            new QuestionDto(5L, "Cách nào giúp phát hiện câu hỏi có độ phân biệt thấp trong ngân hàng câu hỏi?",
                    "Đổi màu đáp án đúng", "Ẩn câu hỏi khỏi giao diện",
                    "Phân tích tỷ lệ trả lời đúng theo nhóm điểm", "Tăng thời gian làm bài",
                    "C", "Khó", "Chương 5")
    ));

    private final List<ResultDto> results = List.of(
            new ResultDto(1L, "Quiz 1", 8.0, "15 phút", 16, 4, 20, true, "1A, 2B, 3C, 4B, 5D"),
            new ResultDto(2L, "Quiz 2", 9.5, "12 phút", 19, 1, 20, true, "1B, 2A, 3D, 4C, 5A"),
            new ResultDto(3L, "Quiz 3", 7.0, "18 phút", 14, 6, 20, false, ""),
            new ResultDto(4L, "Quiz 4", 8.5, "16 phút", 17, 3, 20, true, "1C, 2C, 3A, 4D, 5B"),
            new ResultDto(5L, "Quiz 5", 9.0, "14 phút", 18, 2, 20, true, "1D, 2B, 3B, 4A, 5C")
    );

    public List<QuestionDto> findQuestions(String search, String difficulty, String chapter) {
        String keyword = search == null ? "" : search.trim().toLowerCase();
        return questions.stream()
                .filter(question -> keyword.isBlank() || question.searchText().contains(keyword))
                .filter(question -> difficulty == null || difficulty.isBlank() || question.difficulty().equalsIgnoreCase(difficulty))
                .filter(question -> chapter == null || chapter.isBlank() || question.chapter().equalsIgnoreCase(chapter))
                .toList();
    }

    public Optional<QuestionDto> findQuestion(Long id) {
        return questions.stream().filter(question -> question.id().equals(id)).findFirst();
    }

    public QuestionDto addQuestion(QuestionRequest request) {
        QuestionDto question = request.toDto(questionId.getAndIncrement());
        questions.add(0, question);
        return question;
    }

    public Optional<QuestionDto> updateQuestion(Long id, QuestionRequest request) {
        for (int index = 0; index < questions.size(); index++) {
            if (questions.get(index).id().equals(id)) {
                QuestionDto updatedQuestion = request.toDto(id);
                questions.set(index, updatedQuestion);
                return Optional.of(updatedQuestion);
            }
        }
        return Optional.empty();
    }

    public boolean deleteQuestion(Long id) {
        return questions.removeIf(question -> question.id().equals(id));
    }

    public List<ResultDto> results() {
        return new ArrayList<>(results);
    }

    public Optional<ResultDto> findResult(Long id) {
        return results.stream().filter(result -> result.id().equals(id)).findFirst();
    }

    public List<QuestionDto> allQuestions() {
        return new ArrayList<>(questions);
    }
}
