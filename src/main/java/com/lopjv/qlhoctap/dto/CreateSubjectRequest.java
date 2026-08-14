package com.lopjv.qlhoctap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSubjectRequest {

    @NotNull(message = "ID khóa học không được để trống")
    private Long courseId;

    @NotNull(message = "ID giảng viên không được để trống")
    private Long teacherId;

    @NotBlank(message = "Mã môn học không được để trống")
    @Size(max = 30, message = "Mã môn học tối đa 30 ký tự")
    private String code;

    @NotBlank(message = "Tên môn học không được để trống")
    @Size(max = 200, message = "Tên môn học tối đa 200 ký tự")
    private String title;

    private String description;

    @Builder.Default
    private Integer orderIndex = 1;
}
