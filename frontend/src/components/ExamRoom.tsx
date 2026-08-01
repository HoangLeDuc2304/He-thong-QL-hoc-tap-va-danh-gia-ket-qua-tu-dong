import React, { useState, useCallback } from 'react';
import { useCountdown } from '../hooks/useCountdown';
import { useTabSwitchDetection } from '../hooks/useTabSwitchDetection';
import { useExamDraftSave } from '../hooks/useExamDraftSave';
import type {
  ExamInfo,
  ExamSubmissionRequest,
  ExamResultResponse,
  StudentAnswer,
} from '../types/exam';

/**
 * Props cho component ExamRoom.
 */
interface ExamRoomProps {
  /** Thông tin đề thi (bao gồm danh sách câu hỏi, thời gian, giới hạn chuyển tab) */
  examInfo: ExamInfo;
  /** Hàm gọi API nộp bài thi lên server */
  onSubmitExam: (submission: ExamSubmissionRequest) => Promise<ExamResultResponse>;
}

/**
 * Component phòng thi trực tuyến — tích hợp tất cả logic phòng thi.
 *
 * Tích hợp 3 custom hooks:
 * 1. useCountdown: Đếm ngược thời gian, tự động nộp bài khi hết giờ.
 * 2. useTabSwitchDetection: Phát hiện chuyển tab, tự động nộp bài khi vi phạm.
 * 3. useExamDraftSave: Lưu/khôi phục bài làm vào localStorage.
 *
 * Luồng hoạt động:
 * - Sinh viên vào phòng thi → khôi phục draft (nếu có) → bắt đầu đếm ngược.
 * - Chọn đáp án → tự động lưu draft vào localStorage.
 * - Chuyển tab → tăng bộ đếm, cảnh báo. Vượt giới hạn → nộp bài tự động.
 * - Hết giờ → nộp bài tự động.
 * - Nhấn "Nộp bài" → gọi API → nhận kết quả → xóa draft.
 */
const ExamRoom: React.FC<ExamRoomProps> = ({ examInfo, onSubmitExam }) => {
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [isSubmitted, setIsSubmitted] = useState<boolean>(false);
  const [examResult, setExamResult] = useState<ExamResultResponse | null>(null);
  const [submitError, setSubmitError] = useState<string | null>(null);

  /**
   * Hook 3: Lưu và khôi phục bài làm từ localStorage.
   */
  const {
    answers,
    updateAnswer,
    clearDraft,
    isRestoredFromDraft,
  } = useExamDraftSave(examInfo.examId);

  /**
   * Hàm nộp bài thi — được gọi thủ công hoặc tự động.
   *
   * @param isAutoSubmitted - true nếu nộ tự động (hết giờ hoặc vi phạm tab)
   */
  const handleSubmitExam = useCallback(
    async (isAutoSubmitted: boolean = false): Promise<void> => {
      if (isSubmitting || isSubmitted) {
        return;
      }

      setIsSubmitting(true);
      setSubmitError(null);

      try {
        const studentAnswers: StudentAnswer[] = Object.entries(answers).map(
          ([questionIdString, selectedOption]) => ({
            questionId: Number(questionIdString),
            selectedOption: selectedOption,
          })
        );

        const submission: ExamSubmissionRequest = {
          examId: examInfo.examId,
          answers: studentAnswers,
          tabSwitchCount: tabSwitchCount,
          isAutoSubmitted: isAutoSubmitted,
        };

        const result = await onSubmitExam(submission);

        setExamResult(result);
        setIsSubmitted(true);
        clearDraft();
      } catch (error) {
        const errorMessage =
          error instanceof Error ? error.message : 'Đã xảy ra lỗi khi nộp bài.';
        setSubmitError(errorMessage);
        console.error('[ExamRoom] Lỗi nộp bài:', error);
      } finally {
        setIsSubmitting(false);
      }
    },
    [answers, examInfo.examId, isSubmitting, isSubmitted, onSubmitExam, clearDraft]
  );

  /**
   * Hook 1: Đếm ngược thời gian làm bài.
   * Khi hết giờ, tự động gọi handleSubmitExam(true).
   */
  const { formattedTime, totalSecondsRemaining, isExpired } = useCountdown(
    examInfo.durationMinutes,
    () => handleSubmitExam(true)
  );

  /**
   * Hook 2: Phát hiện chuyển tab.
   * Khi vượt quá giới hạn, tự động nộp bài.
   */
  const { tabSwitchCount, maxTabSwitches, isLimitExceeded } =
    useTabSwitchDetection(examInfo.maxTabSwitches, () => handleSubmitExam(true));

  /**
   * Xử lý khi sinh viên chọn đáp án cho một câu hỏi.
   */
  const handleSelectOption = useCallback(
    (questionId: number, selectedOption: string): void => {
      if (isSubmitted) {
        return;
      }
      updateAnswer(questionId, selectedOption);
    },
    [isSubmitted, updateAnswer]
  );

  /**
   * Xử lý khi sinh viên nhấn nút "Nộp bài".
   */
  const handleManualSubmit = useCallback((): void => {
    const confirmedSubmit = window.confirm(
      `Bạn có chắc chắn muốn nộp bài?\n\n` +
      `Đã trả lời: ${Object.keys(answers).length}/${examInfo.questions.length} câu.\n` +
      `Thời gian còn lại: ${formattedTime}`
    );

    if (confirmedSubmit) {
      handleSubmitExam(false);
    }
  }, [answers, examInfo.questions.length, formattedTime, handleSubmitExam]);

  /**
   * Xác định class CSS cho timer dựa trên thời gian còn lại.
   * Dưới 5 phút: hiển thị cảnh báo (đỏ).
   * Dưới 10 phút: hiển thị chú ý (vàng).
   */
  const getTimerClassName = (): string => {
    if (totalSecondsRemaining <= 300) return 'exam-timer exam-timer--danger';
    if (totalSecondsRemaining <= 600) return 'exam-timer exam-timer--warning';
    return 'exam-timer';
  };

  // ===================== RENDER =====================

  /**
   * Hiển thị kết quả thi sau khi nộp bài thành công.
   */
  if (isSubmitted && examResult) {
    return (
      <div className="exam-result">
        <h2 className="exam-result__title">Kết quả thi</h2>
        <div className="exam-result__info">
          <p className="exam-result__exam-title">{examResult.examTitle}</p>
          <p className="exam-result__score">
            Điểm: <strong>{examResult.score}/10</strong>
          </p>
          <p className="exam-result__correct">
            Số câu đúng: {examResult.totalCorrect}/{examResult.totalQuestions}
          </p>
          <p className="exam-result__tab-switches">
            Số lần chuyển tab: {examResult.tabSwitchCount}
          </p>
          {examResult.isAutoSubmitted && (
            <p className="exam-result__auto-submitted">
              ⚠️ Bài thi đã được hệ thống tự động nộp.
            </p>
          )}
          <p className="exam-result__message">{examResult.message}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="exam-room">
      {/* ===== HEADER: Timer + Anti-cheat info ===== */}
      <div className="exam-room__header">
        <h1 className="exam-room__title">{examInfo.examTitle}</h1>

        <div className={getTimerClassName()}>
          <span className="exam-timer__icon">⏱️</span>
          <span className="exam-timer__time">{formattedTime}</span>
        </div>

        <div className="exam-room__anti-cheat">
          <span
            className={`exam-room__tab-count ${tabSwitchCount > 0 ? 'exam-room__tab-count--warned' : ''
              }`}
          >
            Rời tab: {tabSwitchCount}/{maxTabSwitches}
          </span>
        </div>

        {isRestoredFromDraft && (
          <div className="exam-room__draft-notice">
            ✅ Bài làm đã được khôi phục từ phiên trước.
          </div>
        )}
      </div>

      {/* ===== DANH SÁCH CÂU HỎI ===== */}
      <div className="exam-room__questions">
        {examInfo.questions.map((question) => (
          <div key={question.id} className="question-card">
            <h3 className="question-card__title">
              Câu {question.questionOrder}: {question.content}
            </h3>

            <div className="question-card__options">
              {(['A', 'B', 'C', 'D'] as const).map((optionLetter) => {
                const optionText =
                  optionLetter === 'A'
                    ? question.optionA
                    : optionLetter === 'B'
                      ? question.optionB
                      : optionLetter === 'C'
                        ? question.optionC
                        : question.optionD;

                const isSelected = answers[question.id] === optionLetter;

                return (
                  <label
                    key={optionLetter}
                    className={`question-card__option ${isSelected ? 'question-card__option--selected' : ''
                      }`}
                  >
                    <input
                      type="radio"
                      name={`question-${question.id}`}
                      value={optionLetter}
                      checked={isSelected}
                      onChange={() =>
                        handleSelectOption(question.id, optionLetter)
                      }
                      disabled={isSubmitted}
                      className="question-card__radio"
                    />
                    <span className="question-card__option-letter">
                      {optionLetter}.
                    </span>
                    <span className="question-card__option-text">
                      {optionText}
                    </span>
                  </label>
                );
              })}
            </div>
          </div>
        ))}
      </div>

      {/* ===== FOOTER: Nút nộp bài + Trạng thái ===== */}
      <div className="exam-room__footer">
        <div className="exam-room__progress">
          Đã trả lời: {Object.keys(answers).length}/{examInfo.questions.length} câu
        </div>

        {submitError && (
          <div className="exam-room__error">
            ❌ {submitError}
          </div>
        )}

        <button
          className="exam-room__submit-button"
          onClick={handleManualSubmit}
          disabled={isSubmitting || isSubmitted}
        >
          {isSubmitting ? 'Đang nộp bài...' : 'Nộp bài'}
        </button>
      </div>
    </div>
  );
};

export default ExamRoom;
