package com.example.gamificacao.grupo_7.dto.aluno;

import com.example.gamificacao.grupo_7.enums.Plano;

import java.util.UUID;

/**
 * Contrato de resposta da API: o RA e achatado para String (em vez do
 * embeddable aninhado {"ra": {"ra": "..."}}) - DTOs nao devem vazar
 * detalhes de persistencia como o objeto @Embeddable. Moedas faz parte do
 * contrato para o dashboard exibir o saldo real do aluno; cursosConcluidos
 * expoe o progresso oficial persistido (TDD4), que nao retrocede quando o
 * aluno apaga cursos do historico.
 */
public record AlunoResponseDTO (
        UUID id,
        String name,
        String ra,
        Plano plano,
        Integer moedas,
        Integer cursosConcluidos
){
}
