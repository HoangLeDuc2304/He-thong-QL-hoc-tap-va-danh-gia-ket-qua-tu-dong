-- =============================================================================
-- CƠ SỞ DỮ LIỆU HỆ THỐNG E-LEARNING, ĐÁNH GIÁ KẾT QUẢ HỌC TẬP VÀ QUẢN LÝ LỚP HỌC
-- Công nghệ: PostgreSQL
-- =============================================================================

-- =============================================================================
-- PHÂN HỆ 1: AUTHENTICATION SERVICE (XÁC THỰC, BẢO MẬT VÀ PHÂN QUYỀN RBAC)
-- =============================================================================

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
