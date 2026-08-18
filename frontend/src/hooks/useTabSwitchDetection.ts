import { useState, useEffect, useRef, useCallback } from 'react';

/**
 * Giá trị trả về từ hook useTabSwitchDetection.
 */
interface UseTabSwitchDetectionReturn {
  /** Số lần sinh viên đã rời khỏi tab (chuyển tab hoặc minimize) */
  tabSwitchCount: number;
  /** Số lần chuyển tab tối đa được phép */
  maxTabSwitches: number;
  /** Đã vượt quá giới hạn chuyển tab hay chưa */
  isLimitExceeded: boolean;
}

/**
 * Custom Hook phát hiện sinh viên chuyển tab (rời khỏi trang thi).
 *
 * Chức năng:
 * 1. Sử dụng sự kiện "visibilitychange" của trình duyệt để phát hiện khi sinh viên
 *    rời khỏi tab (chuyển sang tab/app khác hoặc minimize trình duyệt).
 * 2. Mỗi lần rời tab, tăng bộ đếm tabSwitchCount lên 1.
 * 3. Khi tabSwitchCount vượt quá maxTabSwitches, tự động gọi hàm onLimitExceeded
 *    (thường là hàm nộp bài + thông báo vi phạm).
 * 4. Tự động dọn dẹp event listener khi component unmount.
 *
 * Cách hoạt động của visibilitychange:
 * - document.visibilityState === "hidden": Người dùng KHÔNG nhìn thấy trang
 *   (chuyển tab, minimize, lock screen).
 * - document.visibilityState === "visible": Người dùng quay lại trang.
 *
 * @param maxTabSwitches - Số lần chuyển tab tối đa cho phép (ví dụ: 3)
 * @param onLimitExceeded - Hàm callback khi vượt quá giới hạn (nộp bài tự động)
 * @returns Object chứa thông tin chuyển tab
 *
 * @example
 * ```tsx
 * const { tabSwitchCount, isLimitExceeded } = useTabSwitchDetection(3, handleAutoSubmit);
 *
 * return (
 *   <div>
 *     Số lần rời tab: {tabSwitchCount}/3
 *     {isLimitExceeded && <span>⚠️ Đã vi phạm!</span>}
 *   </div>
 * );
 * ```
 */
export function useTabSwitchDetection(
  maxTabSwitches: number,
  onLimitExceeded: () => void
): UseTabSwitchDetectionReturn {
  const [tabSwitchCount, setTabSwitchCount] = useState<number>(0);

  /**
   * Dùng useRef để lưu tham chiếu mới nhất đến hàm onLimitExceeded.
   * Tương tự pattern trong useCountdown: tránh reset event listener
   * khi callback thay đổi do re-render.
   */
  const onLimitExceededRef = useRef<() => void>(onLimitExceeded);

  useEffect(() => {
    onLimitExceededRef.current = onLimitExceeded;
  }, [onLimitExceeded]);

  /**
   * Flag đánh dấu đã gọi onLimitExceeded chưa, tránh gọi nhiều lần.
   */
  const hasCalledLimitExceededRef = useRef<boolean>(false);

  /**
   * Ref để theo dõi giá trị tabSwitchCount mới nhất bên trong event listener.
   * Event listener được tạo 1 lần (qua useEffect), nên không thể đọc state
   * mới nhất trực tiếp. Dùng ref để giải quyết vấn đề closure stale.
   */
  const tabSwitchCountRef = useRef<number>(0);

  /**
   * Effect chính: Đăng ký event listener "visibilitychange" trên document.
   *
   * Luồng xử lý khi sự kiện xảy ra:
   * 1. Kiểm tra document.visibilityState === "hidden" (người dùng rời tab).
   * 2. Tăng tabSwitchCount lên 1.
   * 3. Nếu vượt quá maxTabSwitches, gọi onLimitExceeded.
   */
  useEffect(() => {
    const handleVisibilityChange = (): void => {
      if (document.visibilityState === 'hidden') {
        tabSwitchCountRef.current += 1;
        const newCount = tabSwitchCountRef.current;

        setTabSwitchCount(newCount);

        console.warn(
          `[Anti-Cheat] Phát hiện chuyển tab! Lần thứ ${newCount}/${maxTabSwitches}`
        );

        if (newCount >= maxTabSwitches && !hasCalledLimitExceededRef.current) {
          hasCalledLimitExceededRef.current = true;
          console.error(
            `[Anti-Cheat] Đã vượt quá giới hạn chuyển tab (${maxTabSwitches} lần). Tự động nộp bài!`
          );

          /**
           * Dùng setTimeout(0) để đảm bảo state đã cập nhật trước khi gọi callback.
           */
          setTimeout(() => {
            onLimitExceededRef.current();
          }, 0);
        }
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);

    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, [maxTabSwitches]);

  /**
   * Trạng thái đã vượt quá giới hạn chuyển tab.
   */
  const isLimitExceeded = tabSwitchCount >= maxTabSwitches;

  return {
    tabSwitchCount,
    maxTabSwitches,
    isLimitExceeded,
  };
}
