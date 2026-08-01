
-- Tạo kiểu ENUM cho vai trò người dùng
CREATE TYPE user_role AS ENUM ('ADMIN', 'TEACHER', 'STUDENT');

-- Tạo kiểu ENUM cho độ khó câu hỏi
CREATE TYPE question_difficulty AS ENUM ('EASY', 'MEDIUM', 'HARD');

-- BẢNG USERS: Lưu thông tin người dùng (Admin, Giáo viên, Sinh viên)
CREATE TABLE users (
    id              BIGSERIAL       PRIMARY KEY,
    full_name       VARCHAR(150)    NOT NULL,
    email           VARCHAR(255)    NOT NULL UNIQUE,
    password        VARCHAR(255)    NOT NULL,
    role            user_role       NOT NULL DEFAULT 'STUDENT',
    avatar_url      VARCHAR(500),
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index để tìm kiếm người dùng theo vai trò
CREATE INDEX idx_users_role ON users (role);

-- =============================================================================
-- BẢNG COURSES: Lưu thông tin khóa học
-- =============================================================================
CREATE TABLE courses (
    id              BIGSERIAL       PRIMARY KEY,
    course_name     VARCHAR(255)    NOT NULL,
    description     TEXT,
    teacher_id      BIGINT          NOT NULL,
    is_published    BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_courses_teacher
        FOREIGN KEY (teacher_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Index để tìm khóa học theo giáo viên
CREATE INDEX idx_courses_teacher_id ON courses (teacher_id);

-- BẢNG LESSONS: Lưu thông tin bài học thuộc khóa học
CREATE TABLE lessons (
    id              BIGSERIAL       PRIMARY KEY,
    course_id       BIGINT          NOT NULL,
    lesson_title    VARCHAR(255)    NOT NULL,
    content         TEXT,
    chapter         VARCHAR(100),
    order_index     INTEGER         NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_lessons_course
        FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE
);

-- Index để truy vấn bài học theo khóa học và thứ tự
CREATE INDEX idx_lessons_course_id ON lessons (course_id);
CREATE INDEX idx_lessons_course_order ON lessons (course_id, order_index);

-- BẢNG QUESTION_BANK: Ngân hàng câu hỏi trắc nghiệm
-- Mỗi câu hỏi có 4 đáp án (A, B, C, D), đáp án đúng, và độ khó
CREATE TABLE question_bank (
    id              BIGSERIAL               PRIMARY KEY,
    course_id       BIGINT                  NOT NULL,
    chapter         VARCHAR(100),
    content         TEXT                    NOT NULL,
    option_a        VARCHAR(500)            NOT NULL,
    option_b        VARCHAR(500)            NOT NULL,
    option_c        VARCHAR(500)            NOT NULL,
    option_d        VARCHAR(500)            NOT NULL,
    correct_option  CHAR(1)                 NOT NULL CHECK (correct_option IN ('A', 'B', 'C', 'D')),
    difficulty      question_difficulty     NOT NULL DEFAULT 'MEDIUM',
    created_by      BIGINT                  NOT NULL,
    created_at      TIMESTAMP               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP               NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_question_bank_course
        FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE,

    CONSTRAINT fk_question_bank_created_by
        FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_question_bank_course_chapter_difficulty
    ON question_bank (course_id, chapter, difficulty);

CREATE INDEX idx_question_bank_course_difficulty
    ON question_bank (course_id, difficulty);

CREATE INDEX idx_question_bank_created_by
    ON question_bank (created_by);

-- BẢNG EXAMS: Lưu thông tin đề thi / bài kiểm tra
CREATE TABLE exams (
    id              BIGSERIAL       PRIMARY KEY,
    course_id       BIGINT          NOT NULL,
    exam_title      VARCHAR(255)    NOT NULL,
    description     TEXT,
    duration_minutes INTEGER        NOT NULL DEFAULT 60,
    max_tab_switches INTEGER        NOT NULL DEFAULT 3,
    start_time      TIMESTAMP       NOT NULL,
    end_time        TIMESTAMP       NOT NULL,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_by      BIGINT          NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_exams_course
        FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE,

    CONSTRAINT fk_exams_created_by
        FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE CASCADE,

    CONSTRAINT chk_exams_time_range
        CHECK (end_time > start_time)
);

CREATE INDEX idx_exams_course_id ON exams (course_id);

CREATE INDEX idx_exams_created_by ON exams (created_by);

-- BẢNG EXAM_QUESTIONS: Bảng trung gian lưu đề thi sau khi trộn
-- Mỗi bản ghi là một câu hỏi đã được gán vào đề thi cụ thể
CREATE TABLE exam_questions (
    id              BIGSERIAL       PRIMARY KEY,
    exam_id         BIGINT          NOT NULL,
    question_id     BIGINT          NOT NULL,
    question_order  INTEGER         NOT NULL DEFAULT 0,

    CONSTRAINT fk_exam_questions_exam
        FOREIGN KEY (exam_id) REFERENCES exams (id) ON DELETE CASCADE,

    CONSTRAINT fk_exam_questions_question
        FOREIGN KEY (question_id) REFERENCES question_bank (id) ON DELETE CASCADE,

    CONSTRAINT uq_exam_question_unique
        UNIQUE (exam_id, question_id)
);

CREATE INDEX idx_exam_questions_exam_id ON exam_questions (exam_id);
CREATE INDEX idx_exam_questions_exam_order ON exam_questions (exam_id, question_order);

-- BẢNG EXAM_RESULTS: Lưu kết quả thi và dữ liệu anti-cheat
CREATE TABLE exam_results (
    id                  BIGSERIAL       PRIMARY KEY,
    exam_id             BIGINT          NOT NULL,
    student_id          BIGINT          NOT NULL,
    score               NUMERIC(5, 2)   NOT NULL DEFAULT 0.00,
    total_correct       INTEGER         NOT NULL DEFAULT 0,
    total_questions     INTEGER         NOT NULL DEFAULT 0,
    answers_json        TEXT,
    tab_switch_count    INTEGER         NOT NULL DEFAULT 0,
    is_auto_submitted   BOOLEAN         NOT NULL DEFAULT FALSE,
    submitted_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at          TIMESTAMP,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_exam_results_exam
        FOREIGN KEY (exam_id) REFERENCES exams (id) ON DELETE CASCADE,

    CONSTRAINT fk_exam_results_student
        FOREIGN KEY (student_id) REFERENCES users (id) ON DELETE CASCADE,

    CONSTRAINT uq_exam_student_unique
        UNIQUE (exam_id, student_id),

    CONSTRAINT chk_score_range
        CHECK (score >= 0 AND score <= 10)
);

CREATE INDEX idx_exam_results_exam_id ON exam_results (exam_id);

CREATE INDEX idx_exam_results_student_id ON exam_results (student_id);

CREATE INDEX idx_exam_results_exam_student ON exam_results (exam_id, student_id);

ALTER TABLE exams ADD COLUMN IF NOT EXISTS show_answers_after_submit BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE exams ADD COLUMN IF NOT EXISTS pass_score NUMERIC(5,2) NOT NULL DEFAULT 5.00;

-- BẢNG COURSE_ENROLLMENTS: Sinh viên đăng ký khóa học
-- Mỗi sinh viên có thể đăng ký nhiều khóa, mỗi khóa có nhiều sinh viên
CREATE TABLE course_enrollments (
    id              BIGSERIAL       PRIMARY KEY,
    course_id       BIGINT          NOT NULL,
    student_id      BIGINT          NOT NULL,
    enrolled_at     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_enrollments_course
        FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE,

    CONSTRAINT fk_enrollments_student
        FOREIGN KEY (student_id) REFERENCES users (id) ON DELETE CASCADE,

    CONSTRAINT uq_enrollment_unique
        UNIQUE (course_id, student_id)
);

CREATE INDEX idx_enrollments_student_id ON course_enrollments (student_id);

CREATE INDEX idx_enrollments_course_id ON course_enrollments (course_id);

CREATE TYPE material_type AS ENUM ('VIDEO', 'DOCUMENT_URL', 'FILE_UPLOAD');

-- BẢNG LESSON_MATERIALS: Tài liệu đính kèm bài học
-- Mỗi bài học có thể có nhiều tài liệu (video, PDF, link...)
CREATE TABLE lesson_materials (
    id              BIGSERIAL       PRIMARY KEY,
    lesson_id       BIGINT          NOT NULL,
    material_type   material_type   NOT NULL,
    material_title  VARCHAR(255)    NOT NULL,
    material_url    VARCHAR(1000)   NOT NULL,
    description     TEXT,
    order_index     INTEGER         NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_materials_lesson
        FOREIGN KEY (lesson_id) REFERENCES lessons (id) ON DELETE CASCADE
);

CREATE INDEX idx_materials_lesson_id ON lesson_materials (lesson_id);

CREATE INDEX idx_materials_lesson_order ON lesson_materials (lesson_id, order_index);

