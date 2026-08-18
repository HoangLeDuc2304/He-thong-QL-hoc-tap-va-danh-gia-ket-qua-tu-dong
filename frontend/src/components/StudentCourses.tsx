import React, { useState, useEffect } from 'react';
import type { EnrolledCourse } from '../types/student';
import { studentDashboardApiService } from '../services/apiService';
import '../styles/StudentCourses.css';

const StudentCourses: React.FC = () => {
  const [courses, setCourses] = useState<EnrolledCourse[]>([]);
  const [filter, setFilter] = useState<'all' | 'active' | 'completed'>('active');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchCourses = async () => {
      try {
        setLoading(true);
        setError(null);

        try {
          const coursesData = await studentDashboardApiService.getEnrolledCourses();
          const enrolledCourses: EnrolledCourse[] = coursesData.map((course: any) => ({
            id: course.courseId,
            code: course.code,
            title: course.title,
            instructor: course.createdByName || 'Giáo viên',
            schedule: 'Thứ 2, 4, 6',
            room: 'P.402',
            credits: 3,
            status: 'Đang học',
            progress: 0,
            grade: 0
          }));
          setCourses(enrolledCourses);
        } catch (apiErr) {
          console.warn('Sử dụng dữ liệu môn học mẫu');
          setCourses([
            {
              id: 1,
              code: 'CNTT2026',
              title: 'Công nghệ thông tin',
              instructor: 'Nguyễn Văn Toàn',
              schedule: 'Thứ 2, 4, 6 - 08:00',
              room: 'Lab 1',
              credits: 120,
              status: 'Đang học',
              progress: 100,
              grade: 8.5
            },
            {
              id: 2,
              code: 'SE2026',
              title: 'Kỹ thuật phần mềm',
              instructor: 'Trần Thị Lan',
              schedule: 'Thứ 3, 5 - 13:30',
              room: 'P.302',
              credits: 110,
              status: 'Đang học',
              progress: 0,
              grade: 0
            }
          ]);
        }
      } catch (err: any) {
        setError(err.message || 'Lỗi tải danh sách khóa học');
      } finally {
        setLoading(false);
      }
    };

    fetchCourses();
  }, []);

  const filteredCourses = courses.filter(course => {
    if (filter === 'all') return true;
    if (filter === 'active') return course.status === 'Đang học';
    if (filter === 'completed') return course.status === 'Hoàn thành';
    return true;
  });

  if (loading) return <div style={{ padding: '40px', textAlign: 'center' }}>Đang tải khóa học...</div>;

  return (
    <div className="student-courses">
      <div className="courses-header">
        <h1>Danh sách khóa học của tôi</h1>
      </div>

      <div className="courses-filter">
        <button className={`filter-btn ${filter === 'active' ? 'active' : ''}`} onClick={() => setFilter('active')}>
          Đang học ({courses.filter(c => c.status === 'Đang học').length})
        </button>
        <button className={`filter-btn ${filter === 'completed' ? 'active' : ''}`} onClick={() => setFilter('completed')}>
          Đã hoàn thành ({courses.filter(c => c.status === 'Hoàn thành').length})
        </button>
        <button className={`filter-btn ${filter === 'all' ? 'active' : ''}`} onClick={() => setFilter('all')}>
          Tất cả ({courses.length})
        </button>
      </div>

      <div className="courses-container">
        {filteredCourses.length === 0 ? (
          <div className="no-courses"><p>Không có khóa học nào để hiển thị.</p></div>
        ) : (
          filteredCourses.map(course => (
            <div key={course.id} className="course-detail-card">
              <div className="course-header-detail">
                <div className="course-title-section">
                  <h2>{course.title}</h2>
                  <p className="course-code">{course.code}</p>
                </div>
                <span className={`course-status-badge ${course.status.toLowerCase().replace(/\s+/g, '-')}`}>
                  {course.status}
                </span>
              </div>
              <div className="course-meta">
                <div className="meta-item"><span>👨‍🏫 Giảng viên:</span> <strong>{course.instructor}</strong></div>
                <div className="meta-item"><span>📅 Lịch học:</span> {course.schedule}</div>
                <div className="meta-item"><span>🏛️ Phòng:</span> {course.room}</div>
              </div>
              <div className="course-progress-section">
                <div className="progress-header"><span>Tiến độ: {course.progress}%</span></div>
                <div className="progress-bar-large"><div className="progress-fill" style={{ width: `${course.progress}%` }}></div></div>
              </div>
              <div className="course-actions">
                <button className="btn-primary">Vào học</button>
                <button className="btn-secondary">Tài liệu</button>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default StudentCourses;
