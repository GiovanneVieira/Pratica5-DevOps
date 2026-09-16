package com.example.gamificacao.grupo_7.service.progressao;

public enum ResultadoProgressao {

    /** TDD3 / BDD3: nota <= 7.0 reprova o curso, sem liberar bonus e sem progresso no plano. */
    REPROVADO_SEM_PROGRESSO,

    /** TDD1 / BDD1: aluno basico com menos de 11 cursos concluidos e nota > 7.0 recebe 3 cursos bonus. */
    CURSO_BONUS_BASICO,

    /** TDD2 / BDD2: 12o curso concluido com nota > 7.0 por aluno basico: upgrade para Premium e recompensas. */
    UPGRADE_PREMIUM,

    /**
     * TDD5 / BDD2 recorrente: a cada 12 cursos concluidos com nota > 7.0 o
     * aluno Premium recebe novamente as recompensas do BDD2: 3 moedas e voucher
     * (sem novo upgrade de plano e sem cursos bonus).
     */
    RECOMPENSA_RECURRENTE,

    /** Aluno Premium fora dos multiplos de 12 (ou fora das regras da planilha): conclusao sem novas recompensas. */
    SEM_RECOMPENSA
}
