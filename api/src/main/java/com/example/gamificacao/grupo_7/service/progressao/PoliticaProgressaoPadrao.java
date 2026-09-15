package com.example.gamificacao.grupo_7.service.progressao;

import com.example.gamificacao.grupo_7.enums.CursoStatus;
import com.example.gamificacao.grupo_7.enums.CursosParametros;
import com.example.gamificacao.grupo_7.enums.Plano;
import org.springframework.stereotype.Component;

@Component
public class PoliticaProgressaoPadrao implements PoliticaProgressao {

    @Override
    public ResultadoProgressao avaliar(CursoStatus statusAposConclusao, Plano planoAluno, long cursosConcluidosAntes) {
        // BDD3: nota <= 7.0 reprova o curso: nada e liberado e o progresso nao avanca
        if (statusAposConclusao != CursoStatus.CONCLUIDO) {
            return ResultadoProgressao.REPROVADO_SEM_PROGRESSO;
        }

        // As recompensas da planilha se aplicam apenas ao plano Basico
        if (planoAluno != Plano.BASICO) {
            return ResultadoProgressao.SEM_RECOMPENSA;
        }

        // BDD2: concluindo o 12o curso (11 concluidos antes) com nota > 7.0
        if (cursosConcluidosAntes >= CursosParametros.LIMITE_CURSOS_UPGRADE_PREMIUM.getValor()) {
            return ResultadoProgressao.UPGRADE_PREMIUM;
        }

        // BDD1: menos de 11 cursos concluidos com nota > 7.0
        return ResultadoProgressao.CURSO_BONUS_BASICO;
    }

}
