-- Seed data for the teacher dashboard. Safe to run when these IDs already exist.

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
(12, 4, 3, 'Software Engineering', 'UML được sử dụng chủ yếu để làm gì?', 'SINGLE_CHOICE', 'EASY')
ON CONFLICT (id) DO NOTHING;

INSERT INTO question_options (id, question_id, content, is_correct)
VALUES
(1, 1, 'Microsoft', FALSE), (2, 1, 'Sun Microsystems', TRUE), (3, 1, 'Apple', FALSE), (4, 1, 'IBM', FALSE),
(5, 2, 'Inheritance', TRUE), (6, 2, 'Encapsulation', FALSE), (7, 2, 'Compilation', FALSE), (8, 2, 'Iteration', FALSE),
(9, 3, 'Encapsulation', TRUE), (10, 3, 'Inheritance', TRUE), (11, 3, 'Polymorphism', TRUE), (12, 3, 'Compilation', FALSE),
(13, 4, 'class', TRUE), (14, 4, 'object', FALSE), (15, 4, 'newclass', FALSE), (16, 4, 'struct', FALSE),
(17, 5, 'boolean', TRUE), (18, 5, 'int', FALSE), (19, 5, 'String', FALSE), (20, 5, 'double', FALSE),
(21, 6, 'SELECT', TRUE), (22, 6, 'INSERT', FALSE), (23, 6, 'DELETE', FALSE), (24, 6, 'UPDATE', FALSE),
(25, 7, 'Xác định duy nhất một bản ghi', TRUE), (26, 7, 'Lưu file', FALSE), (27, 7, 'Tạo giao diện', FALSE), (28, 7, 'Mã hóa mật khẩu', FALSE),
(29, 8, 'Kết hợp dữ liệu từ nhiều bảng', TRUE), (30, 8, 'Xóa bảng', FALSE), (31, 8, 'Tạo database', FALSE), (32, 8, 'Tạo user', FALSE),
(33, 9, '<a>', TRUE), (34, 9, '<p>', FALSE), (35, 9, '<div>', FALSE), (36, 9, '<link>', FALSE),
(37, 10, 'Tạo tương tác cho trang web', TRUE), (38, 10, 'Quản lý database trực tiếp', FALSE), (39, 10, 'Thay thế hệ điều hành', FALSE), (40, 10, 'Tạo máy chủ vật lý', FALSE),
(41, 11, 'String', TRUE), (42, 11, 'Number', TRUE), (43, 11, 'Boolean', TRUE), (44, 11, 'HTML', FALSE),
(45, 12, 'Mô hình hóa và trực quan hóa hệ thống', TRUE), (46, 12, 'Chỉ dùng để viết code Java', FALSE), (47, 12, 'Quản lý máy chủ', FALSE), (48, 12, 'Lưu trữ file', FALSE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO exams
(id, subject_id, created_by, title, duration_minutes, start_time, end_time, max_tab_switches, status)
VALUES
(1, 1, 2, 'Kiểm tra Java cơ bản', 30, '2026-08-20 08:00:00+07', '2026-08-20 09:00:00+07', 3, 'PUBLISHED'),
(2, 2, 2, 'Kiểm tra Cơ sở dữ liệu', 30, '2026-08-21 08:00:00+07', '2026-08-21 09:00:00+07', 3, 'PUBLISHED'),
(3, 3, 3, 'Kiểm tra Lập trình Web', 45, '2026-08-22 08:00:00+07', '2026-08-22 09:00:00+07', 3, 'PUBLISHED')
ON CONFLICT (id) DO NOTHING;

INSERT INTO exam_configurations (id, exam_id, chapter_topic, difficulty, question_count)
VALUES
(1, 1, 'Java cơ bản', 'EASY', 2), (2, 1, 'OOP', 'MEDIUM', 1),
(3, 2, 'SQL', 'EASY', 2), (4, 2, 'Database', 'MEDIUM', 1),
(5, 3, 'HTML', 'EASY', 1), (6, 3, 'JavaScript', 'MEDIUM', 1)
ON CONFLICT (id) DO NOTHING;

INSERT INTO exam_questions (exam_id, question_id, order_index)
VALUES
(1, 1, 1), (1, 2, 2), (1, 3, 3), (1, 4, 4), (1, 5, 5),
(2, 6, 1), (2, 7, 2), (2, 8, 3),
(3, 9, 1), (3, 10, 2), (3, 11, 3)
ON CONFLICT (exam_id, question_id) DO NOTHING;

SELECT setval('question_bank_id_seq', COALESCE((SELECT MAX(id) FROM question_bank), 1));
SELECT setval('question_options_id_seq', COALESCE((SELECT MAX(id) FROM question_options), 1));
SELECT setval('exams_id_seq', COALESCE((SELECT MAX(id) FROM exams), 1));
SELECT setval('exam_configurations_id_seq', COALESCE((SELECT MAX(id) FROM exam_configurations), 1));
