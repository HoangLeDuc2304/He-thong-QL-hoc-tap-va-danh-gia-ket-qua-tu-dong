CREATE USER 'qlhoctap'@'localhost' IDENTIFIED BY '23042004';
GRANT ALL PRIVILEGES ON qlhoctap1.* TO 'qlhoctap'@'localhost';
FLUSH PRIVILEGES;
create database qlhoctap1;

use qlhoctap1;
CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- ---------------------------------------------------------
-- 2.2 USERS
-- ---------------------------------------------------------

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    username VARCHAR(50) NOT NULL UNIQUE,

    password VARCHAR(255) NOT NULL,

    email VARCHAR(100) NOT NULL UNIQUE,

    full_name VARCHAR(100),

    avatar_url VARCHAR(500),

    role_id INT NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_users_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);


-- =========================================================
-- 3. PHÂN HỆ COURSE / LMS
-- =========================================================


-- ---------------------------------------------------------
-- 3.1 COURSES
-- ---------------------------------------------------------

CREATE TABLE courses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    title VARCHAR(255) NOT NULL,

    description TEXT,

    thumbnail_url VARCHAR(500),

    teacher_id BIGINT NOT NULL,

    status ENUM(
        'DRAFT',
        'PUBLISHED',
        'ARCHIVED'
    ) NOT NULL DEFAULT 'DRAFT',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_courses_teacher
        FOREIGN KEY (teacher_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);


-- ---------------------------------------------------------
-- 3.2 LESSONS
-- ---------------------------------------------------------

CREATE TABLE lessons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    course_id BIGINT NOT NULL,

    title VARCHAR(255) NOT NULL,

    description TEXT,

    content LONGTEXT,

    video_url VARCHAR(500),

    document_url VARCHAR(500),

    lesson_order INT NOT NULL DEFAULT 1,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_lessons_course
        FOREIGN KEY (course_id)
        REFERENCES courses(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


-- ---------------------------------------------------------
-- 3.3 ENROLLMENTS
-- Sinh viên đăng ký khóa học
-- ---------------------------------------------------------

CREATE TABLE enrollments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    course_id BIGINT NOT NULL,

    student_id BIGINT NOT NULL,

    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    status ENUM(
        'ACTIVE',
        'COMPLETED',
        'CANCELLED'
    ) NOT NULL DEFAULT 'ACTIVE',

    completed_at TIMESTAMP NULL,

    CONSTRAINT uk_enrollment
        UNIQUE (course_id, student_id),

    CONSTRAINT fk_enrollments_course
        FOREIGN KEY (course_id)
        REFERENCES courses(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_enrollments_student
        FOREIGN KEY (student_id)
        REFERENCES users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


-- =========================================================
-- 4. QUESTION BANK
-- =========================================================


-- ---------------------------------------------------------
-- 4.1 QUESTION BANK
-- ---------------------------------------------------------

CREATE TABLE question_banks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    course_id BIGINT NOT NULL,

    name VARCHAR(255) NOT NULL,

    description TEXT,

    created_by BIGINT NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_question_banks_course
        FOREIGN KEY (course_id)
        REFERENCES courses(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_question_banks_creator
        FOREIGN KEY (created_by)
        REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);


-- ---------------------------------------------------------
-- 4.2 QUESTIONS
-- ---------------------------------------------------------

CREATE TABLE questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    question_bank_id BIGINT NOT NULL,

    content TEXT NOT NULL,

    question_type ENUM(
        'SINGLE_CHOICE',
        'MULTIPLE_CHOICE',
        'TRUE_FALSE'
    ) NOT NULL DEFAULT 'SINGLE_CHOICE',

    difficulty ENUM(
        'EASY',
        'MEDIUM',
        'HARD'
    ) NOT NULL DEFAULT 'MEDIUM',

    explanation TEXT,

    points DECIMAL(5,2) NOT NULL DEFAULT 1.00,

    created_by BIGINT NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_questions_bank
        FOREIGN KEY (question_bank_id)
        REFERENCES question_banks(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_questions_creator
        FOREIGN KEY (created_by)
        REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);


-- ---------------------------------------------------------
-- 4.3 QUESTION OPTIONS
-- ---------------------------------------------------------

CREATE TABLE question_options (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    question_id BIGINT NOT NULL,

    option_label CHAR(1) NOT NULL,

    option_content TEXT NOT NULL,

    is_correct BOOLEAN NOT NULL DEFAULT FALSE,

    option_order INT NOT NULL DEFAULT 1,

    CONSTRAINT fk_question_options_question
        FOREIGN KEY (question_id)
        REFERENCES questions(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


-- =========================================================
-- 5. EXAM / ĐỀ THI
-- =========================================================


-- ---------------------------------------------------------
-- 5.1 EXAMS
-- ---------------------------------------------------------

CREATE TABLE exams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    course_id BIGINT NOT NULL,

    title VARCHAR(255) NOT NULL,

    description TEXT,

    duration_minutes INT NOT NULL,

    total_questions INT NOT NULL DEFAULT 0,

    total_points DECIMAL(8,2) NOT NULL DEFAULT 0,

    start_time DATETIME NULL,

    end_time DATETIME NULL,

    status ENUM(
        'DRAFT',
        'PUBLISHED',
        'CLOSED'
    ) NOT NULL DEFAULT 'DRAFT',

    created_by BIGINT NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_exams_course
        FOREIGN KEY (course_id)
        REFERENCES courses(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_exams_creator
        FOREIGN KEY (created_by)
        REFERENCES users(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);


-- ---------------------------------------------------------
-- 5.2 EXAM QUESTIONS
-- Câu hỏi thuộc đề thi
-- ---------------------------------------------------------

CREATE TABLE exam_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    exam_id BIGINT NOT NULL,

    question_id BIGINT NOT NULL,

    question_order INT NOT NULL,

    points DECIMAL(5,2) NOT NULL DEFAULT 1.00,

    CONSTRAINT uk_exam_question
        UNIQUE (exam_id, question_id),

    CONSTRAINT fk_exam_questions_exam
        FOREIGN KEY (exam_id)
        REFERENCES exams(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_exam_questions_question
        FOREIGN KEY (question_id)
        REFERENCES questions(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);


-- =========================================================
-- 6. EXAM ATTEMPT / SINH VIÊN LÀM BÀI
-- =========================================================


-- ---------------------------------------------------------
-- 6.1 EXAM ATTEMPTS
-- ---------------------------------------------------------

CREATE TABLE exam_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    exam_id BIGINT NOT NULL,

    student_id BIGINT NOT NULL,

    started_at DATETIME NOT NULL,

    submitted_at DATETIME NULL,

    deadline_at DATETIME NULL,

    status ENUM(
        'IN_PROGRESS',
        'SUBMITTED',
        'TIMEOUT',
        'CANCELLED'
    ) NOT NULL DEFAULT 'IN_PROGRESS',

    score DECIMAL(8,2) DEFAULT 0,

    correct_answers INT DEFAULT 0,

    wrong_answers INT DEFAULT 0,

    unanswered INT DEFAULT 0,

    CONSTRAINT fk_exam_attempts_exam
        FOREIGN KEY (exam_id)
        REFERENCES exams(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_exam_attempts_student
        FOREIGN KEY (student_id)
        REFERENCES users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


-- ---------------------------------------------------------
-- 6.2 STUDENT ANSWERS
-- ---------------------------------------------------------

CREATE TABLE student_answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    attempt_id BIGINT NOT NULL,

    question_id BIGINT NOT NULL,

    selected_option_id BIGINT NULL,

    answer_text TEXT NULL,

    is_correct BOOLEAN NULL,

    points_earned DECIMAL(5,2) DEFAULT 0,

    answered_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_student_answer
        UNIQUE (attempt_id, question_id),

    CONSTRAINT fk_student_answers_attempt
        FOREIGN KEY (attempt_id)
        REFERENCES exam_attempts(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_student_answers_question
        FOREIGN KEY (question_id)
        REFERENCES questions(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_student_answers_option
        FOREIGN KEY (selected_option_id)
        REFERENCES question_options(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);


-- =========================================================
-- 7. KẾT QUẢ THI
-- =========================================================


CREATE TABLE exam_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    attempt_id BIGINT NOT NULL UNIQUE,

    student_id BIGINT NOT NULL,

    exam_id BIGINT NOT NULL,

    score DECIMAL(8,2) NOT NULL DEFAULT 0,

    percentage DECIMAL(5,2) DEFAULT 0,

    grade VARCHAR(10),

    correct_answers INT DEFAULT 0,

    wrong_answers INT DEFAULT 0,

    unanswered INT DEFAULT 0,

    completed_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_exam_results_attempt
        FOREIGN KEY (attempt_id)
        REFERENCES exam_attempts(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_exam_results_student
        FOREIGN KEY (student_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_exam_results_exam
        FOREIGN KEY (exam_id)
        REFERENCES exams(id)
        ON DELETE CASCADE
);


-- =========================================================
-- 8. BÀI TẬP UML
-- =========================================================


-- ---------------------------------------------------------
-- 8.1 UML ASSIGNMENTS
-- ---------------------------------------------------------

CREATE TABLE uml_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    course_id BIGINT NOT NULL,

    title VARCHAR(255) NOT NULL,

    description TEXT,

    instructions LONGTEXT,

    uml_type ENUM(
        'USE_CASE',
        'CLASS_DIAGRAM',
        'SEQUENCE_DIAGRAM',
        'ACTIVITY_DIAGRAM',
        'STATE_DIAGRAM',
        'ER_DIAGRAM',
        'OTHER'
    ) NOT NULL DEFAULT 'CLASS_DIAGRAM',

    max_score DECIMAL(5,2) NOT NULL DEFAULT 10.00,

    deadline DATETIME NULL,

    created_by BIGINT NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_uml_assignments_course
        FOREIGN KEY (course_id)
        REFERENCES courses(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_uml_assignments_creator
        FOREIGN KEY (created_by)
        REFERENCES users(id)
        ON DELETE RESTRICT
);


-- =========================================================
-- 9. SINH VIÊN NỘP BÀI UML
-- =========================================================


CREATE TABLE uml_submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    assignment_id BIGINT NOT NULL,

    student_id BIGINT NOT NULL,

    file_name VARCHAR(255),

    file_url VARCHAR(500),

    file_type VARCHAR(50),

    file_size BIGINT,

    submission_text LONGTEXT,

    submitted_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    status ENUM(
        'SUBMITTED',
        'PROCESSING',
        'GRADED',
        'FAILED'
    ) NOT NULL DEFAULT 'SUBMITTED',

    final_score DECIMAL(5,2) NULL,

    teacher_score DECIMAL(5,2) NULL,

    teacher_feedback TEXT NULL,

    CONSTRAINT fk_uml_submissions_assignment
        FOREIGN KEY (assignment_id)
        REFERENCES uml_assignments(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_uml_submissions_student
        FOREIGN KEY (student_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);


-- =========================================================
-- 10. AI CHẤM UML
-- =========================================================


-- ---------------------------------------------------------
-- 10.1 AI GRADING RESULTS
-- ---------------------------------------------------------

CREATE TABLE ai_grading_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    submission_id BIGINT NOT NULL,

    model_name VARCHAR(100),

    model_version VARCHAR(100),

    score DECIMAL(5,2),

    confidence DECIMAL(5,2),

    analysis LONGTEXT,

    strengths LONGTEXT,

    weaknesses LONGTEXT,

    suggestions LONGTEXT,

    raw_response LONGTEXT,

    graded_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    status ENUM(
        'PROCESSING',
        'COMPLETED',
        'FAILED'
    ) NOT NULL DEFAULT 'PROCESSING',

    CONSTRAINT fk_ai_grading_submission
        FOREIGN KEY (submission_id)
        REFERENCES uml_submissions(id)
        ON DELETE CASCADE
);


-- =========================================================
-- 11. CHI TIẾT TIÊU CHÍ CHẤM UML
-- =========================================================


CREATE TABLE uml_grading_criteria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    assignment_id BIGINT NOT NULL,

    criterion_name VARCHAR(255) NOT NULL,

    description TEXT,

    max_score DECIMAL(5,2) NOT NULL,

    weight DECIMAL(5,2) DEFAULT 1.00,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_uml_criteria_assignment
        FOREIGN KEY (assignment_id)
        REFERENCES uml_assignments(id)
        ON DELETE CASCADE
);


-- ---------------------------------------------------------
-- 11.1 AI SCORE THEO TIÊU CHÍ
-- ---------------------------------------------------------

CREATE TABLE ai_criterion_scores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    ai_grading_result_id BIGINT NOT NULL,

    criterion_id BIGINT NOT NULL,

    score DECIMAL(5,2) NOT NULL,

    feedback TEXT,

    CONSTRAINT fk_ai_criterion_result
        FOREIGN KEY (ai_grading_result_id)
        REFERENCES ai_grading_results(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_ai_criterion
        FOREIGN KEY (criterion_id)
        REFERENCES uml_grading_criteria(id)
        ON DELETE CASCADE
);


-- =========================================================
-- 12. TIẾN ĐỘ HỌC TẬP
-- =========================================================


CREATE TABLE lesson_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    lesson_id BIGINT NOT NULL,

    student_id BIGINT NOT NULL,

    is_completed BOOLEAN NOT NULL DEFAULT FALSE,

    progress_percent DECIMAL(5,2) DEFAULT 0,

    last_accessed_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    completed_at DATETIME NULL,

    CONSTRAINT uk_lesson_progress
        UNIQUE (lesson_id, student_id),

    CONSTRAINT fk_lesson_progress_lesson
        FOREIGN KEY (lesson_id)
        REFERENCES lessons(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_lesson_progress_student
        FOREIGN KEY (student_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);


-- =========================================================
-- 13. PHÂN QUYỀN RBAC NÂNG CAO
-- =========================================================


-- ---------------------------------------------------------
-- 13.1 PERMISSIONS
-- ---------------------------------------------------------

CREATE TABLE permissions (
    id INT AUTO_INCREMENT PRIMARY KEY,

    name VARCHAR(100) NOT NULL UNIQUE,

    description VARCHAR(255)
);


-- ---------------------------------------------------------
-- 13.2 ROLE PERMISSIONS
-- ---------------------------------------------------------

CREATE TABLE role_permissions (
    role_id INT NOT NULL,

    permission_id INT NOT NULL,

    PRIMARY KEY (role_id, permission_id),

    CONSTRAINT fk_role_permissions_role
        FOREIGN KEY (role_id)
        REFERENCES roles(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_role_permissions_permission
        FOREIGN KEY (permission_id)
        REFERENCES permissions(id)
        ON DELETE CASCADE
);


-- =========================================================
-- 14. INDEX TỐI ƯU DATABASE
-- =========================================================


CREATE INDEX idx_users_role
ON users(role_id);


CREATE INDEX idx_courses_teacher
ON courses(teacher_id);


CREATE INDEX idx_lessons_course
ON lessons(course_id);


CREATE INDEX idx_enrollments_student
ON enrollments(student_id);


CREATE INDEX idx_questions_bank
ON questions(question_bank_id);


CREATE INDEX idx_questions_difficulty
ON questions(difficulty);


CREATE INDEX idx_exam_questions_exam
ON exam_questions(exam_id);


CREATE INDEX idx_exam_attempts_student
ON exam_attempts(student_id);


CREATE INDEX idx_exam_attempts_exam
ON exam_attempts(exam_id);


CREATE INDEX idx_student_answers_attempt
ON student_answers(attempt_id);


CREATE INDEX idx_exam_results_student
ON exam_results(student_id);


CREATE INDEX idx_exam_results_exam
ON exam_results(exam_id);


CREATE INDEX idx_uml_submissions_student
ON uml_submissions(student_id);


CREATE INDEX idx_uml_submissions_assignment
ON uml_submissions(assignment_id);


CREATE INDEX idx_ai_grading_submission
ON ai_grading_results(submission_id);


-- =========================================================
-- 15. DỮ LIỆU ROLE MẶC ĐỊNH
-- =========================================================

INSERT INTO roles
    (name, description)
VALUES
    ('ADMIN', 'Quản trị viên hệ thống'),
    ('TEACHER', 'Giảng viên'),
    ('STUDENT', 'Sinh viên');


-- =========================================================
-- 16. DỮ LIỆU PERMISSION MẪU
-- =========================================================

INSERT INTO permissions
    (name, description)
VALUES
    ('USER_READ', 'Xem người dùng'),
    ('USER_CREATE', 'Tạo người dùng'),
    ('USER_UPDATE', 'Cập nhật người dùng'),
    ('USER_DELETE', 'Xóa người dùng'),

    ('COURSE_READ', 'Xem khóa học'),
    ('COURSE_CREATE', 'Tạo khóa học'),
    ('COURSE_UPDATE', 'Cập nhật khóa học'),
    ('COURSE_DELETE', 'Xóa khóa học'),

    ('QUESTION_READ', 'Xem ngân hàng câu hỏi'),
    ('QUESTION_CREATE', 'Tạo câu hỏi'),
    ('QUESTION_UPDATE', 'Cập nhật câu hỏi'),
    ('QUESTION_DELETE', 'Xóa câu hỏi'),
    ('QUESTION_IMPORT', 'Import câu hỏi từ Excel'),
    ('QUESTION_EXPORT', 'Export câu hỏi'),

    ('EXAM_READ', 'Xem đề thi'),
    ('EXAM_CREATE', 'Tạo đề thi'),
    ('EXAM_UPDATE', 'Cập nhật đề thi'),
    ('EXAM_DELETE', 'Xóa đề thi'),

    ('EXAM_TAKE', 'Làm bài thi'),
    ('EXAM_GRADE', 'Chấm bài thi'),

    ('UML_READ', 'Xem bài tập UML'),
    ('UML_CREATE', 'Tạo bài tập UML'),
    ('UML_SUBMIT', 'Nộp bài UML'),
    ('UML_GRADE', 'Chấm bài UML'),
    ('UML_AI_GRADE', 'AI chấm bài UML'),

    ('STATISTICS_READ', 'Xem thống kê');


-- =========================================================
-- 17. GÁN PERMISSION CHO ADMIN
-- =========================================================

INSERT INTO role_permissions (role_id, permission_id)
SELECT
    r.id,
    p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ADMIN';


-- =========================================================
-- 18. GÁN PERMISSION CHO TEACHER
-- =========================================================

INSERT INTO role_permissions (role_id, permission_id)
SELECT
    r.id,
    p.id
FROM roles r
JOIN permissions p
WHERE r.name = 'TEACHER'
AND p.name IN (
    'COURSE_READ',
    'COURSE_CREATE',
    'COURSE_UPDATE',
    'COURSE_DELETE',

    'QUESTION_READ',
    'QUESTION_CREATE',
    'QUESTION_UPDATE',
    'QUESTION_DELETE',
    'QUESTION_IMPORT',
    'QUESTION_EXPORT',

    'EXAM_READ',
    'EXAM_CREATE',
    'EXAM_UPDATE',
    'EXAM_DELETE',
    'EXAM_GRADE',

    'UML_READ',
    'UML_CREATE',
    'UML_GRADE',
    'UML_AI_GRADE',

    'STATISTICS_READ'
);


-- =========================================================
-- 19. GÁN PERMISSION CHO STUDENT
-- =========================================================

INSERT INTO role_permissions (role_id, permission_id)
SELECT
    r.id,
    p.id
FROM roles r
JOIN permissions p
WHERE r.name = 'STUDENT'
AND p.name IN (
    'COURSE_READ',
    'QUESTION_READ',
    'EXAM_READ',
    'EXAM_TAKE',

    'UML_READ',
    'UML_SUBMIT',

    'STATISTICS_READ'
);


-- =========================================================
-- 20. KIỂM TRA DATABASE
-- =========================================================

SHOW TABLES;
INSERT INTO roles (name, description)
VALUES
('ADMIN', 'Quản trị viên hệ thống'),
('TEACHER', 'Giảng viên'),
('STUDENT', 'Sinh viên');

INSERT INTO users
(username, password, email, full_name, avatar_url, role_id, is_active)
VALUES


-- ADMIN
(
    'admin01',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'admin@elearning.com',
    'Nguyễn Quản Trị',
    NULL,
    1,
    TRUE
),

-- TEACHER
(
    'teacher01',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'teacher01@elearning.com',
    'Nguyễn Văn An',
    NULL,
    2,
    TRUE
),

(
    'teacher02',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'teacher02@elearning.com',
    'Trần Thị Bình',
    NULL,
    2,
    TRUE
),
(
    'gv01',
    '123456',
    'gv01@gmail.com',
    'Trần Thị Bình',
    NULL,
    2,
    TRUE
),

-- STUDENTS
(
    'student01',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'student01@elearning.com',
    'Lê Văn Minh',
    NULL,
    3,
    TRUE
),
(
    'sv01',
    '123456',
    'sv01@elearning.com',
    'Nguyễn Quản Học',
    NULL,
    1,
    TRUE
),

(
    'student02',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'student02@elearning.com',
    'Phạm Thị Lan',
    NULL,
    3,
    TRUE
),

(
    'student03',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'student03@elearning.com',
    'Hoàng Văn Nam',
    NULL,
    3,
    TRUE
),

(
    'student04',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'student04@elearning.com',
    'Vũ Thị Hoa',
    NULL,
    3,
    TRUE
),

(
    'student05',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldL17lhWy',
    'student05@elearning.com',
    'Đỗ Minh Tuấn',
    NULL,
    3,
    TRUE
);
INSERT INTO courses
(title, description, thumbnail_url, teacher_id, status)
VALUES

(
    'Lập trình Java cơ bản',
    'Khóa học cung cấp kiến thức nền tảng về Java, OOP, Collections và xử lý ngoại lệ.',
    NULL,
    2,
    'PUBLISHED'
),

(
    'Phân tích và thiết kế hệ thống',
    'Khóa học về UML, Use Case, Class Diagram, Sequence Diagram và thiết kế cơ sở dữ liệu.',
    NULL,
    3,
    'PUBLISHED'
);

INSERT INTO lessons
(course_id, title, description, content, video_url, document_url, lesson_order)
VALUES

-- Java
(
    1,
    'Giới thiệu Java',
    'Tổng quan về Java và JVM.',
    'Nội dung bài học giới thiệu Java, JDK, JRE và JVM.',
    NULL,
    NULL,
    1
),

(
    1,
    'Lập trình hướng đối tượng',
    'Các khái niệm OOP.',
    'Class, Object, Encapsulation, Inheritance, Polymorphism.',
    NULL,
    NULL,
    2
),

(
    1,
    'Collections Framework',
    'Các cấu trúc dữ liệu trong Java.',
    'List, Set, Map và các implementation phổ biến.',
    NULL,
    NULL,
    3
),

-- UML
(
    2,
    'Tổng quan về UML',
    'Giới thiệu ngôn ngữ mô hình hóa UML.',
    'UML là ngôn ngữ mô hình hóa được sử dụng để phân tích và thiết kế hệ thống.',
    NULL,
    NULL,
    1
),

(
    2,
    'Use Case Diagram',
    'Phân tích yêu cầu bằng Use Case.',
    'Actor, Use Case, Include, Extend và Generalization.',
    NULL,
    NULL,
    2
),

(
    2,
    'Class Diagram',
    'Thiết kế lớp bằng UML.',
    'Class, Attribute, Method, Association, Aggregation, Composition và Inheritance.',
    NULL,
    NULL,
    3
),

(
    2,
    'Sequence Diagram',
    'Mô hình hóa tương tác giữa các đối tượng.',
    'Lifeline, Message, Activation và các loại tương tác.',
    NULL,
    NULL,
    4
);

INSERT INTO enrollments
(course_id, student_id, status)
VALUES

-- Khóa Java
(1, 4, 'ACTIVE'),
(1, 5, 'ACTIVE'),
(1, 6, 'COMPLETED'),
(1, 7, 'ACTIVE'),

-- Khóa UML
(2, 4, 'ACTIVE'),
(2, 5, 'ACTIVE'),
(2, 6, 'ACTIVE'),
(2, 7, 'COMPLETED'),
(2, 8, 'ACTIVE');

INSERT INTO question_banks
(course_id, name, description, created_by)
VALUES

(
    1,
    'Ngân hàng câu hỏi Java',
    'Các câu hỏi trắc nghiệm về Java cơ bản và OOP.',
    2
),

(
    2,
    'Ngân hàng câu hỏi UML',
    'Các câu hỏi trắc nghiệm về UML và phân tích thiết kế hệ thống.',
    3
);

INSERT INTO questions
(question_bank_id, content, question_type, difficulty, explanation, points, created_by)
VALUES

-- Java
(
    1,
    'Java là ngôn ngữ lập trình thuộc loại nào?',
    'SINGLE_CHOICE',
    'EASY',
    'Java là ngôn ngữ lập trình hướng đối tượng.',
    1.00,
    2
),

(
    1,
    'Từ khóa nào được sử dụng để tạo một lớp trong Java?',
    'SINGLE_CHOICE',
    'EASY',
    'Từ khóa class dùng để khai báo lớp.',
    1.00,
    2
),

(
    1,
    'Tính đóng gói trong OOP có ý nghĩa gì?',
    'SINGLE_CHOICE',
    'MEDIUM',
    'Encapsulation giúp che giấu dữ liệu và kiểm soát truy cập.',
    1.00,
    2
),

(
    1,
    'Interface trong Java có thể được một class triển khai bằng từ khóa nào?',
    'SINGLE_CHOICE',
    'MEDIUM',
    'Từ khóa implements được sử dụng để triển khai interface.',
    1.00,
    2
),

(
    1,
    'Cấu trúc dữ liệu nào lưu dữ liệu theo cặp key-value?',
    'SINGLE_CHOICE',
    'EASY',
    'Map lưu dữ liệu theo dạng key-value.',
    1.00,
    2
),

-- UML
(
    2,
    'UML là viết tắt của cụm từ nào?',
    'SINGLE_CHOICE',
    'EASY',
    'UML là Unified Modeling Language.',
    1.00,
    3
),

(
    2,
    'Biểu đồ nào thường được sử dụng để mô tả chức năng của hệ thống?',
    'SINGLE_CHOICE',
    'EASY',
    'Use Case Diagram mô tả chức năng và tương tác của actor với hệ thống.',
    1.00,
    3
),

(
    2,
    'Class Diagram dùng để mô tả điều gì?',
    'SINGLE_CHOICE',
    'EASY',
    'Class Diagram mô tả cấu trúc tĩnh của hệ thống.',
    1.00,
    3
),

(
    2,
    'Quan hệ nào biểu diễn quan hệ kế thừa trong UML?',
    'SINGLE_CHOICE',
    'MEDIUM',
    'Generalization biểu diễn quan hệ kế thừa.',
    1.00,
    3
),

(
    2,
    'Sequence Diagram tập trung mô tả yếu tố nào?',
    'SINGLE_CHOICE',
    'MEDIUM',
    'Sequence Diagram mô tả thứ tự tương tác giữa các đối tượng theo thời gian.',
    1.00,
    3
);

INSERT INTO question_options
(question_id, option_label, option_content, is_correct, option_order)
VALUES

-- Question 1
(1, 'A', 'Hướng đối tượng', TRUE, 1),
(1, 'B', 'Thuần thủ tục', FALSE, 2),
(1, 'C', 'Ngôn ngữ máy', FALSE, 3),
(1, 'D', 'Assembly', FALSE, 4),

-- Question 2
(2, 'A', 'object', FALSE, 1),
(2, 'B', 'class', TRUE, 2),
(2, 'C', 'new', FALSE, 3),
(2, 'D', 'struct', FALSE, 4),

-- Question 3
(3, 'A', 'Che giấu dữ liệu', TRUE, 1),
(3, 'B', 'Tăng tốc CPU', FALSE, 2),
(3, 'C', 'Tạo database', FALSE, 3),
(3, 'D', 'Biên dịch chương trình', FALSE, 4),

-- Question 4
(4, 'A', 'extends', FALSE, 1),
(4, 'B', 'inherits', FALSE, 2),
(4, 'C', 'implements', TRUE, 3),
(4, 'D', 'interface', FALSE, 4),

-- Question 5
(5, 'A', 'List', FALSE, 1),
(5, 'B', 'Set', FALSE, 2),
(5, 'C', 'Map', TRUE, 3),
(5, 'D', 'Queue', FALSE, 4),

-- Question 6
(6, 'A', 'Unified Modeling Language', TRUE, 1),
(6, 'B', 'Universal Model Language', FALSE, 2),
(6, 'C', 'Unified Management Language', FALSE, 3),
(6, 'D', 'User Modeling Language', FALSE, 4),

-- Question 7
(7, 'A', 'Class Diagram', FALSE, 1),
(7, 'B', 'Use Case Diagram', TRUE, 2),
(7, 'C', 'Sequence Diagram', FALSE, 3),
(7, 'D', 'ER Diagram', FALSE, 4),

-- Question 8
(8, 'A', 'Cấu trúc tĩnh của hệ thống', TRUE, 1),
(8, 'B', 'Tốc độ mạng', FALSE, 2),
(8, 'C', 'Giao diện website', FALSE, 3),
(8, 'D', 'Chi phí dự án', FALSE, 4),

-- Question 9
(9, 'A', 'Association', FALSE, 1),
(9, 'B', 'Aggregation', FALSE, 2),
(9, 'C', 'Generalization', TRUE, 3),
(9, 'D', 'Dependency', FALSE, 4),

-- Question 10
(10, 'A', 'Thứ tự tương tác theo thời gian', TRUE, 1),
(10, 'B', 'Cấu trúc database', FALSE, 2),
(10, 'C', 'Giao diện người dùng', FALSE, 3),
(10, 'D', 'Cấu hình server', FALSE, 4);

INSERT INTO exams
(course_id, title, description, duration_minutes, total_questions, total_points, start_time, end_time, status, created_by)
VALUES

(
    1,
    'Kiểm tra Java cơ bản',
    'Bài kiểm tra kiến thức Java và OOP.',
    30,
    5,
    5.00,
    NULL,
    NULL,
    'PUBLISHED',
    2
),

(
    2,
    'Kiểm tra UML cơ bản',
    'Bài kiểm tra kiến thức UML.',
    30,
    5,
    5.00,
    NULL,
    NULL,
    'PUBLISHED',
    3
);

INSERT INTO exam_questions
(exam_id, question_id, question_order, points)
VALUES

-- Exam Java
(1, 1, 1, 1.00),
(1, 2, 2, 1.00),
(1, 3, 3, 1.00),
(1, 4, 4, 1.00),
(1, 5, 5, 1.00),

-- Exam UML
(2, 6, 1, 1.00),
(2, 7, 2, 1.00),
(2, 8, 3, 1.00),
(2, 9, 4, 1.00),
(2, 10, 5, 1.00);
SELECT * FROM roles;
select * from users;

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM users;

SET FOREIGN_KEY_CHECKS = 1;
INSERT INTO users
(username, password, email, full_name, avatar_url, role_id, is_active)
VALUES
(
    'admin01',
    'YOUR_BCRYPT_HASH',
    'admin@elearning.com',
    'Nguyễn Quản Trị',
    NULL,
    1,
    TRUE
),
(
    'teacher01',
    'YOUR_BCRYPT_HASH',
    'teacher01@elearning.com',
    'Nguyễn Văn An',
    NULL,
    2,
    TRUE
),
(
    'teacher02',
    'YOUR_BCRYPT_HASH',
    'teacher02@elearning.com',
    'Trần Thị Bình',
    NULL,
    2,
    TRUE
),
(
    'student01',
    'YOUR_BCRYPT_HASH',
    'student01@elearning.com',
    'Lê Văn Minh',
    NULL,
    3,
    TRUE
),
(
    'student02',
    'YOUR_BCRYPT_HASH',
    'student02@elearning.com',
    'Phạm Thị Lan',
    NULL,
    3,
    TRUE
),
(
    'student03',
    'YOUR_BCRYPT_HASH',
    'student03@elearning.com',
    'Hoàng Văn Nam',
    NULL,
    3,
    TRUE
),
(
    'student04',
    'YOUR_BCRYPT_HASH',
    'student04@elearning.com',
    'Vũ Thị Hoa',
    NULL,
    3,
    TRUE
),
(
    'student05',
    'YOUR_BCRYPT_HASH',
    'student05@elearning.com',
    'Đỗ Minh Tuấn',
    NULL,
    3,
    TRUE
),
(
    'student06',
    'YOUR_BCRYPT_HASH',
    'student06@elearning.com',
    'Nguyễn Quang Học',
    NULL,
    3,
    TRUE
);
SET SQL_SAFE_UPDATES = 0;

DELETE FROM users;

SET SQL_SAFE_UPDATES = 1;
SELECT * FROM users;
SELECT
    u.id,
    u.username,
    u.full_name,
    u.role_id,
    r.name AS role
FROM users u
LEFT JOIN roles r ON u.role_id = r.id
ORDER BY u.id;


