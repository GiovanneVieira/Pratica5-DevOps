package com.example.gamificacao.grupo_7.mapper;

import com.example.gamificacao.grupo_7.dto.aluno.AlunoRequestDTO;
import com.example.gamificacao.grupo_7.dto.aluno.AlunoResponseDTO;
import com.example.gamificacao.grupo_7.enums.Plano;
import com.example.gamificacao.grupo_7.model.Aluno;
import com.example.gamificacao.grupo_7.model.Curso;
import com.example.gamificacao.grupo_7.model.Voucher;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.UUID;

@Component
public class AlunoMapper {

    /*public Aluno buildEntity(String name){
        return Aluno.builder()
                .id(UUID.randomUUID())
                .plano(Plano.BASICO)
                .name(name)
                .moedas(0)
                .build();
    }*/

    public Aluno toEntity(AlunoRequestDTO requestDTO){
        return Aluno.builder()
                .plano(Plano.BASICO)
                .name(requestDTO.name())
                .moedas(0)
                .email(requestDTO.email())
                .password(requestDTO.password())
                .build();
    }

    public AlunoRequestDTO toRequestDTO(Aluno aluno){
        return new AlunoRequestDTO(
                aluno.getName(),
                aluno.getEmail(),
                aluno.getPassword()
        );
    }

    public AlunoResponseDTO toResponseDTO(Aluno aluno){
        return new AlunoResponseDTO(
                aluno.getId(),
                aluno.getEmail(),
                aluno.getRa(),
                aluno.getPlano()
        );
    }



}
