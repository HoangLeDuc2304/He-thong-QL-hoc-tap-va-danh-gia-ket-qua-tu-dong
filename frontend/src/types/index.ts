/**
 * Barrel export cho tất cả TypeScript types/interfaces.
 *
 * Cho phép import gọn gàng:
 * import type { ExamInfo, StudentAnswer, ExamDraft } from '../types';
 */
export type {
  StudentAnswer,
  ExamQuestion,
  ExamInfo,
  ExamSubmissionRequest,
  ExamResultResponse,
  ExamDraft,
} from './exam';
