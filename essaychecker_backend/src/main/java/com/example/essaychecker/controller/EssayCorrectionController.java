package com.example.essaychecker.controller;

import com.example.essaychecker.model.EssayRequest;
import com.example.essaychecker.service.DeepSeekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class EssayCorrectionController {

    private final DeepSeekService deepSeekService;

    @Autowired
    public EssayCorrectionController(DeepSeekService deepSeekService) {
        this.deepSeekService = deepSeekService;
    }

    @PostMapping("/correct_essay")
    public ResponseEntity<?> correctEssay(@RequestBody EssayRequest request) {
        if (request == null || request.getEssay() == null || request.getEssay().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\": \"Essay content is missing\"}");
        }

        try {
            String correctionResult = deepSeekService.getCorrection(request.getEssay());
            return ResponseEntity.ok(correctionResult);
        } catch (Exception e) {
            // 记录详细的异常信息以便调试
            System.err.println("Failed to get correction from AI service: " + e.getMessage());
            // 返回一个更详细的错误信息给客户端
            return ResponseEntity.status(500).body("{\"error\": \"Failed to get correction from AI service: "
                    + e.getMessage() + "\"}");
        }
    }
}
//455705967