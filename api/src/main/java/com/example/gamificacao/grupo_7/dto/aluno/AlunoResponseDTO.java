package com.example.gamificacao.grupo_7.dto.aluno;

import com.example.gamificacao.grupo_7.enums.Plano;

import java.util.UUID;

/**
 * Contrato de resposta da API: o RA e achatado para String (em vez do
 * embeddable aninhado {"ra": {"ra": "..."}}) - DTOs nao devem vazar
 * detalhes de persistencia como o objeto @Embeddable.
 */
public record AlunoResponseDTO (
        UUID id,
        String name,
        String ra,
        Plano plano
){
}
