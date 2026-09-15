package com.example.gamificacao.grupo_7.dto.matricula;

import com.example.gamificacao.grupo_7.enums.VoucherStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record VoucherResponseDTO(
        UUID id,
        String nome,
        Double valorEmReais,
        String descricao,
        VoucherStatus status,
        LocalDateTime expiresAt
) {
}
