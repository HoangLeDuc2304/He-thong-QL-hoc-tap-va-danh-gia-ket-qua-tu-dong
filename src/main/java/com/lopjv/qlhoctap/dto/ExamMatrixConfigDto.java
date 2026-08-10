package com.lopjv.qlhoctap.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO chứa ma trận cấu hình trộn đề tự động từ Ngân hàng câu hỏi.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamMatrixConfigDto {

    @NotNull(message = "ID đề thi không được để trống")
    private Long examId;

    @NotNull(message = "ID môn học không được để trống")
    private Long subjectId;

    private String chapterTopic;

    @Min(value = 0, message = "Số lượng câu hỏi DỄ không được nhỏ hơn 0")
    @Builder.Default
    private Integer easyCount = 0;

    @Min(value = 0, message = "Số lượng câu hỏi TRUNG BÌNH không được nhỏ hơn 0")
    @Builder.Default
    private Integer mediumCount = 0;

    @Min(value = 0, message = "Số lượng câu hỏi KHÓ không được nhỏ hơn 0")
    @Builder.Default
    private Integer hardCount = 0;
}
