package com.example.gamificacao.grupo_7.dto;

import com.example.gamificacao.grupo_7.model.Curso;
import com.example.gamificacao.grupo_7.model.Voucher;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record RecompensasPremiumDTO(
        @NotNull(message = "O voucher não pode ser nulo")
        Voucher voucher,
        @NotNull(message = "A lista de cursos não pode ser nula")
        List<Curso> cursos,
        @NotNull(message = "A quantidade de moedas não pode ser nula")
        @Positive(message = "A quantidade de moedas deve ser positiva")
        Integer moedas
) {
}
