package com.example.gamificacao.grupo_7.dto.matricula;

import com.example.gamificacao.grupo_7.enums.CursoStatus;
import com.example.gamificacao.grupo_7.enums.Plano;

import java.util.List;
import java.util.UUID;

public record ConcluirCursoResponseDTO(
        UUID matriculaId,
        String nomeCurso,
        CursoStatus status,
        Plano plano,
        Integer moedas,
        List<MatriculaResponseDTO> cursosLiberados,
        VoucherResponseDTO voucher
) {
}
