package com.example.gamificacao.grupo_7.dto.matricula;

import com.example.gamificacao.grupo_7.enums.CursoStatus;

import java.util.UUID;

public record MatriculaResponseDTO(
        UUID id,
        String nomeCurso,
        CursoStatus status,
        Double notaFinal
) {
}
