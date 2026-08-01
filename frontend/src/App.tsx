import React, { useState } from 'react';
import ExamRoom from './components/ExamRoom';
import { examApiService } from './services/apiService';
import type { ExamInfo, ExamSubmissionRequest, ExamResultResponse } from './types/exam';
import './App.css';

/**
 * Dữ liệu đề thi mẫu (mock data) để demo phòng thi.
 * Trong thực tế, dữ liệu này sẽ được lấy từ API backend.
 */
const MOCK_EXAM_INFO: ExamInfo = {
  examId: 1,
  examTitle: 'Bài kiểm tra Chương 1 - Nhập môn Lập trình',
  durationMinutes: 2,
  maxTabSwitches: 3,
  questions: [
    {
      id: 1,
      content: 'Ngôn ngữ lập trình Java thuộc loại ngôn ngữ nào?',
      optionA: 'Ngôn ngữ máy',
      optionB: 'Ngôn ngữ biên dịch',
      optionC: 'Ngôn ngữ thông dịch',
      optionD: 'Ngôn ngữ hướng đối tượng',
      questionOrder: 1,
    },
    {
      id: 2,
      content: 'Keyword nào dùng để khai báo hằng số trong Java?',
      optionA: 'const',
      optionB: 'final',
      optionC: 'static',
      optionD: 'immutable',
      questionOrder: 2,
    },
    {
      id: 3,
      content: 'JVM là viết tắt của gì?',
      optionA: 'Java Virtual Machine',
      optionB: 'Java Version Manager',
      optionC: 'Java Visual Module',
      optionD: 'Java Verified Method',
      questionOrder: 3,
    },
    {
      id: 4,
      content: 'Phương thức nào là điểm bắt đầu của chương trình Java?',
      optionA: 'start()',
      optionB: 'run()',
      optionC: 'main()',
      optionD: 'init()',
      questionOrder: 4,
    },
    {
      id: 5,
      content: 'Kiểu dữ liệu nào lưu trữ số thực trong Java?',
      optionA: 'int',
      optionB: 'boolean',
      optionC: 'double',
      optionD: 'char',
      questionOrder: 5,
    },
  ],
};

/**
 * Component App chính — điểm vào của ứng dụng.
 *
 * Hiện tại hiển thị phòng thi demo với dữ liệu mẫu.
 * Trong thực tế, sẽ có routing để chuyển giữa các trang
 * (đăng nhập, danh sách đề thi, phòng thi, kết quả...).
 */
const App: React.FC = () => {
  const [isExamStarted, setIsExamStarted] = useState<boolean>(false);

  /**
   * Hàm nộp bài thi — gọi API backend hoặc mock response.
   * Trong demo, trả về kết quả giả lập.
   */
  const handleSubmitExam = async (
    submission: ExamSubmissionRequest
  ): Promise<ExamResultResponse> => {
    console.log('[App] Đang nộp bài thi:', submission);

    /**
     * Trong production, gọi API thực:
     * return examApiService.submitExam(submission);
     *
     * Demo: trả về mock response sau 1 giây (giả lập network delay).
     */
    return new Promise((resolve) => {
      setTimeout(() => {
        const totalQuestions = MOCK_EXAM_INFO.questions.length;
        const totalCorrect = Math.floor(Math.random() * totalQuestions) + 1;
        const score = parseFloat(
          ((totalCorrect / totalQuestions) * 10).toFixed(2)
        );

        resolve({
          examResultId: 1,
          examId: submission.examId,
          examTitle: MOCK_EXAM_INFO.examTitle,
          score: score,
          totalCorrect: totalCorrect,
          totalQuestions: totalQuestions,
          tabSwitchCount: submission.tabSwitchCount,
          isAutoSubmitted: submission.isAutoSubmitted,
          submittedAt: new Date().toISOString(),
          message: `Điểm của bạn: ${score}/10. Số câu đúng: ${totalCorrect}/${totalQuestions}.`,
        });
      }, 1000);
    });
  };

  if (!isExamStarted) {
    return (
      <div className="app">
        <div className="start-screen">
          <div className="start-screen__card">
            <h1 className="start-screen__title">
              📚 Hệ thống Quản lý Học tập
            </h1>
            <h2 className="start-screen__subtitle">
              E-Learning & Automated Assessment
            </h2>

            <div className="start-screen__exam-info">
              <h3 className="start-screen__exam-title">
                {MOCK_EXAM_INFO.examTitle}
              </h3>
              <div className="start-screen__details">
                <p>⏱️ Thời gian: <strong>{MOCK_EXAM_INFO.durationMinutes} phút</strong></p>
                <p>📝 Số câu hỏi: <strong>{MOCK_EXAM_INFO.questions.length} câu</strong></p>
                <p>🚫 Giới hạn chuyển tab: <strong>{MOCK_EXAM_INFO.maxTabSwitches} lần</strong></p>
              </div>
            </div>

            <div className="start-screen__rules">
              <h4>📋 Lưu ý khi làm bài:</h4>
              <ul>
                <li>Không chuyển tab trong quá trình thi.</li>
                <li>Bài thi sẽ tự động nộp khi hết giờ.</li>
                <li>Bài làm được tự động lưu nháp.</li>
                <li>Mỗi sinh viên chỉ được nộp bài 1 lần.</li>
              </ul>
            </div>

            <button
              className="start-screen__button"
              onClick={() => setIsExamStarted(true)}
            >
              🚀 Bắt đầu làm bài
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="app">
      <ExamRoom examInfo={MOCK_EXAM_INFO} onSubmitExam={handleSubmitExam} />
    </div>
  );
};

export default App;
