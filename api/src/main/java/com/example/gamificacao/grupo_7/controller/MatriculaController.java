package com.example.gamificacao.grupo_7.controller;

import com.example.gamificacao.grupo_7.dto.matricula.MatriculaRequestDTO;
import com.example.gamificacao.grupo_7.dto.matricula.MatriculaResponseDTO;
import com.example.gamificacao.grupo_7.service.MatriculaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MatriculaController {

    private final MatriculaService matriculaService;

    @PostMapping("/alunos/{alunoId}/matriculas")
    public ResponseEntity<MatriculaResponseDTO> postMatricula(
            @PathVariable UUID alunoId,
            @RequestBody
            @Valid
            MatriculaRequestDTO requestDTO
    ){
        var matricula = this.matriculaService.matricularAluno(alunoId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(matricula);
    }

    @GetMapping("/alunos/{alunoId}/matriculas")
    public ResponseEntity<List<MatriculaResponseDTO>> getMatriculas(@PathVariable UUID alunoId){
        var matriculas = this.matriculaService.listarMatriculas(alunoId);
        return ResponseEntity.status(HttpStatus.OK).body(matriculas);
    }

}
