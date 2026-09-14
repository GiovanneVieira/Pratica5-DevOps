package com.example.gamificacao.grupo_7.model;

import com.example.gamificacao.grupo_7.enums.VoucherStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Voucher {

    private UUID id;

    private String nome;

    private Double valor;

    private String descricao;

    private Aluno aluno;

    private VoucherStatus status;

    private LocalDateTime expiresAt;

}
