package com.example.gamificacao.grupo_7.dto.matricula;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record ConcluirCursoRequestDTO(
        @NotNull(message = "A nota final não pode ser nula")
        @DecimalMin(value = "0.0", message = "A nota final deve ser no mínimo 0.0")
        @DecimalMax(value = "10.0", message = "A nota final deve ser no máximo 10.0")
        Double notaFinal
) {
}
