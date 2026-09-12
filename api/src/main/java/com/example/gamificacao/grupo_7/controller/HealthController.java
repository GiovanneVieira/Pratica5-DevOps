package com.example.gamificacao.grupo_7.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<?> healthCheck(){

        Map<String, String> response = Map.of("message", "OK");
        return ResponseEntity.ok(response);
    }

}
