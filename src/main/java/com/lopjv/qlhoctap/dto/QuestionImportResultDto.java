package com.lopjv.qlhoctap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO chứa kết quả và báo lỗi chi tiết khi import Ngân hàng câu hỏi từ file Excel.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionImportResultDto {

    private int totalProcessed;
    private int totalSuccess;
    private int totalFailed;
    private List<String> errors;
}
