import React, { useState, useEffect } from 'react';
import type { StudentInfo, EnrolledCourse, ExamForStudent, ActivityLog, DashboardStats } from '../types/student';
import { studentDashboardApiService } from '../services/apiService';
import '../styles/StudentDashboard.css';

const StudentDashboard: React.FC = () => {
  const [student, setStudent] = useState<StudentInfo | null>(null);
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [upcomingExams, setUpcomingExams] = useState<ExamForStudent[]>([]);
  const [recentActivities, setRecentActivities] = useState<ActivityLog[]>([]);
  const [topCourses, setTopCourses] = useState<EnrolledCourse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        setLoading(true);
        setError(null);

        try {
          // Thử gọi API thực tế từ Backend
          const studentInfo = await studentDashboardApiService.getStudentInfo();
          const studentData: StudentInfo = {
            id: studentInfo.userId,
            fullName: studentInfo.fullName,
            studentCode: studentInfo.studentId || 'SV001',
            email: studentInfo.email,
            phone: '0987654321',
            className: 'Lớp CNTT2026',
            avatar: `https://ui-avatars.com/api/?name=${encodeURIComponent(studentInfo.fullName)}&background=10b981&color=fff`
          };
          setStudent(studentData);

          const statsData = await studentDashboardApiService.getDashboardStats();
          setStats({
            totalCourses: statsData.totalCourses || 0,
            completedCourses: 0,
            totalExams: statsData.completedExams || 0,
            upcomingExams: 1,
            averageGrade: statsData.averageScore || 0,
            passedExams: statsData.completedExams || 0
          });

          const examsData = await studentDashboardApiService.getUpcomingExams();
          const now = Date.now();
          const trueUpcomingExams = examsData.filter((exam: any) => new Date(exam.startTime).getTime() > now);
          setUpcomingExams(trueUpcomingExams.map((exam: any) => ({
            id: exam.examId,
            title: exam.title,
            courseCode: 'N/A',
            courseName: exam.subjectTitle,
            type: 'Kiểm tra',
            startTime: new Date(exam.startTime).toLocaleString('vi-VN'),
            endTime: new Date(exam.endTime).toLocaleString('vi-VN'),
            duration: exam.durationMinutes,
            status: 'Sắp tới',
            questions: exam.questionCount ?? 0
          })));

          const coursesData = await studentDashboardApiService.getEnrolledCourses();
          setTopCourses(coursesData.map((course: any) => ({
            id: course.courseId,
            code: course.code,
            title: course.title,
            instructor: course.createdByName || 'Giảng viên',
            schedule: 'Thứ 2, 4, 6',
            room: 'P.402',
            credits: 3,
            status: 'Đang học',
            progress: 60,
            grade: 0
          })));

        } catch (apiErr) {
          // Nếu Backend chưa chạy hoặc lỗi API, sử dụng dữ liệu mẫu từ schema.sql
          console.warn('Backend API chưa sẵn sàng hoặc lỗi, sử dụng dữ liệu mẫu.');

          const storedName = localStorage.getItem('username') || 'Nguyễn Minh Anh';

          setStudent({
            id: 4,
            fullName: storedName,
            studentCode: 'SV001',
            email: 'sv001@elearning.vn',
            phone: '0912345678',
            className: 'Lớp CNTT2026',
            avatar: `https://ui-avatars.com/api/?name=${encodeURIComponent(storedName)}&background=10b981&color=fff`
          });

          setStats({
            totalCourses: 2,
            completedCourses: 1,
            totalExams: 2,
            upcomingExams: 1,
            averageGrade: 8.5,
            passedExams: 2
          });

          setUpcomingExams([
            {
              id: 3,
              title: 'Kiểm tra Lập trình Web',
              courseCode: 'WEB01',
              courseName: 'Lập trình Web',
              type: 'Kiểm tra',
              startTime: '22/08/2026 08:00',
              endTime: '22/08/2026 08:45',
              duration: 45,
              status: 'Sắp tới',
              questions: 3
            }
          ]);

          setTopCourses([
            {
              id: 1,
              code: 'CNTT2026',
              title: 'Công nghệ thông tin',
              instructor: 'Nguyễn Văn Toàn',
              schedule: 'Thứ 2, 4, 6',
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
              schedule: 'Thứ 3, 5',
              room: 'P.302',
              credits: 110,
              status: 'Đang học',
              progress: 0,
              grade: 0
            }
          ]);

          setRecentActivities([
            { id: 1, type: 'exam', title: 'Đã hoàn thành bài thi', description: 'Môn Lập trình Java - Điểm: 8.5', timestamp: '2 giờ trước', icon: '📝' },
            { id: 2, type: 'course', title: 'Đã tham gia khóa học mới', description: 'Kỹ thuật phần mềm', timestamp: 'Hôm qua', icon: '📚' }
          ]);
        }

      } catch (err: any) {
        setError(err.message || 'Lỗi hệ thống');
        console.error('Dashboard Error:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  if (loading) {
    return <div style={{ padding: '40px', textAlign: 'center' }}>Đang tải dữ liệu...</div>;
  }

  if (error) {
    return <div style={{ padding: '40px', color: '#ef4444', textAlign: 'center' }}>
      <h3>Đã xảy ra lỗi</h3>
      <p>{error}</p>
      <button onClick={() => window.location.reload()} className="teacher-button">Thử lại</button>
    </div>;
  }

  if (!student || !stats) {
    return <div style={{ padding: '40px', textAlign: 'center' }}>Không tìm thấy thông tin sinh viên.</div>;
  }

  return (
    <div className="student-dashboard">
      {/* Header với thông tin sinh viên */}
      <div className="dashboard-header">
        <div className="student-info">
          <img src={student.avatar} alt={student.fullName} className="student-avatar" />
          <div className="student-details">
            <h1>Xin chào, {student.fullName}!</h1>
            <p className="student-code">Mã sinh viên: {student.studentCode} | Lớp: {student.className}</p>
            <p className="student-contact">{student.email} | {student.phone}</p>
          </div>
        </div>
      </div>

      {/* Thống kê tổng quan */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon">📚</div>
          <div className="stat-content">
            <div className="stat-value">{stats.totalCourses}</div>
            <div className="stat-label">Khóa học</div>
            <div className="stat-subtitle">{stats.completedCourses} đã hoàn thành</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon">📝</div>
          <div className="stat-content">
            <div className="stat-value">{stats.totalExams}</div>
            <div className="stat-label">Kỳ thi</div>
            <div className="stat-subtitle">{stats.upcomingExams} sắp tới</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon">⭐</div>
          <div className="stat-content">
            <div className="stat-value">{stats.averageGrade.toFixed(1)}</div>
            <div className="stat-label">Điểm trung bình</div>
            <div className="stat-subtitle">{stats.passedExams}/{stats.totalExams} đạt</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon">✅</div>
          <div className="stat-content">
            <div className="stat-value">{stats.totalExams > 0 ? ((stats.passedExams / stats.totalExams) * 100).toFixed(0) : 0}%</div>
            <div className="stat-label">Tỉ lệ đạt</div>
            <div className="stat-subtitle">Học kỳ này</div>
          </div>
        </div>
      </div>

      {/* Kỳ thi sắp tới */}
      <div className="section">
        <div className="section-header">
          <h2>🗓️ Kỳ thi sắp tới</h2>
        </div>
        <div className="exams-list">
          {upcomingExams.length > 0 ? upcomingExams.map(exam => (
            <div key={exam.id} className="exam-card">
              <div className="exam-header">
                <h3>{exam.title}</h3>
                <span className={`exam-type ${exam.type.toLowerCase().replace(/\s+/g, '-')}`}>
                  {exam.type}
                </span>
              </div>
              <div className="exam-details">
                <div className="detail">
                  <span className="label">Môn học:</span>
                  <span className="value">{exam.courseName}</span>
                </div>
                <div className="detail">
                  <span className="label">Ngày thi:</span>
                  <span className="value">{exam.startTime.split(',')[0]}</span>
                </div>
                <div className="detail">
                  <span className="label">Thời gian:</span>
                  <span className="value">{exam.duration} phút ({exam.questions} câu)</span>
                </div>
              </div>
              <button className="exam-action-btn">Làm bài thi →</button>
            </div>
          )) : <p style={{ color: '#64748b', padding: '20px' }}>Hiện chưa có kỳ thi nào sắp tới.</p>}
        </div>
      </div>

      {/* Khóa học nổi bật */}
      <div className="section">
        <div className="section-header">
          <h2>📖 Khóa học của tôi</h2>
        </div>
        <div className="courses-grid">
          {topCourses.map(course => (
            <div key={course.id} className="course-card">
              <div className="course-header">
                <h3>{course.title}</h3>
                <span className={`course-status ${course.status.toLowerCase().replace(/\s+/g, '-')}`}>
                  {course.status}
                </span>
              </div>
              <div className="course-info">
                <p><strong>Giảng viên:</strong> {course.instructor}</p>
                <p><strong>Lịch:</strong> {course.schedule}, Phòng {course.room}</p>
              </div>
              <div className="progress-bar">
                <div className="progress-fill" style={{ width: `${course.progress}%` }}></div>
              </div>
              <div className="progress-label">
                Tiến độ: {course.progress}% {course.grade !== undefined && course.grade > 0 && `| Điểm: ${course.grade}`}
              </div>
              <button className="course-action-btn">Vào học ngay →</button>
            </div>
          ))}
        </div>
      </div>

      {/* Hoạt động gần đây */}
      <div className="section">
        <div className="section-header">
          <h2>📋 Hoạt động gần đây</h2>
        </div>
        <div className="activity-timeline">
          {recentActivities.length > 0 ? recentActivities.map(activity => (
            <div key={activity.id} className="activity-item">
              <div className="activity-icon">{activity.icon}</div>
              <div className="activity-content">
                <h4>{activity.title}</h4>
                <p>{activity.description}</p>
                <span className="activity-time">{activity.timestamp}</span>
              </div>
            </div>
          )) : <p style={{ color: '#64748b', padding: '20px' }}>Chưa có hoạt động nào gần đây.</p>}
        </div>
      </div>
    </div>
  );
};

export default StudentDashboard;
