import { useState, useEffect, useRef, useCallback } from 'react';

/**
 * Giá trị trả về từ hook useCountdown.
 */
interface UseCountdownReturn {
  /** Số phút còn lại */
  minutes: number;
  /** Số giây còn lại (trong phút hiện tại, 0-59) */
  seconds: number;
  /** Tổng số giây còn lại */
  totalSecondsRemaining: number;
  /** Chuỗi hiển thị thời gian dạng "MM:SS" */
  formattedTime: string;
  /** Đã hết giờ hay chưa */
  isExpired: boolean;
}

/**
 * Custom Hook đếm ngược thời gian làm bài thi.
 *
 * Chức năng:
 * 1. Nhận vào số phút làm bài, tự động đếm ngược theo từng giây bằng setInterval.
 * 2. Trả về số phút, giây còn lại, chuỗi "MM:SS", và trạng thái hết giờ.
 * 3. Khi hết giờ (totalSecondsRemaining === 0), tự động gọi hàm onTimeUp (nộp bài).
 * 4. Tự động dọn dẹp interval khi component unmount để tránh memory leak.
 *
 * @param durationMinutes - Số phút làm bài (ví dụ: 60 phút)
 * @param onTimeUp - Hàm callback được gọi khi hết giờ (thường là hàm nộp bài tự động)
 * @returns Object chứa thông tin thời gian còn lại
 *
 * @example
 * ```tsx
 * const { formattedTime, isExpired } = useCountdown(60, handleAutoSubmit);
 *
 * return <div>Thời gian còn lại: {formattedTime}</div>;
 * ```
 */
export function useCountdown(
  durationMinutes: number,
  onTimeUp: () => void
): UseCountdownReturn {
  const [totalSecondsRemaining, setTotalSecondsRemaining] = useState<number>(
    durationMinutes * 60
  );

  /**
   * Dùng useRef để lưu trữ tham chiếu đến hàm onTimeUp mới nhất.
   *
   * Lý do: Nếu dùng trực tiếp onTimeUp trong useEffect, mỗi khi onTimeUp thay đổi
   * (do re-render), useEffect sẽ chạy lại và reset interval. Dùng ref đảm bảo
   * interval không bị reset nhưng vẫn gọi đúng phiên bản mới nhất của onTimeUp.
   */
  const onTimeUpRef = useRef<() => void>(onTimeUp);

  /**
   * Cập nhật ref mỗi khi onTimeUp thay đổi.
   * Điều này đảm bảo khi interval callback chạy, nó luôn gọi phiên bản mới nhất.
   */
  useEffect(() => {
    onTimeUpRef.current = onTimeUp;
  }, [onTimeUp]);

  /**
   * Flag đánh dấu đã gọi onTimeUp chưa, tránh gọi nhiều lần.
   */
  const hasCalledOnTimeUpRef = useRef<boolean>(false);

  /**
   * Effect chính: Thiết lập setInterval đếm ngược mỗi 1000ms (1 giây).
   *
   * Mỗi giây:
   * 1. Giảm totalSecondsRemaining đi 1.
   * 2. Khi về 0, gọi onTimeUp callback và dừng interval.
   *
   * Cleanup: clearInterval khi component unmount.
   */
  useEffect(() => {
    if (totalSecondsRemaining <= 0) {
      return;
    }

    const intervalId = setInterval(() => {
      setTotalSecondsRemaining((previousSeconds) => {
        const nextSeconds = previousSeconds - 1;

        if (nextSeconds <= 0 && !hasCalledOnTimeUpRef.current) {
          hasCalledOnTimeUpRef.current = true;
          /**
           * Dùng setTimeout(0) để đưa lời gọi onTimeUp ra khỏi chu kỳ render hiện tại.
           * Tránh lỗi "Cannot update a component while rendering a different component".
           */
          setTimeout(() => {
            onTimeUpRef.current();
          }, 0);
        }

        return Math.max(0, nextSeconds);
      });
    }, 1000);

    return () => {
      clearInterval(intervalId);
    };
  }, [totalSecondsRemaining <= 0]);

  /**
   * Tính số phút còn lại (phần nguyên).
   */
  const minutes = Math.floor(totalSecondsRemaining / 60);

  /**
   * Tính số giây còn lại (phần dư sau khi chia cho 60).
   */
  const seconds = totalSecondsRemaining % 60;

  /**
   * Định dạng thời gian thành chuỗi "MM:SS".
   * Ví dụ: 5 phút 3 giây → "05:03"
   */
  const formattedTime = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;

  /**
   * Trạng thái đã hết giờ.
   */
  const isExpired = totalSecondsRemaining <= 0;

  return {
    minutes,
    seconds,
    totalSecondsRemaining,
    formattedTime,
    isExpired,
  };
}
