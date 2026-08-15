CREATE DATABASE IF NOT EXISTS qlhoctap1;
USE qlhoctap1;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(200),
    role ENUM('ADMIN','TEACHER','STUDENT') NOT NULL,
    role_id INT,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS questions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    option_a VARCHAR(255) NOT NULL,
    option_b VARCHAR(255) NOT NULL,
    option_c VARCHAR(255) NOT NULL,
    option_d VARCHAR(255) NOT NULL,
    correct_answer CHAR(1) NOT NULL,
    difficulty VARCHAR(50) NOT NULL,
    chapter VARCHAR(50) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS assignments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    due_date DATE,
    max_score INT DEFAULT 10,
    type VARCHAR(50) DEFAULT 'UML',
    published BOOLEAN DEFAULT TRUE,
    created_by INT,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

INSERT INTO users (username, email, password, full_name, role, role_id, is_active) VALUES
('admin01', 'admin01@elearning.com', '123456', 'Nguyễn Quân Trí', 'ADMIN', 1, TRUE),
('teacher01', 'teacher01@elearning.com', '123456', 'Nguyễn Văn An', 'TEACHER', 2, TRUE),
('teacher02', 'teacher02@elearning.com', '123456', 'Trần Thị Bình', 'TEACHER', 2, TRUE),
('student01', 'student01@elearning.com', '123456', 'Lê Văn Minh', 'STUDENT', 3, TRUE),
('student02', 'student02@elearning.com', '123456', 'Phạm Thị Lan', 'STUDENT', 3, TRUE),
('student03', 'student03@elearning.com', '123456', 'Hoàng Văn Nam', 'STUDENT', 3, TRUE),
('student04', 'student04@elearning.com', '123456', 'Vũ Thị Hoa', 'STUDENT', 3, TRUE),
('student05', 'student05@elearning.com', '123456', 'Đỗ Minh Tuấn', 'STUDENT', 3, TRUE),
('student06', 'student06@elearning.com', '123456', 'Nguyễn Quang Học', 'STUDENT', 3, TRUE);

INSERT INTO questions (content, option_a, option_b, option_c, option_d, correct_answer, difficulty, chapter, is_active) VALUES
('Trong mô hình học tập trực tuyến, LMS có vai trò chính là gì?',
 'Quản lý nội dung, lớp học và tiến độ học tập',
 'Chỉ lưu trữ tài liệu dạng PDF',
 'Chỉ dùng để gửi email thông báo',
 'Thay thế hoàn toàn giảng viên',
 'A', 'Dễ', 'Chương 1', TRUE),

('Thuật toán nào phù hợp để tự động tính điểm trắc nghiệm theo đáp án chuẩn?',
 'Sắp xếp nổi bọt',
 'So khớp đáp án',
 'Nén dữ liệu',
 'Mã hóa bất đối xứng',
 'B', 'Dễ', 'Chương 2', TRUE),

('Khi đánh giá tự động bài tự luận ngắn, hệ thống cần tiền xử lý dữ liệu văn bản như thế nào?',
 'Xóa toàn bộ câu trả lời',
 'Chỉ đếm số ký tự',
 'Chuẩn hóa, tách từ và loại bỏ nhiễu',
 'Chỉ kiểm tra thời gian nộp bài',
 'C', 'Trung bình', 'Chương 3', TRUE),

('Chỉ số nào thường dùng để đánh giá mức độ hoàn thành học phần của người học?',
 'Màu nền giao diện',
 'Tỷ lệ hoàn thành, điểm trung bình, số lần làm bài',
 'Kích thước màn hình',
 'Tên trình duyệt',
 'B', 'Trung bình', 'Chương 4', TRUE),

('Cách nào giúp phát hiện câu hỏi có độ phân biệt thấp trong ngân hàng câu hỏi?',
 'Đổi màu đáp án đúng',
 'Ẩn câu hỏi khỏi giao diện',
 'Phân tích tỷ lệ trả lời đúng theo nhóm điểm',
 'Tăng thời gian làm bài',
 'C', 'Khó', 'Chương 5', TRUE);

INSERT INTO assignments (title, description, due_date, max_score, type, published, created_by) VALUES
('Bài kiểm tra UML', 'Nộp sơ đồ lớp và mô tả quan hệ', '2026-08-20', 10, 'UML', TRUE, 2),
('Quiz Java - Chương 5', 'Trắc nghiệm Java nâng cao', '2026-08-18', 10, 'TRAC_NGHIEM', TRUE, 2);
