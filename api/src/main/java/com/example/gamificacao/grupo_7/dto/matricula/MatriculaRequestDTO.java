package com.example.gamificacao.grupo_7.dto.matricula;

import jakarta.validation.constraints.NotBlank;

public record MatriculaRequestDTO(
        @NotBlank(message = "O nome do curso não pode ser nulo ou vazio")
        String nomeCurso
) {
}
