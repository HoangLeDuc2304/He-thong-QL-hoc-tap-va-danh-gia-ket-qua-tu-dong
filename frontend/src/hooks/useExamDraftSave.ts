import { useState, useEffect, useCallback } from 'react';
import type { ExamDraft } from '../types/exam';

/**
 * Giá trị trả về từ hook useExamDraftSave.
 */
interface UseExamDraftSaveReturn {
  /** Trạng thái đáp án hiện tại: Map<questionId, selectedOption> */
  answers: Record<number, string>;
  /** Cập nhật đáp án cho một câu hỏi cụ thể */
  updateAnswer: (questionId: number, selectedOption: string) => void;
  /** Xóa draft khỏi localStorage (sau khi nộp bài thành công) */
  clearDraft: () => void;
  /** Draft đã được khôi phục từ localStorage hay chưa */
  isRestoredFromDraft: boolean;
}

/**
 * Tiền tố (prefix) cho key trong localStorage.
 * Mỗi đề thi có key riêng dạng: "exam_draft_<examId>"
 */
const LOCAL_STORAGE_KEY_PREFIX = 'exam_draft_';

/**
 * Custom Hook lưu và khôi phục trạng thái bài làm từ localStorage.
 *
 * Chức năng:
 * 1. Khi component mount: Kiểm tra localStorage có draft cho examId hiện tại không.
 *    Nếu có, tự động khôi phục trạng thái đáp án (answers) từ draft.
 * 2. Mỗi khi sinh viên chọn/thay đổi đáp án: Tự động lưu toàn bộ trạng thái
 *    vào localStorage ngay lập tức.
 * 3. Khi nộp bài thành công: Gọi clearDraft() để xóa draft khỏi localStorage.
 *
 * Ưu điểm:
 * - Sinh viên F5 hoặc tải lại trang → bài làm không bị mất.
 * - Sinh viên đóng trình duyệt rồi mở lại → bài làm vẫn còn.
 * - Mỗi đề thi lưu draft riêng biệt, không ảnh hưởng nhau.
 *
 * @param examId - ID đề thi hiện tại
 * @returns Object chứa answers, updateAnswer, clearDraft, isRestoredFromDraft
 *
 * @example
 * ```tsx
 * const { answers, updateAnswer, clearDraft, isRestoredFromDraft } = useExamDraftSave(examId);
 *
 * // Khi sinh viên chọn đáp án
 * const handleSelectOption = (questionId: number, option: string) => {
 *   updateAnswer(questionId, option);
 * };
 *
 * // Sau khi nộp bài thành công
 * const handleSubmitSuccess = () => {
 *   clearDraft();
 * };
 * ```
 */
export function useExamDraftSave(examId: number): UseExamDraftSaveReturn {
  const [answers, setAnswers] = useState<Record<number, string>>({});
  const [isRestoredFromDraft, setIsRestoredFromDraft] = useState<boolean>(false);

  /**
   * Tạo key localStorage cho đề thi hiện tại.
   * Ví dụ: examId = 5 → key = "exam_draft_5"
   */
  const storageKey = `${LOCAL_STORAGE_KEY_PREFIX}${examId}`;

  /**
   * Effect khôi phục draft: Chạy 1 lần khi component mount hoặc examId thay đổi.
   *
   * Luồng:
   * 1. Đọc dữ liệu từ localStorage theo key "exam_draft_<examId>".
   * 2. Parse JSON thành đối tượng ExamDraft.
   * 3. Kiểm tra examId trong draft khớp với examId hiện tại (phòng trường hợp dữ liệu lỗi).
   * 4. Nếu hợp lệ, khôi phục answers và đánh dấu isRestoredFromDraft = true.
   */
  useEffect(() => {
    try {
      const savedDraftJson = localStorage.getItem(storageKey);

      if (savedDraftJson === null) {
        return;
      }

      const savedDraft: ExamDraft = JSON.parse(savedDraftJson);

      if (savedDraft.examId !== examId) {
        console.warn(
          `[DraftSave] Draft examId (${savedDraft.examId}) không khớp với examId hiện tại (${examId}). Bỏ qua.`
        );
        return;
      }

      if (savedDraft.answers && Object.keys(savedDraft.answers).length > 0) {
        setAnswers(savedDraft.answers);
        setIsRestoredFromDraft(true);
        console.info(
          `[DraftSave] Đã khôi phục ${Object.keys(savedDraft.answers).length} câu trả lời từ draft. ` +
          `Lưu lúc: ${savedDraft.savedAt}`
        );
      }
    } catch (parseError) {
      console.error('[DraftSave] Lỗi parse draft từ localStorage:', parseError);
      localStorage.removeItem(storageKey);
    }
  }, [examId, storageKey]);

  /**
   * Effect lưu draft: Chạy mỗi khi answers thay đổi.
   *
   * Mỗi khi sinh viên chọn hoặc thay đổi đáp án, toàn bộ trạng thái answers
   * được serialize thành JSON và lưu vào localStorage.
   *
   * Chỉ lưu khi có ít nhất 1 câu trả lời (tránh lưu draft rỗng).
   */
  useEffect(() => {
    if (Object.keys(answers).length === 0) {
      return;
    }

    try {
      const draft: ExamDraft = {
        examId: examId,
        answers: answers,
        savedAt: new Date().toISOString(),
      };

      localStorage.setItem(storageKey, JSON.stringify(draft));
    } catch (storageError) {
      console.error('[DraftSave] Lỗi lưu draft vào localStorage:', storageError);
    }
  }, [answers, examId, storageKey]);

  /**
   * Cập nhật đáp án cho một câu hỏi cụ thể.
   *
   * Sử dụng useCallback để tránh tạo hàm mới mỗi lần render,
   * giúp các component con sử dụng React.memo không bị re-render không cần thiết.
   *
   * @param questionId - ID câu hỏi cần cập nhật
   * @param selectedOption - Đáp án sinh viên chọn (A, B, C, hoặc D)
   */
  const updateAnswer = useCallback(
    (questionId: number, selectedOption: string): void => {
      setAnswers((previousAnswers) => ({
        ...previousAnswers,
        [questionId]: selectedOption,
      }));
    },
    []
  );

  /**
   * Xóa draft khỏi localStorage.
   *
   * Gọi hàm này sau khi sinh viên nộp bài thành công
   * để dọn dẹp dữ liệu không còn cần thiết.
   */
  const clearDraft = useCallback((): void => {
    localStorage.removeItem(storageKey);
    console.info(`[DraftSave] Đã xóa draft cho examId=${examId}`);
  }, [storageKey, examId]);

  return {
    answers,
    updateAnswer,
    clearDraft,
    isRestoredFromDraft,
  };
}
