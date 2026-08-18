import React, { useState, useEffect } from 'react';
import type { ExamForStudent } from '../types/student';
import { studentDashboardApiService } from '../services/apiService';
import ExamRoom from './ExamRoom';
import '../styles/StudentExams.css';

const StudentExams: React.FC = () => {
  const [exams, setExams] = useState<ExamForStudent[]>([]);
  const [filter, setFilter] = useState<'upcoming' | 'ongoing' | 'completed'>('upcoming');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeExamId, setActiveExamId] = useState<number | null>(null);

  useEffect(() => {
    const fetchExams = async () => {
      try {
        setLoading(true);
        setError(null);

        try {
          const examsData = await studentDashboardApiService.getUpcomingExams();
          const now = Date.now();
          const upcomingExams: ExamForStudent[] = examsData.map((exam: any) => ({
            id: exam.examId,
            title: exam.title,
            courseCode: exam.subjectCode || 'N/A',
            courseName: exam.subjectTitle,
            type: 'Kiểm tra',
            startTime: exam.startTime,
            endTime: exam.endTime,
            duration: exam.durationMinutes,
            status: new Date(exam.endTime).getTime() < now
              ? 'Đã kết thúc'
              : new Date(exam.startTime).getTime() <= now ? 'Đang diễn ra' : 'Sắp tới',
            questions: exam.questionCount ?? 0
          }));
          setExams(upcomingExams);
        } catch (apiErr) {
          console.warn('Backend API chưa sẵn sàng, sử dụng dữ liệu bài thi mẫu.');
          // Dữ liệu mẫu đồng bộ với TeacherInterface và schema.sql
          setExams([
            {
              id: 1,
              title: 'Kiểm tra Java cơ bản',
              courseCode: 'JAVA01',
              courseName: 'Lập trình Java',
              type: 'Kiểm tra',
              startTime: new Date().toISOString(), // Đang diễn ra
              endTime: new Date(Date.now() + 30 * 60000).toISOString(),
              duration: 30,
              status: 'Đang diễn ra',
              questions: 5
            },
            {
              id: 2,
              title: 'Kiểm tra Cơ sở dữ liệu',
              courseCode: 'DB01',
              courseName: 'Cơ sở dữ liệu',
              type: 'Kiểm tra',
              startTime: new Date(Date.now() + 86400000).toISOString(),
              endTime: new Date(Date.now() + 86500000).toISOString(),
              duration: 30,
              status: 'Sắp tới',
              questions: 3
            },
            {
              id: 3,
              title: 'Kiểm tra Lập trình Web',
              courseCode: 'WEB01',
              courseName: 'Lập trình Web',
              type: 'Thi giữa kỳ',
              startTime: new Date(Date.now() + 172800000).toISOString(),
              endTime: new Date(Date.now() + 172900000).toISOString(),
              duration: 45,
              status: 'Sắp tới',
              questions: 10
            }
          ]);
        }
      } catch (err: any) {
        setError(err.message || 'Lỗi hệ thống');
      } finally {
        setLoading(false);
      }
    };

    fetchExams();
  }, []);

  const handleStartExam = (id: number) => {
    setActiveExamId(id);
  };

  if (activeExamId) {
    return <ExamRoom examId={activeExamId} onExit={() => setActiveExamId(null)} />;
  }

  const filteredExams = exams.filter(exam => {
    if (filter === 'upcoming') return exam.status === 'Sắp tới';
    if (filter === 'ongoing') return exam.status === 'Đang diễn ra';
    if (filter === 'completed') return exam.status === 'Đã kết thúc';
    return true;
  });

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'Sắp tới': return 'status-upcoming';
      case 'Đang diễn ra': return 'status-ongoing';
      case 'Đã kết thúc': return 'status-completed';
      default: return '';
    }
  };

  if (loading) return <div style={{ padding: '40px', textAlign: 'center' }}>Đang tải danh sách bài thi...</div>;

  return (
    <div className="student-exams">
      <div className="exams-header">
        <h1>Bài kiểm tra của tôi</h1>
      </div>

      <div className="exams-filter">
        <button className={`filter-btn ${filter === 'ongoing' ? 'active' : ''}`} onClick={() => setFilter('ongoing')}>
          Đang diễn ra ({exams.filter(e => e.status === 'Đang diễn ra').length})
        </button>
        <button className={`filter-btn ${filter === 'upcoming' ? 'active' : ''}`} onClick={() => setFilter('upcoming')}>
          Sắp tới ({exams.filter(e => e.status === 'Sắp tới').length})
        </button>
        <button className={`filter-btn ${filter === 'completed' ? 'active' : ''}`} onClick={() => setFilter('completed')}>
          Đã kết thúc ({exams.filter(e => e.status === 'Đã kết thúc').length})
        </button>
      </div>

      <div className="exams-container">
        {filteredExams.length === 0 ? (
          <div className="no-exams"><p>Không có bài kiểm tra nào trong mục này.</p></div>
        ) : (
          filteredExams.map(exam => (
            <div key={exam.id} className="exam-card-detailed">
              <div className="exam-card-header">
                <div className="exam-title-section">
                  <h3>{exam.title}</h3>
                  <p className="exam-course">{exam.courseName} ({exam.courseCode})</p>
                </div>
                <span className={`exam-status-badge ${getStatusColor(exam.status)}`}>{exam.status}</span>
              </div>

              <div className="exam-details-grid">
                <div className="detail-item">
                  <span className="detail-label">📅 Ngày thi</span>
                  <span className="detail-value">{new Date(exam.startTime).toLocaleDateString('vi-VN')}</span>
                </div>
                <div className="detail-item">
                  <span className="detail-label">⏱️ Thời lượng</span>
                  <span className="detail-value">{exam.duration} phút</span>
                </div>
                <div className="detail-item">
                  <span className="detail-label">❓ Câu hỏi</span>
                  <span className="detail-value">{exam.questions} câu</span>
                </div>
              </div>

              <div className="exam-actions">
                {exam.status === 'Đang diễn ra' ? (
                  <button className="btn-primary btn-exam btn-urgent" onClick={() => handleStartExam(exam.id)}>
                    Vào làm bài ngay →
                  </button>
                ) : (
                  <button className="btn-secondary" disabled={exam.status === 'Đã kết thúc'}>
                    {exam.status === 'Sắp tới' ? 'Chưa đến giờ' : 'Đã đóng'}
                  </button>
                )}
                <button className="btn-secondary">Xem hướng dẫn</button>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default StudentExams;
