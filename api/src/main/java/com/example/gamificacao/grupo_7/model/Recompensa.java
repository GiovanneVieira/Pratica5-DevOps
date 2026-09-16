package com.example.gamificacao.grupo_7.model;

import java.util.List;

/**
 * Resultado da aplicacao de recompensas de progressao sobre um aluno:
 * as matriculas dos cursos liberados, as moedas concedidas na operacao e,
 * quando ha (upgrade ou recorrencia), o voucher para projetos reais.
 */
public record Recompensa(List<Matricula> cursosLiberados, Voucher voucher, int moedasConcedidas) {

    public static Recompensa vazia() {
        return new Recompensa(List.of(), null, 0);
    }

}
