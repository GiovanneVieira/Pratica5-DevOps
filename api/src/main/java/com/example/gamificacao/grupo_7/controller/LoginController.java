package com.example.gamificacao.grupo_7.controller;

import com.example.gamificacao.grupo_7.dto.aluno.AlunoResponseDTO;
import com.example.gamificacao.grupo_7.dto.login.LoginRequestDTO;
import com.example.gamificacao.grupo_7.service.AlunoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Login simples: recebe email e senha, compara a senha do input com o hash
 * BCrypt armazenado (passwordEncoder.matches) e devolve os dados do aluno
 * quando as credenciais sao validas (401 caso contrario).
 */
@RestController
@RequiredArgsConstructor
public class LoginController {

    private final AlunoService alunoService;

    @PostMapping("/login")
    public ResponseEntity<AlunoResponseDTO> postLogin(
            @RequestBody
            @Valid
            LoginRequestDTO requestDTO
    ){
        var aluno = this.alunoService.login(requestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(aluno);
    }

}
