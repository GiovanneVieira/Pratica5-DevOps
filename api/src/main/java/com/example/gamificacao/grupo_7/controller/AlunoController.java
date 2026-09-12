package com.example.gamificacao.grupo_7.controller;

import com.example.gamificacao.grupo_7.model.Aluno;
import com.example.gamificacao.grupo_7.service.AlunoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/aluno")
public class AlunoController {

    private final AlunoService alunoService;

    @PostMapping
    public ResponseEntity<?> createAluno(@RequestBody Aluno aluno){
        Aluno alunoCriado = this.alunoService.createAluno(aluno);
        return ResponseEntity.ok(alunoCriado);
    }

}
