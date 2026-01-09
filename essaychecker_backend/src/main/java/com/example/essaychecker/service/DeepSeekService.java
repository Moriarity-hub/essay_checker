package com.example.essaychecker.service;

import com.example.essaychecker.mapper.EssayMapper;
import com.example.essaychecker.pojo.Essay;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

@Service
public class DeepSeekService {

    @Autowired
    private EssayMapper essayMapper;

    @Value("${deepseek.api.key}")
    private String deepseekApiKey;

    @Value("${deepseek.api.url}")
    private String deepseekApiUrl;

    private final RestTemplate restTemplate;

    public DeepSeekService() {
        this.restTemplate = new RestTemplate();
    }

    public String getCorrection(String essayContent) throws Exception {
        //将文章插入到数据库
        Essay essay = new Essay();
        essay.setContent(essayContent);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(deepseekApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "deepseek-coder");
        requestBody.put("messages", Collections.singletonList(
                Map.of(
                        "role", "user",
                        "content", "你是一位专业的中文作文批改专家。" +
                                "你的任务是分析用户提供的作文，并从语法、语句通顺度、逻辑性和写作技巧四个方面给出详细的批改建议和评分。" +
                                "请以结构化的 JSON 格式返回结果，不要包含任何额外的文字说明。" +
                                "JSON 格式必须包含以下字段：'grammar_errors' (语法错误列表), 'fluency_suggestions' (语句通顺建议列表), " +
                                "'logic_evaluation' (逻辑评估), 'general_suggestions' (综合提升建议列表)。"
                                + "\n\n请批改以下作文：\n\n" + essayContent
                )
        ));
        requestBody.put("temperature", 0.5);
        requestBody.put("max_tokens", 1024);
        requestBody.put("response_format", Map.of("type", "json_object"));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(deepseekApiUrl, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // DeepSeek API 返回一个选择数组。我们需要获取第一个。
                Object choices = response.getBody().get("choices");
                if (choices instanceof java.util.List && !((java.util.List) choices).isEmpty()) {
                    // 从列表中的第一个选择中提取内容对象
                    Map<String, Object> firstChoice = (Map<String, Object>) ((java.util.List) choices).get(0);
                    Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                    String content = (String) message.get("content");
                    essay.setContentResult(content);
                    essayMapper.insertEssay(essay);
                    // 我们需要直接返回 JSON 字符串，前端需要解析
                    return content;
                } else {
                    throw new RuntimeException("DeepSeek API response does not contain valid choices array.");
                }
            } else {
                // 如果 API 返回错误状态码，抛出更具体的异常
                throw new RuntimeException("DeepSeek API returned an error: " + response.getStatusCode() + ", Body: " + new ObjectMapper().writeValueAsString(response.getBody()));
            }
        } catch (Exception e) {
            // 重新抛出一个详细的异常，以便控制器捕获
            throw new Exception("Error during API call to DeepSeek: " + e.getMessage(), e);
        }
    }
}
