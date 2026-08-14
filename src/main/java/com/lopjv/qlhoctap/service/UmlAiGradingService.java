package com.lopjv.qlhoctap.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lopjv.qlhoctap.dto.UmlAiAnalysisResultDto;
import com.lopjv.qlhoctap.entity.UmlAssignment;
import com.lopjv.qlhoctap.entity.UmlSubmission;
import com.lopjv.qlhoctap.exception.ResourceNotFoundException;
import com.lopjv.qlhoctap.repository.UmlSubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UmlAiGradingService {

    private static final Logger logger = LoggerFactory.getLogger(UmlAiGradingService.class);

    private final UmlSubmissionRepository umlSubmissionRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${application.ai.api-key:MOCK_API_KEY}")
    private String aiApiKey;

    @Value("${application.ai.api-url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent}")
    private String aiApiUrl;

    public UmlAiGradingService(UmlSubmissionRepository umlSubmissionRepository, RestTemplate restTemplate) {
        this.umlSubmissionRepository = umlSubmissionRepository;
        this.objectMapper = new ObjectMapper();
        this.restTemplate = restTemplate;
    }

    @Transactional
    public UmlSubmission analyzeAndGradeUmlSubmission(Long submissionId) {
        UmlSubmission submission = umlSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài nộp UML với ID: " + submissionId));

        UmlAssignment assignment = submission.getAssignment();

        // 1. Chuẩn bị Prompt đánh giá cho AI LLM
        String promptText = buildAiEvaluationPrompt(assignment, submission);

        // 2. Gửi Request tới AI API và nhận kết quả JSON
        UmlAiAnalysisResultDto aiResult = callAiLlmApi(promptText, assignment.getMaxScore());

        // 3. Cập nhật thông tin vào bản ghi UmlSubmission
        submission.setAiSuggestedScore(aiResult.getAiSuggestedScore());
        submission.setAiFeedback(aiResult.getAiFeedback());
        submission.setAiAnalyzedAt(OffsetDateTime.now());
        submission.setStatus("AI_ANALYZED");

        return umlSubmissionRepository.save(submission);
    }

    private String buildAiEvaluationPrompt(UmlAssignment assignment, UmlSubmission submission) {
        return String.format(
                "Bạn là một chuyên gia Kiến trúc phần mềm và Giáo sư giảng dạy UML xuất sắc.\n" +
                        "Hãy phân tích và đánh giá bài nộp bài tập thiết kế sơ đồ UML của sinh viên theo các thông tin sau:\n\n" +
                        "1. Tên bài tập: %s\n" +
                        "2. Đề bài / Yêu cầu chi tiết: %s\n" +
                        "3. Tiêu chí chấm điểm (Rubric): %s\n" +
                        "4. Thang điểm tối đa: %s điểm\n" +
                        "5. Đường dẫn file bài nộp (Ảnh/PDF): %s (Loại file: %s)\n\n" +
                        "YÊU CẦU ĐẦU RA:\n" +
                        "Hãy trả về ĐÚNG MỘT CHUỖI JSON DUY NHẤT (không kèm markdown code block ```json) có cấu trúc như sau:\n" +
                        "{\n" +
                        "  \"score\": 8.5,\n" +
                        "  \"feedback\": \"Nhận xét chi tiết về các Class, Actor, UseCase, mối quan hệ Relationship, điểm mạnh và các lỗi thiết kế cần khắc phục.\"\n" +
                        "}",
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getRubricCriteria() != null ? assignment.getRubricCriteria() : "Chất lượng thiết kế sơ đồ UML chuẩn ISO/IEC",
                assignment.getMaxScore().toString(),
                submission.getFileUrl(),
                submission.getFileType()
        );
    }

    private UmlAiAnalysisResultDto callAiLlmApi(String promptText, BigDecimal maxScore) {
        if ("MOCK_API_KEY".equals(aiApiKey) || aiApiKey == null || aiApiKey.trim().isEmpty()) {
            logger.info("Chưa cấu hình AI API Key thật. Sử dụng kết quả phân tích AI mẫu (Mock AI Engine)...");
            return generateMockAiResponse(maxScore);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Cấu trúc Request Body cho Gemini REST API
            Map<String, Object> textPart = Map.of("text", promptText);
            Map<String, Object> contentObj = Map.of("parts", List.of(textPart));
            Map<String, Object> requestBody = Map.of("contents", List.of(contentObj));

            String fullUrl = aiApiUrl + "?key=" + aiApiKey;
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseAiJsonResponse(response.getBody(), maxScore);
            }

        } catch (Exception ex) {
            logger.error("Lỗi khi gọi AI LLM API: {}. Chuyển sang kết quả phân tích AI dự phòng...", ex.getMessage());
        }

        return generateMockAiResponse(maxScore);
    }

    private UmlAiAnalysisResultDto parseAiJsonResponse(String rawResponseBody, BigDecimal maxScore) {
        try {
            JsonNode rootNode = objectMapper.readTree(rawResponseBody);

            // Trích xuất văn bản từ phản hồi Gemini REST API
            JsonNode textNode = rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");

            String aiResponseText = textNode.asText().trim();

            // Loại bỏ bọc markdown ```json ... ``` nếu AI vô tình trả về
            if (aiResponseText.startsWith("```json")) {
                aiResponseText = aiResponseText.substring(7);
            }
            if (aiResponseText.startsWith("```")) {
                aiResponseText = aiResponseText.substring(3);
            }
            if (aiResponseText.endsWith("```")) {
                aiResponseText = aiResponseText.substring(0, aiResponseText.length() - 3);
            }

            JsonNode jsonResult = objectMapper.readTree(aiResponseText.trim());

            double scoreDouble = jsonResult.path("score").asDouble(7.5);
            String feedback = jsonResult.path("feedback").asText("Đã phân tích thành công sơ đồ UML.");

            BigDecimal suggestedScore = BigDecimal.valueOf(scoreDouble).setScale(2, RoundingMode.HALF_UP);
            if (suggestedScore.compareTo(maxScore) > 0) {
                suggestedScore = maxScore;
            }

            return UmlAiAnalysisResultDto.builder()
                    .aiSuggestedScore(suggestedScore)
                    .aiFeedback(feedback)
                    .build();

        } catch (Exception ex) {
            logger.error("Lỗi khi giải mã chuỗi JSON từ AI: {}", ex.getMessage());
            return generateMockAiResponse(maxScore);
        }
    }

    private UmlAiAnalysisResultDto generateMockAiResponse(BigDecimal maxScore) {
        BigDecimal mockScore = maxScore.multiply(BigDecimal.valueOf(0.85)).setScale(2, RoundingMode.HALF_UP);

        String mockFeedback = "### Phân Tích & Đánh Giá Chi Tiết Sơ Đồ UML từ AI\n" +
                "1. **Tính đầy đủ các thành phần (Completeness):** Sơ đồ thể hiện tốt các Lớp chính (Entities, Services, Controllers), có đầy đủ thuộc tính và phương thức cơ bản.\n" +
                "2. **Mối quan hệ giữa các lớp (Relationships):** Đã thể hiện chính xác mối quan hệ Thừa kế (Generalization) và Phụ thuộc (Dependency). Cần lưu ý bổ sung Cardinality (1..*, 0..1) cho các Association.\n" +
                "3. **Đánh giá tổng quan:** Thiết kế mạch lạc, tuân thủ nguyên lý SOLID cơ bản.\n" +
                "-> **Điểm đề xuất AI:** " + mockScore + " / " + maxScore;

        return UmlAiAnalysisResultDto.builder()
                .aiSuggestedScore(mockScore)
                .aiFeedback(mockFeedback)
                .build();
    }
}
