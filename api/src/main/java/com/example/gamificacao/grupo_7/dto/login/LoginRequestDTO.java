package com.example.gamificacao.grupo_7.dto.login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank(message = "O email não pode ser nulo ou vazio")
        @Email(message = "O email deve ser válido")
        String email,
        @NotBlank(message = "A senha não pode ser nula ou vazia")
        String password
) {
}
