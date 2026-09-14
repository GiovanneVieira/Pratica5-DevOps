package com.example.gamificacao.grupo_7.model;

import com.example.gamificacao.grupo_7.enums.VoucherStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Voucher {

    @NotNull(message = "O id do voucher não pode ser nulo")
    private UUID id;

    @NotBlank(message = "O nome do voucher não pode ser nulo ou vazio")
    private String nome;

    @NotNull(message = "O valor do voucher não pode ser nulo")
    @Positive(message = "O valor do voucher deve ser positivo")
    private Double valor;

    private String descricao;

    @NotNull(message = "O aluno do voucher não pode ser nulo")
    private Aluno aluno;

    @NotNull(message = "O status do voucher não pode ser nulo")
    private VoucherStatus status;

    @NotNull(message = "A data de expiração do voucher não pode ser nula")
    private LocalDateTime expiresAt;
}
