package com.lopjv.qlhoctap.exception;

/**
 * Ngoại lệ ném ra khi số lượng câu hỏi trong Ngân hàng không đủ theo ma trận cấu hình đề thi.
 */
public class InsufficientQuestionsException extends RuntimeException {

    public InsufficientQuestionsException(String message) {
        super(message);
    }

    public InsufficientQuestionsException(String message, Throwable cause) {
        super(message, cause);
    }
}
