package com.antigravity.domain.admin.demo.controller;

import com.antigravity.domain.admin.demo.service.DemoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/demo")
@RequiredArgsConstructor
public class DemoController {

    private final DemoService demoService;

    @PostMapping("/setup")
    public ResponseEntity<String> setupDemo() {
        demoService.setupDemoData();
        return ResponseEntity.ok("데모 데이터 생성 완료");
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetDemo() {
        demoService.resetDemoData();
        return ResponseEntity.ok("데모 데이터 초기화 완료");
    }
}
