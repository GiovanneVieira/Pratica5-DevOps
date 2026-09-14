package com.example.gamificacao.grupo_7.mapper;

import com.example.gamificacao.grupo_7.enums.Plano;
import com.example.gamificacao.grupo_7.model.Aluno;
import com.example.gamificacao.grupo_7.model.Curso;
import com.example.gamificacao.grupo_7.model.Voucher;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.UUID;

@Component
public class AlunoMapper {

    public Aluno buildEntity(String name){
        return Aluno.builder()
                .id(UUID.randomUUID())
                .plano(Plano.BASICO)
                .name(name)
                .moedas(0)
                .vouchers(new ArrayList<Voucher>())
                .cursos(new ArrayList<Curso>())
                .build();
    }


}
