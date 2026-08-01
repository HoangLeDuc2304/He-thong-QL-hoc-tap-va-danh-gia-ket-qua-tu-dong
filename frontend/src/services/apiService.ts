import type { ExamSubmissionRequest, ExamResultResponse, ExamInfo } from '../types/exam';

/**
 * Base URL cho API backend.
 * Trong development, Spring Boot chạy ở port 8080.
 * Trong production, thay bằng URL thực tế của server.
 */
const API_BASE_URL = 'http://localhost:8080/api';

/**
 * Lấy JWT token từ localStorage.
 * Token được lưu sau khi đăng nhập thành công.
 *
 * @returns chuỗi JWT token hoặc null nếu chưa đăng nhập
 */
function getAuthToken(): string | null {
  return localStorage.getItem('accessToken');
}

/**
 * Tạo header Authorization chứa Bearer token cho mỗi request.
 *
 * @returns object Headers với Content-Type và Authorization
 * @throws Error nếu chưa đăng nhập (không có token)
 */
function createAuthHeaders(): HeadersInit {
  const token = getAuthToken();
  if (!token) {
    throw new Error('Chưa đăng nhập. Vui lòng đăng nhập trước khi thực hiện thao tác.');
  }

  return {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`,
  };
}

/**
 * Service gọi các API backend liên quan đến phòng thi.
 *
 * Tất cả các hàm đều:
 * - Tự động gắn JWT token vào header Authorization.
 * - Parse response JSON và xử lý lỗi HTTP.
 * - Trả về kiểu dữ liệu TypeScript tương ứng.
 */
export const examApiService = {

  /**
   * Lấy thông tin đề thi và danh sách câu hỏi cho sinh viên.
   *
   * @param examId - ID đề thi
   * @returns Promise chứa thông tin đề thi (ExamInfo)
   * @throws Error nếu request thất bại
   */
  async getExamForStudent(examId: number): Promise<ExamInfo> {
    const response = await fetch(`${API_BASE_URL}/student/exams/${examId}`, {
      method: 'GET',
      headers: createAuthHeaders(),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => null);
      throw new Error(
        errorData?.message || `Lỗi lấy thông tin đề thi. Status: ${response.status}`
      );
    }

    return response.json();
  },

  /**
   * Nộp bài thi và nhận kết quả chấm điểm.
   *
   * Gửi danh sách câu trả lời kèm dữ liệu anti-cheat (tabSwitchCount, isAutoSubmitted)
   * lên server. Server sẽ chấm điểm và trả về kết quả.
   *
   * @param submission - DTO chứa examId, answers, tabSwitchCount, isAutoSubmitted
   * @returns Promise chứa kết quả thi (ExamResultResponse)
   * @throws Error nếu request thất bại (quá hạn, đã nộp trước đó, v.v.)
   */
  async submitExam(submission: ExamSubmissionRequest): Promise<ExamResultResponse> {
    const response = await fetch(`${API_BASE_URL}/student/exams/submit`, {
      method: 'POST',
      headers: createAuthHeaders(),
      body: JSON.stringify(submission),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => null);
      throw new Error(
        errorData?.message || `Lỗi nộp bài thi. Status: ${response.status}`
      );
    }

    return response.json();
  },
};

/**
 * Service gọi các API xác thực (đăng nhập, đăng ký).
 */
export const authApiService = {

  /**
   * Đăng nhập và lưu JWT token vào localStorage.
   *
   * @param email - Địa chỉ email
   * @param password - Mật khẩu
   * @returns Promise chứa thông tin người dùng và token
   * @throws Error nếu sai email/mật khẩu
   */
  async login(email: string, password: string): Promise<{
    accessToken: string;
    userId: number;
    fullName: string;
    email: string;
    role: string;
  }> {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => null);
      throw new Error(
        errorData?.message || 'Sai email hoặc mật khẩu. Vui lòng thử lại.'
      );
    }

    const data = await response.json();

    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('userRole', data.role);
    localStorage.setItem('userId', String(data.userId));
    localStorage.setItem('fullName', data.fullName);

    return data;
  },

  /**
   * Đăng xuất: Xóa token và thông tin người dùng khỏi localStorage.
   */
  logout(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('userRole');
    localStorage.removeItem('userId');
    localStorage.removeItem('fullName');
  },

  /**
   * Kiểm tra người dùng đã đăng nhập hay chưa.
   *
   * @returns true nếu có accessToken trong localStorage
   */
  isAuthenticated(): boolean {
    return localStorage.getItem('accessToken') !== null;
  },
};
