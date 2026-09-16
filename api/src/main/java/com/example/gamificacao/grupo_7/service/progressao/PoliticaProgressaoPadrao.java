package com.example.gamificacao.grupo_7.service.progressao;

import com.example.gamificacao.grupo_7.enums.CursoStatus;
import com.example.gamificacao.grupo_7.enums.CursosParametros;
import com.example.gamificacao.grupo_7.enums.Plano;
import org.springframework.stereotype.Component;

@Component
public class PoliticaProgressaoPadrao implements PoliticaProgressao {

    /**
     * Decisao central dos tres ciclos ATDD (TDD1, TDD2 e TDD3), derivada dos
     * cenarios Given/When/Then da planilha Template_ATDD_Gamificacao.xlsx (aba pb).
     * Cada bloco abaixo corresponde a um cenario de aceitacao de um ciclo RED-GREEN-BLUE.
     */
    @Override
    public ResultadoProgressao avaliar(CursoStatus statusAposConclusao, Plano planoAluno, long cursosConcluidosAntes) {
        // TDD3 - GREEN: nota <= 7.0 reprova o curso: nada e liberado e o progresso nao avanca
        if (statusAposConclusao != CursoStatus.CONCLUIDO) {
            return ResultadoProgressao.REPROVADO_SEM_PROGRESSO;
        }

        // As recompensas da planilha se aplicam apenas ao plano Basico
        if (planoAluno != Plano.BASICO) {
            return ResultadoProgressao.SEM_RECOMPENSA;
        }

        // TDD2 - GREEN: concluindo o 12o curso (11 concluidos antes) com nota > 7.0
        if (cursosConcluidosAntes >= CursosParametros.LIMITE_CURSOS_UPGRADE_PREMIUM.getValor()) {
            return ResultadoProgressao.UPGRADE_PREMIUM;
        }

        // TDD1 - GREEN: menos de 11 cursos concluidos com nota > 7.0
        return ResultadoProgressao.CURSO_BONUS_BASICO;
    }

}
