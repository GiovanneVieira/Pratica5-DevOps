package com.example.gamificacao.grupo_7.controller;

import com.example.gamificacao.grupo_7.dto.aluno.AlunoRequestDTO;
import com.example.gamificacao.grupo_7.dto.aluno.AlunoResponseDTO;
import com.example.gamificacao.grupo_7.model.Aluno;
import com.example.gamificacao.grupo_7.model.validation_object.RA;
import com.example.gamificacao.grupo_7.service.AlunoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/alunos")
@RequiredArgsConstructor
public class AlunoController {

    private final AlunoService alunoService;

    @PostMapping
    public ResponseEntity<AlunoResponseDTO> postAluno(
                                       @RequestBody
                                       @Valid
                                       AlunoRequestDTO requestDTO
    ){
        var aluno = this.alunoService.criaAluno(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(aluno);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlunoResponseDTO> getAlunoById(@PathVariable UUID id){
        var aluno = this.alunoService.getAlunoById(id);
        return ResponseEntity.status(200).body(aluno);
    }

    @GetMapping("/{ra}")
    public ResponseEntity<AlunoResponseDTO> getAlunoByRa(@PathVariable RA ra){
        var aluno = this.alunoService.getAlunoByRA(ra);
        return ResponseEntity.status(200).body(aluno);
    }

}
