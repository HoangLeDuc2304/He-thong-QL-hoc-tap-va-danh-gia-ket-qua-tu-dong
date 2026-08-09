package com.lopjv.qlhoctap.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller API dành cho Giảng viên.
 */
@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of("message", "Teacher API is ready"));
    }
}
