package com.example.gamificacao.grupo_7.dto.aluno;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlunoRequestDTO(
        @NotBlank(message = "O nome não pode ser nulo ou vazio")
        String name,
        @NotBlank(message = "O email não pode ser nulo ou vazio")
        @Email(message = "O email deve ser válido")
        String email,
        @NotBlank(message = "A senha nao pode ser nula ou vazia")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        String password
) {
}
