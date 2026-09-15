package com.example.gamificacao.grupo_7.mapper;

import com.example.gamificacao.grupo_7.enums.VoucherStatus;
import com.example.gamificacao.grupo_7.model.Aluno;
import com.example.gamificacao.grupo_7.model.Voucher;
import com.example.gamificacao.grupo_7.model.validation_object.VoucherValue;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class VoucherMapper {

    public Voucher buildEntity(String name,
                               Double valor,
                               String description,
                               Aluno aluno
                              ){
        return Voucher.builder()
                .nome(name)
                .valor(new VoucherValue(valor))
                .descricao(description)
                .aluno(aluno)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .status(VoucherStatus.VALIDO)
                .build();
    }

}
