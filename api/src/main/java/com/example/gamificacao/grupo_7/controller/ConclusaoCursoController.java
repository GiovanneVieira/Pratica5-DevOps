package com.example.gamificacao.grupo_7.controller;

import com.example.gamificacao.grupo_7.dto.matricula.ConcluirCursoRequestDTO;
import com.example.gamificacao.grupo_7.dto.matricula.ConcluirCursoResponseDTO;
import com.example.gamificacao.grupo_7.service.ConclusaoCursoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ConclusaoCursoController {

    private final ConclusaoCursoService conclusaoCursoService;

    @PatchMapping("/alunos/{alunoId}/matriculas/{matriculaId}/conclusao")
    public ResponseEntity<ConcluirCursoResponseDTO> patchConclusaoCurso(
            @PathVariable UUID alunoId,
            @PathVariable UUID matriculaId,
            @RequestBody
            @Valid
            ConcluirCursoRequestDTO requestDTO
    ){
        var resposta = this.conclusaoCursoService.concluir(alunoId, matriculaId, requestDTO);
        return ResponseEntity.status(HttpStatus.OK).body(resposta);
    }

}
