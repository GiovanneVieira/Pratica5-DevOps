package com.example.gamificacao.grupo_7.mapper;

import com.example.gamificacao.grupo_7.dto.aluno.AlunoRequestDTO;
import com.example.gamificacao.grupo_7.dto.aluno.AlunoResponseDTO;
import com.example.gamificacao.grupo_7.enums.Plano;
import com.example.gamificacao.grupo_7.model.Aluno;
import com.example.gamificacao.grupo_7.model.validation_object.RA;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class AlunoMapper {

    private final PasswordEncoder passwordEncoder;

    /**
     * A senha nunca e persistida em texto puro: e hasheada com BCrypt aqui,
     * na montagem da entidade (o login compara com passwordEncoder.matches).
     * O RA tambem e gerado aqui pelo construtor padrao de RA (ano + sorteio).
     */
    public Aluno toEntity(AlunoRequestDTO requestDTO){
        return Aluno.builder()
                .plano(Plano.BASICO)
                .name(requestDTO.name())
                .moedas(0)
                .email(requestDTO.email())
                .password(this.passwordEncoder.encode(requestDTO.password()))
                .ra(new RA())
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
                aluno.getName(),
                aluno.getRa() != null ? aluno.getRa().getRa() : null,
                aluno.getPlano(),
                aluno.getMoedas(),
                aluno.getCursosConcluidos()
        );
    }



}
