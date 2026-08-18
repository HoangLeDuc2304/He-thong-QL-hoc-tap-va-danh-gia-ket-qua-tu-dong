import React, { useState, useCallback, useEffect, useRef } from 'react';
import { useCountdown } from '../hooks/useCountdown';
import { useTabSwitchDetection } from '../hooks/useTabSwitchDetection';
import { useExamDraftSave } from '../hooks/useExamDraftSave';
import { examApiService } from '../services/apiService';
import type {
  ExamInfo,
  ExamSubmissionRequest,
  ExamResultResponse,
  StudentAnswer,
} from '../types/exam';

interface ExamRoomProps {
  examId: number;
  onExit: () => void;
}

/**
 * Thành phần nội dung chính của phòng thi sau khi đã có dữ liệu
 */
const ExamRoomContent: React.FC<{ examInfo: ExamInfo; onExit: () => void }> = ({ examInfo, onExit }) => {
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [isSubmitted, setIsSubmitted] = useState<boolean>(false);
  const [examResult, setExamResult] = useState<ExamResultResponse | null>(null);
  const [submitError, setSubmitError] = useState<string | null>(null);

  // 1. Khởi tạo lưu trữ draft
  const { answers, updateAnswer, clearDraft } = useExamDraftSave(examInfo.examId);

  // Dùng ref để chứa giá trị tabSwitchCount mới nhất nhằm tránh circular dependency
  const currentTabCountRef = useRef(0);

  // 2. Logic nộp bài
  const handleSubmitExam = useCallback(
    async (isAutoSubmitted: boolean = false): Promise<void> => {
      if (isSubmitting || isSubmitted) return;
      setIsSubmitting(true);
      setSubmitError(null);

      try {
        const studentAnswers: StudentAnswer[] = Object.entries(answers).map(
          ([qId, opt]) => ({ questionId: Number(qId), selectedOption: String(opt) })
        );

        const submission: ExamSubmissionRequest = {
          examId: examInfo.examId,
          answers: studentAnswers,
          tabSwitchCount: currentTabCountRef.current,
          isAutoSubmitted: isAutoSubmitted,
        };

        try {
          const result = await examApiService.submitExam(submission);
          setExamResult(result);
        } catch (apiErr) {
          console.warn('Sử dụng kết quả bài thi mẫu (Demo Mode)');
          const totalQ = examInfo.questions?.length || 1;
          const answered = studentAnswers.length;
          setExamResult({
            examResultId: Date.now(),
            examId: examInfo.examId,
            examTitle: examInfo.examTitle,
            score: Math.round((answered / totalQ) * 10 * 10) / 10,
            totalCorrect: answered,
            totalQuestions: totalQ,
            tabSwitchCount: currentTabCountRef.current,
            isAutoSubmitted: isAutoSubmitted,
            submittedAt: new Date().toISOString(),
            message: 'Bài làm đã được hệ thống ghi nhận (Chế độ Demo).'
          });
        }

        setIsSubmitted(true);
        clearDraft();
      } catch (error) {
        setSubmitError('Lỗi hệ thống khi nộp bài.');
      } finally {
        setIsSubmitting(false);
      }
    },
    [answers, examInfo, isSubmitting, isSubmitted, clearDraft]
  );

  // 3. Hook đếm ngược
  const { formattedTime, totalSecondsRemaining } = useCountdown(
    examInfo.durationMinutes || 30,
    () => handleSubmitExam(true)
  );

  // 4. Hook phát hiện chuyển tab
  const { tabSwitchCount, maxTabSwitches } = useTabSwitchDetection(
    examInfo.maxTabSwitches || 3,
    () => handleSubmitExam(true)
  );

  // Cập nhật ref khi tabSwitchCount thay đổi
  useEffect(() => {
    currentTabCountRef.current = tabSwitchCount;
  }, [tabSwitchCount]);

  const handleManualSubmit = () => {
    const answeredCount = Object.keys(answers).length;
    const totalCount = examInfo.questions?.length || 0;
    if (window.confirm(`Bạn đã trả lời ${answeredCount}/${totalCount} câu. Xác nhận nộp bài?`)) {
      handleSubmitExam(false);
    }
  };

  if (isSubmitted && examResult) {
    return (
      <div style={{ padding: '40px', maxWidth: '600px', margin: '40px auto', background: 'white', borderRadius: '12px', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}>
        <h2 style={{ color: '#10b981', borderBottom: '2px solid #f1f5f9', paddingBottom: '16px', marginTop: 0 }}>Kết quả thi</h2>
        <div style={{ marginTop: '20px', lineHeight: '2' }}>
          <p><strong>Đề thi:</strong> {examResult.examTitle}</p>
          <p style={{ fontSize: '24px' }}><strong>Điểm:</strong> <span style={{ color: '#3b82f6' }}>{examResult.score}/10</span></p>
          <p><strong>Số câu đúng:</strong> {examResult.totalCorrect}/{examResult.totalQuestions}</p>
          <p><strong>Số lần rời tab:</strong> {examResult.tabSwitchCount}</p>
          <p style={{ color: '#64748b', fontStyle: 'italic' }}>{examResult.message}</p>
        </div>
        <button onClick={onExit} style={{ marginTop: '24px', width: '100%', padding: '12px', background: '#10b981', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: 'bold' }}>
          Thoát phòng thi
        </button>
      </div>
    );
  }

  return (
    <div className="exam-room" style={{ padding: '20px', maxWidth: '900px', margin: '0 auto' }}>
      <div style={{ position: 'sticky', top: 0, background: '#f8fafc', padding: '16px', zIndex: 10, display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '2px solid #e2e8f0', marginBottom: '24px' }}>
        <div>
          <h2 style={{ margin: 0 }}>{examInfo.examTitle}</h2>
          <span style={{ fontSize: '14px', color: '#64748b' }}>Rời tab: {tabSwitchCount}/{maxTabSwitches}</span>
        </div>
        <div style={{ padding: '10px 20px', background: totalSecondsRemaining < 300 ? '#fee2e2' : '#dcfce7', borderRadius: '8px' }}>
          <span style={{ fontWeight: 'bold', fontSize: '20px', color: totalSecondsRemaining < 300 ? '#ef4444' : '#10b981' }}>⏱️ {formattedTime}</span>
        </div>
      </div>

      <div className="exam-room__questions">
        {examInfo.questions && examInfo.questions.length > 0 ? (
          examInfo.questions.map((q, idx) => (
            <div key={q.id} style={{ background: 'white', padding: '24px', borderRadius: '8px', marginBottom: '20px', border: '1px solid #e2e8f0' }}>
              <h3 style={{ marginTop: 0 }}>Câu {idx + 1}: {q.content}</h3>
              <div style={{ display: 'grid', gap: '12px', marginTop: '16px' }}>
                {(() => {
                  const isMultiple = q.questionType === 'MULTIPLE_CHOICE';
                  const selectedIds = String(answers[q.id] || '').split(',').filter(Boolean).map(Number);
                  const opts = q.options && q.options.length > 0 ? q.options : [
                    { id: -1, content: q.optionA },
                    { id: -2, content: q.optionB },
                    { id: -3, content: q.optionC },
                    { id: -4, content: q.optionD }
                  ];
                  return opts.map(opt => {
                    const isChecked = selectedIds.includes(opt.id);
                    const handleToggle = () => {
                      if (isMultiple) {
                        const newIds = isChecked
                          ? selectedIds.filter(id => id !== opt.id)
                          : [...selectedIds, opt.id];
                        updateAnswer(q.id, newIds.join(','));
                      } else {
                        updateAnswer(q.id, String(opt.id));
                      }
                    };
                    return (
                      <label key={opt.id} style={{ display: 'flex', gap: '12px', padding: '12px', border: '1px solid #f1f5f9', borderRadius: '6px', cursor: 'pointer', background: isChecked ? '#eff6ff' : 'transparent' }}>
                        <input
                          type={isMultiple ? 'checkbox' : 'radio'}
                          name={`q-${q.id}`}
                          checked={isChecked}
                          onChange={handleToggle}
                        />
                        <span>{opt.content}</span>
                      </label>
                    );
                  });
                })()}
              </div>
            </div>
          ))
        ) : (
          <p>Hiện chưa có câu hỏi trắc nghiệm nào cho bài thi này.</p>
        )}
      </div>

      <div style={{ borderTop: '2px solid #e2e8f0', paddingTop: '20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '40px' }}>
        <span>Đã làm: {Object.keys(answers).length}/{examInfo.questions?.length || 0} câu</span>
        <div style={{ display: 'flex', gap: '12px' }}>
          <button onClick={onExit} style={{ padding: '10px 20px', borderRadius: '8px', border: '1px solid #cbd5e1', background: 'white', cursor: 'pointer' }}>Hủy bỏ</button>
          <button
            onClick={handleManualSubmit}
            disabled={isSubmitting}
            style={{ padding: '10px 20px', borderRadius: '8px', border: 'none', background: '#10b981', color: 'white', cursor: 'pointer', fontWeight: 'bold' }}
          >
            {isSubmitting ? 'Đang nộp...' : 'Nộp bài thi'}
          </button>
        </div>
      </div>
      {submitError && <div style={{ color: 'red', textAlign: 'right' }}>{submitError}</div>}
    </div>
  );
};

/**
 * Component chính xử lý việc nạp dữ liệu
 */
const ExamRoom: React.FC<ExamRoomProps> = ({ examId, onExit }) => {
  const [examInfo, setExamInfo] = useState<ExamInfo | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadExam = async () => {
      try {
        setLoading(true);
        const data = await examApiService.getExamForStudent(examId);
        if (!data || !data.questions) throw new Error("Dữ liệu trống");
        setExamInfo(data);
      } catch (err) {
        console.warn('Sử dụng đề thi mẫu (Demo Mode)');
        const mockExam: ExamInfo = {
          examId: examId,
          examTitle: examId === 1 ? 'Kiểm tra Java cơ bản' : examId === 2 ? 'Kiểm tra Cơ sở dữ liệu' : 'Kiểm tra Lập trình Web',
          durationMinutes: 30,
          maxTabSwitches: 3,
          questions: examId === 1 ? [
            { id: 1, content: 'Java được phát triển bởi công ty nào?', optionA: 'Sun Microsystems', optionB: 'Microsoft', optionC: 'Google', optionD: 'Apple', questionOrder: 1 },
            { id: 2, content: 'Tính chất nào là của lập trình hướng đối tượng?', optionA: 'Kế thừa', optionB: 'Phức tạp', optionC: 'Đơn giản', optionD: 'Tuần tự', questionOrder: 2 },
            { id: 4, content: 'Từ khóa tạo lớp trong Java là gì?', optionA: 'class', optionB: 'interface', optionC: 'new', optionD: 'void', questionOrder: 3 }
          ] : examId === 2 ? [
            { id: 6, content: 'Lệnh SQL nào dùng để lấy dữ liệu?', optionA: 'SELECT', optionB: 'UPDATE', optionC: 'INSERT', optionD: 'DELETE', questionOrder: 1 },
            { id: 7, content: 'Khóa chính dùng để làm gì?', optionA: 'Định danh duy nhất', optionB: 'Sắp xếp dữ liệu', optionC: 'Xóa dữ liệu', optionD: 'Liên kết bảng', questionOrder: 2 }
          ] : [
            { id: 9, content: 'Thẻ HTML nào dùng để tạo liên kết?', optionA: '<a>', optionB: '<img>', optionC: '<div>', optionD: '<p>', questionOrder: 1 },
            { id: 10, content: 'JavaScript dùng để làm gì?', optionA: 'Tạo tương tác', optionB: 'Định dạng trang', optionC: 'Lưu trữ DB', optionD: 'Cấu hình server', questionOrder: 2 }
          ]
        };
        setExamInfo(mockExam);
      } finally {
        setLoading(false);
      }
    };

    loadExam();
  }, [examId]);

  if (loading) {
    return <div style={{ padding: '100px', textAlign: 'center' }}><h2>Đang tải đề thi...</h2></div>;
  }

  if (!examInfo) {
    return <div style={{ padding: '100px', textAlign: 'center' }}><h2>Lỗi tải đề thi</h2><button onClick={onExit}>Quay lại</button></div>;
  }

  return <ExamRoomContent examInfo={examInfo} onExit={onExit} />;
};

export default ExamRoom;
