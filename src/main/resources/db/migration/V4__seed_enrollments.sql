-- Seed enrollments so students actually appear registered in their courses.
INSERT INTO enrollments (id, course_id, student_id)
VALUES
(1, 1, 4),
(2, 1, 5),
(3, 1, 6),
(4, 1, 7),
(5, 2, 4),
(6, 2, 5),
(7, 2, 6)
ON CONFLICT (id) DO NOTHING;

SELECT setval('enrollments_id_seq', COALESCE((SELECT MAX(id) FROM enrollments), 1));
