package com.example.gamificacao.grupo_7.dto.matricula;

import com.example.gamificacao.grupo_7.dto.voucher.VoucherResponseDTO;
import com.example.gamificacao.grupo_7.enums.CursoStatus;
import com.example.gamificacao.grupo_7.enums.Plano;

import java.util.List;
import java.util.UUID;

/**
 * Contrato de resposta da conclusao de curso. Alem do saldo (moedas), expoe
 * o que foi concedido NESTA operacao: upgradePremium distingue o upgrade do
 * 12o curso (BDD2) da recompensa recorrente a cada 12 concluidos (TDD5), e
 * moedasRecebidas e a quantidade creditada agora (nao o saldo).
 */
public record ConcluirCursoResponseDTO(
        UUID matriculaId,
        String nomeCurso,
        CursoStatus status,
        Plano plano,
        Integer moedas,
        List<MatriculaResponseDTO> cursosLiberados,
        VoucherResponseDTO voucher,
        boolean upgradePremium,
        Integer moedasRecebidas
) {
}
