import os
import sqlite3

path = r'D:\QLhoctap\database.db'

if os.path.exists(path):
    os.remove(path)

conn = sqlite3.connect(path)
cur = conn.cursor()

cur.executescript('''
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    email TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    full_name TEXT,
    role TEXT NOT NULL CHECK(role IN ('ADMIN', 'TEACHER', 'STUDENT')),
    is_active INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE questions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    content TEXT NOT NULL,
    option_a TEXT NOT NULL,
    option_b TEXT NOT NULL,
    option_c TEXT NOT NULL,
    option_d TEXT NOT NULL,
    correct_answer TEXT NOT NULL,
    difficulty TEXT NOT NULL,
    chapter TEXT NOT NULL,
    is_active INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE assignments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT,
    due_date TEXT,
    max_score INTEGER DEFAULT 10,
    type TEXT DEFAULT 'UML',
    published INTEGER NOT NULL DEFAULT 1,
    created_by INTEGER,
    FOREIGN KEY(created_by) REFERENCES users(id)
);
''')

users = [
    ('admin01', 'admin01@elearning.com', '123456', 'Nguyễn Quân Trí', 'ADMIN', 1),
    ('teacher01', 'teacher01@elearning.com', '123456', 'Nguyễn Văn An', 'TEACHER', 1),
    ('teacher02', 'teacher02@elearning.com', '123456', 'Trần Thị Bình', 'TEACHER', 1),
    ('student01', 'student01@elearning.com', '123456', 'Lê Văn Minh', 'STUDENT', 1),
    ('student02', 'student02@elearning.com', '123456', 'Phạm Thị Lan', 'STUDENT', 1),
    ('student03', 'student03@elearning.com', '123456', 'Hoàng Văn Nam', 'STUDENT', 1),
    ('student04', 'student04@elearning.com', '123456', 'Vũ Thị Hoa', 'STUDENT', 1),
    ('student05', 'student05@elearning.com', '123456', 'Đỗ Minh Tuấn', 'STUDENT', 1),
    ('student06', 'student06@elearning.com', '123456', 'Nguyễn Quang Học', 'STUDENT', 1),
]
cur.executemany(
    'INSERT INTO users (username, email, password, full_name, role, is_active) VALUES (?, ?, ?, ?, ?, ?)',
    users
)

questions = [
    ('Trong mô hình học tập trực tuyến, LMS có vai trò chính là gì?', 'Quản lý nội dung, lớp học và tiến độ học tập', 'Chỉ lưu trữ tài liệu dạng PDF', 'Chỉ dùng để gửi email thông báo', 'Thay thế hoàn toàn giảng viên', 'A', 'Dễ', 'Chương 1', 1),
    ('Thuật toán nào phù hợp để tự động tính điểm trắc nghiệm theo đáp án chuẩn?', 'Sắp xếp nổi bọt', 'So khớp đáp án', 'Nén dữ liệu', 'Mã hóa bất đối xứng', 'B', 'Dễ', 'Chương 2', 1),
    ('Khi đánh giá tự động bài tự luận ngắn, hệ thống cần tiền xử lý dữ liệu văn bản như thế nào?', 'Xóa toàn bộ câu trả lời', 'Chỉ đếm số ký tự', 'Chuẩn hóa, tách từ và loại bỏ nhiễu', 'Chỉ kiểm tra thời gian nộp bài', 'C', 'Trung bình', 'Chương 3', 1),
    ('Chỉ số nào thường dùng để đánh giá mức độ hoàn thành học phần của người học?', 'Màu nền giao diện', 'Tỷ lệ hoàn thành, điểm trung bình, số lần làm bài', 'Kích thước màn hình', 'Tên trình duyệt', 'B', 'Trung bình', 'Chương 4', 1),
    ('Cách nào giúp phát hiện câu hỏi có độ phân biệt thấp trong ngân hàng câu hỏi?', 'Đổi màu đáp án đúng', 'Ẩn câu hỏi khỏi giao diện', 'Phân tích tỷ lệ trả lời đúng theo nhóm điểm', 'Tăng thời gian làm bài', 'C', 'Khó', 'Chương 5', 1),
]
cur.executemany(
    'INSERT INTO questions (content, option_a, option_b, option_c, option_d, correct_answer, difficulty, chapter, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)',
    questions
)

assignments = [
    ('Bài kiểm tra UML', 'Nộp sơ đồ lớp và mô tả quan hệ', '2026-08-20', 10, 'UML', 1, 2),
    ('Quiz Java - Chương 5', 'Trắc nghiệm Java nâng cao', '2026-08-18', 10, 'TRAC_NGHIEM', 1, 2),
]
cur.executemany(
    'INSERT INTO assignments (title, description, due_date, max_score, type, published, created_by) VALUES (?, ?, ?, ?, ?, ?, ?)',
    assignments
)

conn.commit()
print('DB created at:', path)
print('users=', cur.execute('SELECT COUNT(*) FROM users').fetchone()[0])
print('questions=', cur.execute('SELECT COUNT(*) FROM questions').fetchone()[0])
print('assignments=', cur.execute('SELECT COUNT(*) FROM assignments').fetchone()[0])
print('teacher01=', cur.execute("SELECT email, role, password FROM users WHERE email='teacher01@elearning.com'").fetchone())
conn.close()
