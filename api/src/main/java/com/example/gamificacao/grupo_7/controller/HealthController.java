package com.example.gamificacao.grupo_7.controller;

import com.example.gamificacao.grupo_7.model.Aluno;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
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
