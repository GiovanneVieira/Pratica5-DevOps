package com.example.gamificacao.grupo_7.model;

import java.util.List;

/**
 * Resultado da aplicacao de recompensas de progressao sobre um aluno:
 * as matriculas dos cursos liberados e, quando ha upgrade para Premium,
 * o voucher para projetos reais.
 */
public record Recompensa(List<Matricula> cursosLiberados, Voucher voucher) {

    public static Recompensa vazia() {
        return new Recompensa(List.of(), null);
    }

}
