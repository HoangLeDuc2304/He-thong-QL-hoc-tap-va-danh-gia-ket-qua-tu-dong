/**
 * Định nghĩa các kiểu dữ liệu TypeScript cho phòng thi.
 *
 * Các interface này được sử dụng chung giữa các hooks và components
 * để đảm bảo type-safety trong toàn bộ luồng làm bài thi.
 */

/**
 * Đại diện cho một câu trả lời của sinh viên.
 * Mỗi object chứa ID câu hỏi và đáp án sinh viên đã chọn.
 */
export interface StudentAnswer {
  questionId: number;
  selectedOption: string; // optionId, hoặc nhiều optionId cách nhau dấu phẩy cho câu hỏi nhiều đáp án
}

/**
 * Đại diện cho một câu hỏi trắc nghiệm trong đề thi.
 */
export interface ExamQuestion {
  id: number;
  content: string;
  optionA: string;
  optionB: string;
  optionC: string;
  optionD: string;
  questionOrder: number;
  options?: { id: number; content: string }[];
  questionType?: string; // SINGLE_CHOICE hoặc MULTIPLE_CHOICE
}

/**
 * Đại diện cho thông tin đề thi.
 */
export interface ExamInfo {
  examId: number;
  examTitle: string;
  durationMinutes: number;
  maxTabSwitches: number;
  questions: ExamQuestion[];
}

/**
 * DTO gửi lên server khi nộp bài thi.
 * Tương ứng với ExamSubmissionRequest ở backend.
 */
export interface ExamSubmissionRequest {
  examId: number;
  answers: StudentAnswer[];
  tabSwitchCount: number;
  isAutoSubmitted: boolean;
}

/**
 * DTO nhận từ server sau khi chấm điểm.
 * Tương ứng với ExamResultResponse ở backend.
 */
export interface ExamResultResponse {
  examResultId: number;
  examId: number;
  examTitle: string;
  score: number;
  totalCorrect: number;
  totalQuestions: number;
  tabSwitchCount: number;
  isAutoSubmitted: boolean;
  submittedAt: string;
  message: string;
}

/**
 * Trạng thái bài làm lưu trong localStorage (draft).
 * Dùng để khôi phục bài làm khi F5 hoặc tải lại trang.
 */
export interface ExamDraft {
  examId: number;
  answers: Record<number, string>; // Map<questionId, selectedOption>
  savedAt: string; // ISO timestamp
}
