package com.lopjv.qlhoctap.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionImportResult {

    private int totalRows;
    private int successCount;
    private int errorCount;

    @Builder.Default
    private List<String> errorDetails = new ArrayList<>();

    public void addError(int rowNumber, String errorMessage) {
        this.errorDetails.add("Dòng " + rowNumber + ": " + errorMessage);
        this.errorCount++;
    }
}
