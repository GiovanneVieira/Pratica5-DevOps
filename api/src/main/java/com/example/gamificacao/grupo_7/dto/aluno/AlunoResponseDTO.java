package com.example.gamificacao.grupo_7.dto.aluno;

import com.example.gamificacao.grupo_7.enums.Plano;
import com.example.gamificacao.grupo_7.model.validation_object.RA;
import java.util.UUID;

public record AlunoResponseDTO (
        UUID id,
        String name,
        RA ra,
        Plano plano
){
}
