package com.example.gamificacao.grupo_7.mapper;

import com.example.gamificacao.grupo_7.model.Aluno;
import com.example.gamificacao.grupo_7.model.Curso;
import com.example.gamificacao.grupo_7.model.Voucher;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class VoucherMapper {

    public Voucher buildEntity(String name,
                               Double valor,
                               String description,
                               Aluno aluno,
                               Curso curso){
        return Voucher.builder()
                .id(UUID.randomUUID())
                .nome(name)
                .valor(valor)
                .descricao(description)
                .aluno(aluno)
                .curso(curso)
                .build();
    }

}
