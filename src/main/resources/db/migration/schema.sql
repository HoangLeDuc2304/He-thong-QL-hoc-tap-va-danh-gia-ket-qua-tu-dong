CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id INT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- =============================================================================
-- PHẦN II: PHÂN HỆ LỘ TRÌNH HỌC TẬP (LMS: COURSE -> SUBJECT -> LESSON)
-- =============================================================================

CREATE TABLE courses (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    created_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE subjects (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    code VARCHAR(30) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    teacher_id BIGINT NOT NULL REFERENCES users(id),
    order_index INT DEFAULT 1 NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE (course_id, code)
);

CREATE TABLE lessons (
    id BIGSERIAL PRIMARY KEY,
    subject_id BIGINT NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    video_url VARCHAR(500),
    attachment_url VARCHAR(500),
    order_index INT DEFAULT 1 NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE enrollments (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    enrolled_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE (course_id, student_id)
);

-- =============================================================================
-- PHẦN III: NGÂN HÀNG CÂU HỎI TRẮC NGHIỆM & ĐÁP ÁN
-- =============================================================================

CREATE TABLE question_bank (
    id BIGSERIAL PRIMARY KEY,
    subject_id BIGINT NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    created_by BIGINT NOT NULL REFERENCES users(id),
    chapter_topic VARCHAR(200),
    content TEXT NOT NULL,
    question_type VARCHAR(30) DEFAULT 'SINGLE_CHOICE' NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_question_type CHECK (question_type IN ('SINGLE_CHOICE', 'MULTIPLE_CHOICE')),
    CONSTRAINT chk_difficulty CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD'))
);

CREATE TABLE question_options (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL REFERENCES question_bank(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE NOT NULL
);

-- =============================================================================
-- PHẦN IV: QUẢN LÝ THI & TỰ ĐỘNG TRỘN ĐỀ
-- =============================================================================

CREATE TABLE exams (
    id BIGSERIAL PRIMARY KEY,
    subject_id BIGINT NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    created_by BIGINT NOT NULL REFERENCES users(id),
    title VARCHAR(200) NOT NULL,
    duration_minutes INT NOT NULL CHECK (duration_minutes > 0),
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    max_tab_switches INT DEFAULT 3 NOT NULL,
    status VARCHAR(20) DEFAULT 'DRAFT' NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_exam_time CHECK (end_time > start_time),
    CONSTRAINT chk_exam_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

CREATE TABLE exam_configurations (
    id BIGSERIAL PRIMARY KEY,
    exam_id BIGINT NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
    chapter_topic VARCHAR(200),
    difficulty VARCHAR(20) NOT NULL,
    question_count INT NOT NULL CHECK (question_count > 0),
    CONSTRAINT chk_config_difficulty CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD'))
);

CREATE TABLE exam_questions (
    exam_id BIGINT NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES question_bank(id) ON DELETE CASCADE,
    order_index INT NOT NULL,
    PRIMARY KEY (exam_id, question_id)
);

-- =============================================================================
-- PHẦN V: THỰC LƯỢNG LÀM BÀI TRẮC NGHIỆM & TỰ ĐỘNG CHẤM ĐIỂM
-- =============================================================================

CREATE TABLE student_exams (
    id BIGSERIAL PRIMARY KEY,
    exam_id BIGINT NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    start_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    submit_time TIMESTAMP WITH TIME ZONE,
    score NUMERIC(5, 2) DEFAULT 0.00,
    tab_switch_count INT DEFAULT 0 NOT NULL,
    status VARCHAR(20) DEFAULT 'IN_PROGRESS' NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE (exam_id, student_id),
    CONSTRAINT chk_submission_status CHECK (status IN ('IN_PROGRESS', 'SUBMITTED', 'AUTO_SUBMITTED', 'CANCELLED'))
);

CREATE TABLE student_answers (
    id BIGSERIAL PRIMARY KEY,
    student_exam_id BIGINT NOT NULL REFERENCES student_exams(id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES question_bank(id),
    is_correct BOOLEAN,
    score_given NUMERIC(4, 2) DEFAULT 0.00,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE (student_exam_id, question_id)
);

CREATE TABLE student_answer_options (
    student_answer_id BIGINT NOT NULL REFERENCES student_answers(id) ON DELETE CASCADE,
    option_id BIGINT NOT NULL REFERENCES question_options(id) ON DELETE CASCADE,
    PRIMARY KEY (student_answer_id, option_id)
);

-- =============================================================================
-- PHẦN VI: PHÂN HỆ BÀI TẬP UML & ĐÁNH GIÁ HỖ TRỢ BỞI AI/LLM
-- =============================================================================

CREATE TABLE uml_assignments (
    id BIGSERIAL PRIMARY KEY,
    subject_id BIGINT NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    created_by BIGINT NOT NULL REFERENCES users(id),
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    rubric_criteria TEXT,
    max_score NUMERIC(5, 2) DEFAULT 10.00 NOT NULL CHECK (max_score > 0),
    due_date TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE uml_submissions (
    id BIGSERIAL PRIMARY KEY,
    assignment_id BIGINT NOT NULL REFERENCES uml_assignments(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    file_url VARCHAR(500) NOT NULL,
    file_type VARCHAR(20) NOT NULL,
    submitted_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ai_suggested_score NUMERIC(5, 2),
    ai_feedback TEXT,
    ai_analyzed_at TIMESTAMP WITH TIME ZONE,
    final_score NUMERIC(5, 2),
    teacher_feedback TEXT,
    graded_by BIGINT REFERENCES users(id),
    graded_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(30) DEFAULT 'SUBMITTED' NOT NULL,
    CONSTRAINT chk_uml_status CHECK (status IN ('SUBMITTED', 'AI_ANALYZED', 'GRADED', 'LATE')),
    CONSTRAINT chk_file_type CHECK (file_type IN ('IMAGE', 'PDF')),
    UNIQUE (assignment_id, student_id)
);

-- =============================================================================
-- PHẦN VII: TỐI ƯU HÓA TRUY VẤN (INDEXES FOR HIGH PERFORMANCE)
-- =============================================================================

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_subjects_course ON subjects(course_id);
CREATE INDEX idx_lessons_subject ON lessons(subject_id);
CREATE INDEX idx_question_bank_filter ON question_bank(subject_id, chapter_topic, difficulty);
CREATE INDEX idx_student_exams_lookup ON student_exams(exam_id, student_id);
CREATE INDEX idx_student_exams_status ON student_exams(status);
CREATE INDEX idx_student_answers_lookup ON student_answers(student_exam_id, question_id);
CREATE INDEX idx_uml_assignments_subject ON uml_assignments(subject_id);
CREATE INDEX idx_uml_submissions_lookup ON uml_submissions(assignment_id, student_id);
CREATE INDEX idx_uml_submissions_status ON uml_submissions(status);




INSERT INTO roles (id, name, description) VALUES
(1, 'ADMIN', 'Quản trị viên hệ thống'),
(2, 'TEACHER', 'Giảng viên'),
(3, 'STUDENT', 'Sinh viên');

-- 2. USERS
INSERT INTO users
(id, username, password, full_name, email, is_active)
VALUES
(1, 'admin@gmail.com', 'HASH_BCRYPT', 'Quản trị viên', 'admin@elearning.vn', TRUE),
(2, 'gv01@gmail.com', 'HASH_BCRYPT', 'Nguyễn Văn Toàn', 'toan@elearning.vn', TRUE),
(3, 'gv02@gmail.com', 'HASH_BCRYPT', 'Trần Thị Lan', 'lan@elearning.vn', TRUE),
(4, 'sv001@gmail.com', 'HASH_BCRYPT', 'Nguyễn Minh Anh', 'sv001@elearning.vn', TRUE),
(5, 'sv002@gmail.com', 'HASH_BCRYPT', 'Trần Minh Đức', 'sv002@elearning.vn', TRUE),
(6, 'sv003@gmail.com', 'HASH_BCRYPT', 'Lê Hoàng Nam', 'sv003@elearning.vn', TRUE),
(7, 'sv004@gmail.com', 'HASH_BCRYPT', 'Phạm Thu Hà', 'sv004@elearning.vn', TRUE);
-- 3. USER ROLES
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), (2, 2), (3, 2), (4, 3), (5, 3), (6, 3), (7, 3);

-- 4. COURSES
INSERT INTO courses
(id, code, title, description, created_by)
VALUES
(1, 'CNTT2026', 'Công nghệ thông tin',
 'Chương trình đào tạo ngành Công nghệ thông tin', 2),
(2, 'SE2026', 'Kỹ thuật phần mềm',
 'Chương trình đào tạo Kỹ thuật phần mềm', 3);

-- 5. SUBJECTS
INSERT INTO subjects
(id, course_id, code, title, description, teacher_id, order_index)
VALUES
(1, 1, 'JAVA01', 'Lập trình Java',
 'Lập trình hướng đối tượng với Java và Spring Boot', 2, 1),
(2, 1, 'DB01', 'Cơ sở dữ liệu',
 'Thiết kế và quản trị cơ sở dữ liệu quan hệ', 2, 2),
(3, 1, 'WEB01', 'Lập trình Web',
 'Phát triển ứng dụng web hiện đại', 3, 3),
(4, 2, 'SE01', 'Công nghệ phần mềm',
 'Phân tích, thiết kế và phát triển phần mềm', 3, 1);
select*from subjects;
SELECT * FROM courses;
-- 6. LESSONS
INSERT INTO lessons
(id, subject_id, title, content, video_url, attachment_url, order_index)
VALUES
(1, 1, 'Giới thiệu Java',
 'Tổng quan về ngôn ngữ Java và môi trường phát triển.',
 'https://example.com/java-intro', NULL, 1),
(2, 1, 'Lập trình hướng đối tượng',
 'Class, Object, Inheritance, Encapsulation và Polymorphism.',
 'https://example.com/oop', NULL, 2),
(3, 1, 'Spring Boot cơ bản',
 'Giới thiệu Spring Boot và xây dựng REST API.',
 'https://example.com/spring-boot', NULL, 3),
(4, 2, 'Mô hình cơ sở dữ liệu',
 'Khái niệm bảng, khóa chính và khóa ngoại.',
 NULL, 'database-basic.pdf', 1),
(5, 2, 'SQL cơ bản',
 'SELECT, INSERT, UPDATE, DELETE và JOIN.',
 NULL, 'sql-basic.pdf', 2),
(6, 3, 'HTML và CSS',
 'Xây dựng giao diện website cơ bản.',
 'https://example.com/html-css', NULL, 1),
(7, 3, 'JavaScript',
 'Các kiến thức JavaScript cơ bản.',
 'https://example.com/javascript', NULL, 2);

-- 7. ENROLLMENTS
INSERT INTO enrollments
(id, course_id, student_id)
VALUES
(1, 1, 4),
(2, 1, 5),
(3, 1, 6),
(4, 1, 7),
(5, 2, 4),
(6, 2, 5),
(7, 2, 6);

-- 8. QUESTION BANK
INSERT INTO question_bank
(id, subject_id, created_by, chapter_topic, content, question_type, difficulty)
VALUES
(1, 1, 2, 'Java cơ bản', 'Java được phát triển bởi công ty nào?', 'SINGLE_CHOICE', 'EASY'),
(2, 1, 2, 'OOP', 'Tính chất nào cho phép một lớp kế thừa thuộc tính và phương thức của lớp khác?', 'SINGLE_CHOICE', 'EASY'),
(3, 1, 2, 'OOP', 'Đâu là các đặc điểm của lập trình hướng đối tượng?', 'MULTIPLE_CHOICE', 'MEDIUM'),
(4, 1, 2, 'Java', 'Từ khóa nào được sử dụng để tạo một lớp trong Java?', 'SINGLE_CHOICE', 'EASY'),
(5, 1, 2, 'Java', 'Kiểu dữ liệu nào dùng để lưu giá trị đúng hoặc sai?', 'SINGLE_CHOICE', 'EASY'),
(6, 2, 2, 'SQL', 'Lệnh SQL nào dùng để lấy dữ liệu từ bảng?', 'SINGLE_CHOICE', 'EASY'),
(7, 2, 2, 'SQL', 'Khóa chính trong cơ sở dữ liệu có mục đích gì?', 'SINGLE_CHOICE', 'EASY'),
(8, 2, 2, 'Database', 'JOIN được sử dụng để làm gì?', 'SINGLE_CHOICE', 'MEDIUM'),
(9, 3, 3, 'HTML', 'Thẻ HTML nào dùng để tạo liên kết?', 'SINGLE_CHOICE', 'EASY'),
(10, 3, 3, 'JavaScript', 'JavaScript được sử dụng chủ yếu để làm gì trong website?', 'SINGLE_CHOICE', 'EASY'),
(11, 3, 3, 'JavaScript', 'Đâu là các kiểu dữ liệu cơ bản trong JavaScript?', 'MULTIPLE_CHOICE', 'MEDIUM'),
(12, 4, 3, 'Software Engineering', 'UML được sử dụng chủ yếu để làm gì?', 'SINGLE_CHOICE', 'EASY');

-- 9. QUESTION OPTIONS
INSERT INTO question_options
(id, question_id, content, is_correct)
VALUES
(1, 1, 'Microsoft', FALSE),
(2, 1, 'Sun Microsystems', TRUE),
(3, 1, 'Apple', FALSE),
(4, 1, 'IBM', FALSE),
(5, 2, 'Inheritance', TRUE),
(6, 2, 'Encapsulation', FALSE),
(7, 2, 'Compilation', FALSE),
(8, 2, 'Iteration', FALSE),
(9, 3, 'Encapsulation', TRUE),
(10, 3, 'Inheritance', TRUE),
(11, 3, 'Polymorphism', TRUE),
(12, 3, 'Compilation', FALSE),
(13, 4, 'class', TRUE),
(14, 4, 'object', FALSE),
(15, 4, 'newclass', FALSE),
(16, 4, 'struct', FALSE),
(17, 5, 'boolean', TRUE),
(18, 5, 'int', FALSE),
(19, 5, 'String', FALSE),
(20, 5, 'double', FALSE),
(21, 6, 'SELECT', TRUE),
(22, 6, 'INSERT', FALSE),
(23, 6, 'DELETE', FALSE),
(24, 6, 'UPDATE', FALSE),
(25, 7, 'Xác định duy nhất một bản ghi', TRUE),
(26, 7, 'Lưu file', FALSE),
(27, 7, 'Tạo giao diện', FALSE),
(28, 7, 'Mã hóa mật khẩu', FALSE),
(29, 8, 'Kết hợp dữ liệu từ nhiều bảng', TRUE),
(30, 8, 'Xóa bảng', FALSE),
(31, 8, 'Tạo database', FALSE),
(32, 8, 'Tạo user', FALSE),
(33, 9, '<a>', TRUE),
(34, 9, '<p>', FALSE),
(35, 9, '<div>', FALSE),
(36, 9, '<link>', FALSE),
(37, 10, 'Tạo tương tác cho trang web', TRUE),
(38, 10, 'Quản lý database trực tiếp', FALSE),
(39, 10, 'Thay thế hệ điều hành', FALSE),
(40, 10, 'Tạo máy chủ vật lý', FALSE),
(41, 11, 'String', TRUE),
(42, 11, 'Number', TRUE),
(43, 11, 'Boolean', TRUE),
(44, 11, 'HTML', FALSE),
(45, 12, 'Mô hình hóa và trực quan hóa hệ thống', TRUE),
(46, 12, 'Chỉ dùng để viết code Java', FALSE),
(47, 12, 'Quản lý máy chủ', FALSE),
(48, 12, 'Lưu trữ file', FALSE);

-- 10. EXAMS
INSERT INTO exams
(id, subject_id, created_by, title, duration_minutes,
 start_time, end_time, max_tab_switches, status)
VALUES
(1, 1, 2, 'Kiểm tra Java cơ bản', 30,
 '2026-08-20 08:00:00+07', '2026-08-20 09:00:00+07', 3, 'PUBLISHED'),
(2, 2, 2, 'Kiểm tra Cơ sở dữ liệu', 30,
 '2026-08-21 08:00:00+07', '2026-08-21 09:00:00+07', 3, 'PUBLISHED'),
(3, 3, 3, 'Kiểm tra Lập trình Web', 45,
 '2026-08-22 08:00:00+07', '2026-08-22 09:00:00+07', 3, 'PUBLISHED');

-- 11. EXAM CONFIGURATIONS
INSERT INTO exam_configurations
(id, exam_id, chapter_topic, difficulty, question_count)
VALUES
(1, 1, 'Java cơ bản', 'EASY', 2),
(2, 1, 'OOP', 'MEDIUM', 1),
(3, 2, 'SQL', 'EASY', 2),
(4, 2, 'Database', 'MEDIUM', 1),
(5, 3, 'HTML', 'EASY', 1),
(6, 3, 'JavaScript', 'MEDIUM', 1);

-- 12. EXAM QUESTIONS
INSERT INTO exam_questions
(exam_id, question_id, order_index)
VALUES
(1, 1, 1),
(1, 2, 2),
(1, 3, 3),
(1, 4, 4),
(1, 5, 5),
(2, 6, 1),
(2, 7, 2),
(2, 8, 3),
(3, 9, 1),
(3, 10, 2),
(3, 11, 3);

-- 13. STUDENT EXAMS
INSERT INTO student_exams
(id, exam_id, student_id, start_time, submit_time,
 score, tab_switch_count, status)
VALUES
(1, 1, 4, '2026-08-20 08:05:00+07', '2026-08-20 08:28:00+07', 8.50, 1, 'SUBMITTED'),
(2, 1, 5, '2026-08-20 08:10:00+07', '2026-08-20 08:35:00+07', 7.00, 2, 'SUBMITTED'),
(3, 1, 6, '2026-08-20 08:15:00+07', '2026-08-20 08:42:00+07', 9.00, 0, 'SUBMITTED'),
(4, 2, 4, '2026-08-21 08:05:00+07', '2026-08-21 08:25:00+07', 8.00, 0, 'SUBMITTED'),
(5, 2, 5, '2026-08-21 08:10:00+07', '2026-08-21 08:40:00+07', 6.50, 1, 'SUBMITTED'),
(6, 3, 4, '2026-08-22 08:05:00+07', NULL, 0.00, 0, 'IN_PROGRESS');

-- 14. STUDENT ANSWERS
INSERT INTO student_answers
(id, student_exam_id, question_id, is_correct, score_given)
VALUES
(1, 1, 1, TRUE, 2.00),
(2, 1, 2, TRUE, 2.00),
(3, 1, 3, FALSE, 0.00),
(4, 1, 4, TRUE, 2.00),
(5, 1, 5, TRUE, 2.50),
(6, 2, 1, TRUE, 2.00),
(7, 2, 2, FALSE, 0.00),
(8, 2, 3, TRUE, 2.00),
(9, 2, 4, TRUE, 2.00),
(10, 2, 5, TRUE, 1.00),
(11, 3, 1, TRUE, 2.00),
(12, 3, 2, TRUE, 2.00),
(13, 3, 3, TRUE, 2.00),
(14, 3, 4, TRUE, 1.50),
(15, 3, 5, TRUE, 1.50),
(16, 4, 6, TRUE, 3.00),
(17, 4, 7, TRUE, 2.50),
(18, 4, 8, TRUE, 2.50),
(19, 5, 6, TRUE, 3.00),
(20, 5, 7, FALSE, 0.00),
(21, 5, 8, TRUE, 3.50);

-- 15. STUDENT ANSWER OPTIONS
INSERT INTO student_answer_options
(student_answer_id, option_id)
VALUES
(1, 2),
(2, 5),
(3, 9),
(4, 13),
(5, 17),
(6, 2),
(7, 6),
(8, 9),
(9, 13),
(10, 17),
(11, 2),
(12, 5),
(13, 9),
(13, 10),
(13, 11),
(14, 13),
(15, 17),
(16, 21),
(17, 25),
(18, 29),
(19, 21),
(20, 26),
(21, 29);

-- 16. UML ASSIGNMENTS
INSERT INTO uml_assignments
(id, subject_id, created_by, title, description,
 rubric_criteria, max_score, due_date)
VALUES
(1, 1, 2,
 'Thiết kế UML hệ thống quản lý thư viện',
 'Sinh viên xây dựng Class Diagram cho hệ thống quản lý thư viện.',
 'Đầy đủ lớp; thuộc tính; phương thức; quan hệ giữa các lớp; tính chính xác của mô hình.',
 10.00, '2026-08-30 23:59:00+07'),
(2, 4, 3,
 'Thiết kế UML hệ thống bán hàng',
 'Xây dựng sơ đồ UML cho hệ thống bán hàng trực tuyến.',
 'Class Diagram; Association; Inheritance; Multiplicity; tính hợp lý của mô hình.',
 10.00, '2026-09-05 23:59:00+07');

-- 17. UML SUBMISSIONS
INSERT INTO uml_submissions
(id, assignment_id, student_id, file_url, file_type,
 submitted_at, ai_suggested_score, ai_feedback,
 ai_analyzed_at, final_score, teacher_feedback,
 graded_by, graded_at, status)
VALUES
(1, 1, 4,
 'uploads/uml/library_sv001.png', 'IMAGE',
 '2026-08-25 20:30:00+07',
 8.50,
 'Mô hình có đầy đủ các lớp chính và quan hệ cơ bản. Cần bổ sung một số multiplicity.',
 '2026-08-25 20:35:00+07',
 8.50,
 'Bài làm tốt, cần chú ý multiplicity.',
 2, '2026-08-26 09:00:00+07', 'GRADED'),

(2, 1, 5,
 'uploads/uml/library_sv002.pdf', 'PDF',
 '2026-08-26 21:15:00+07',
 7.50,
 'Mô hình tương đối đầy đủ nhưng còn thiếu một số quan hệ giữa các lớp.',
 '2026-08-26 21:20:00+07',
 7.00,
 'Cần bổ sung quan hệ giữa Reader và Borrowing.',
 2, '2026-08-27 10:00:00+07', 'GRADED'),

(3, 1, 6,
 'uploads/uml/library_sv003.png', 'IMAGE',
 '2026-08-27 19:45:00+07',
 9.00,
 'Sơ đồ có cấu trúc tốt, đầy đủ các thành phần chính.',
 '2026-08-27 19:50:00+07',
 NULL, NULL, NULL, NULL, 'AI_ANALYZED'),

(4, 2, 4,
 'uploads/uml/shop_sv001.png', 'IMAGE',
 '2026-08-28 20:00:00+07',
 8.00,
 'Các lớp chính được xác định đúng, tuy nhiên cần cải thiện quan hệ kế thừa.',
 '2026-08-28 20:05:00+07',
 NULL, NULL, NULL, NULL, 'AI_ANALYZED');

-- ============================================================
-- ĐỒNG BỘ SEQUENCE SAU KHI GÁN ID THỦ CÔNG
-- ============================================================

SELECT setval(pg_get_serial_sequence('roles', 'id'), COALESCE(MAX(id), 1), true) FROM roles;
SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE(MAX(id), 1), true) FROM users;
SELECT setval(pg_get_serial_sequence('courses', 'id'), COALESCE(MAX(id), 1), true) FROM courses;
SELECT setval(pg_get_serial_sequence('subjects', 'id'), COALESCE(MAX(id), 1), true) FROM subjects;
SELECT setval(pg_get_serial_sequence('lessons', 'id'), COALESCE(MAX(id), 1), true) FROM lessons;
SELECT setval(pg_get_serial_sequence('enrollments', 'id'), COALESCE(MAX(id), 1), true) FROM enrollments;
SELECT setval(pg_get_serial_sequence('question_bank', 'id'), COALESCE(MAX(id), 1), true) FROM question_bank;
SELECT setval(pg_get_serial_sequence('question_options', 'id'), COALESCE(MAX(id), 1), true) FROM question_options;
SELECT setval(pg_get_serial_sequence('exams', 'id'), COALESCE(MAX(id), 1), true) FROM exams;
SELECT setval(pg_get_serial_sequence('exam_configurations', 'id'), COALESCE(MAX(id), 1), true) FROM exam_configurations;
SELECT setval(pg_get_serial_sequence('student_exams', 'id'), COALESCE(MAX(id), 1), true) FROM student_exams;
SELECT setval(pg_get_serial_sequence('student_answers', 'id'), COALESCE(MAX(id), 1), true) FROM student_answers;
SELECT setval(pg_get_serial_sequence('uml_assignments', 'id'), COALESCE(MAX(id), 1), true) FROM uml_assignments;
SELECT setval(pg_get_serial_sequence('uml_submissions', 'id'), COALESCE(MAX(id), 1), true) FROM uml_submissions;

-- ============================================================
-- KIỂM TRA
-- ============================================================
SELECT 'roles' AS table_name, COUNT(*) AS total FROM roles
UNION ALL SELECT 'users', COUNT(*) FROM users
UNION ALL SELECT 'courses', COUNT(*) FROM courses
UNION ALL SELECT 'subjects', COUNT(*) FROM subjects
UNION ALL SELECT 'lessons', COUNT(*) FROM lessons
UNION ALL SELECT 'enrollments', COUNT(*) FROM enrollments
UNION ALL SELECT 'question_bank', COUNT(*) FROM question_bank
UNION ALL SELECT 'question_options', COUNT(*) FROM question_options
UNION ALL SELECT 'exams', COUNT(*) FROM exams
UNION ALL SELECT 'exam_configurations', COUNT(*) FROM exam_configurations
UNION ALL SELECT 'exam_questions', COUNT(*) FROM exam_questions
UNION ALL SELECT 'student_exams', COUNT(*) FROM student_exams
UNION ALL SELECT 'student_answers', COUNT(*) FROM student_answers
UNION ALL SELECT 'student_answer_options', COUNT(*) FROM student_answer_options
UNION ALL SELECT 'uml_assignments', COUNT(*) FROM uml_assignments
UNION ALL SELECT 'uml_submissions', COUNT(*) FROM uml_submissions;
TRUNCATE TABLE users RESTART IDENTITY CASCADE;
select*from users;

SELECT 
    u.id,
    u.username,
    u.password,
    u.is_active,
    r.name AS role
FROM users u
LEFT JOIN user_roles ur ON ur.user_id = u.id
LEFT JOIN roles r ON r.id = ur.role_id
WHERE u.username = 'gv01@gmail.com';
SELECT * FROM roles;
SELECT * FROM user_roles;

SELECT
    u.id,
    u.username,
    r.id AS role_id,
    r.name AS role
FROM users u
LEFT JOIN user_roles ur ON ur.user_id = u.id
LEFT JOIN roles r ON r.id = ur.role_id
ORDER BY u.id;