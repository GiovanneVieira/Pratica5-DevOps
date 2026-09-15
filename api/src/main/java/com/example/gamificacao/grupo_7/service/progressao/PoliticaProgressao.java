package com.example.gamificacao.grupo_7.service.progressao;

import com.example.gamificacao.grupo_7.enums.CursoStatus;
import com.example.gamificacao.grupo_7.enums.Plano;

public interface PoliticaProgressao {

    /**
     * Avalia o resultado da conclusao de um curso conforme as regras de progressao
     * da planilha Template_ATDD_Gamificacao.xlsx (aba pb).
     *
     * @param statusAposConclusao   status do curso apos aplicar a nota (CONCLUIDO ou REPROVADO)
     * @param planoAluno            plano atual do aluno
     * @param cursosConcluidosAntes quantidade de cursos CONCLUIDOS do aluno antes desta conclusao
     */
    ResultadoProgressao avaliar(CursoStatus statusAposConclusao, Plano planoAluno, long cursosConcluidosAntes);

}
