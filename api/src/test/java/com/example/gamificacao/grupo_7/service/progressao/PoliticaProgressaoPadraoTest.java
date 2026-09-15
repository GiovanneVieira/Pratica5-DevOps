package com.example.gamificacao.grupo_7.service.progressao;

import com.example.gamificacao.grupo_7.enums.CursoStatus;
import com.example.gamificacao.grupo_7.enums.Plano;
import com.example.gamificacao.grupo_7.model.Curso;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PoliticaProgressaoPadraoTest {

    private final PoliticaProgressao politica = new PoliticaProgressaoPadrao();

    private CursoStatus statusAposConcluir(double notaFinal){
        var curso = Curso.builder()
                .name("Curso Teste")
                .status(CursoStatus.INICIADO)
                .notaFinal(notaFinal)
                .build();
        curso.conclui();
        return curso.getStatus();
    }

    @Test
    void bdd1_alunoBasicoComMenosDe11ConcluidosENotaSuperiorASeteDeveRetornarCursoBonusBasico(){
        assertEquals(ResultadoProgressao.CURSO_BONUS_BASICO,
                this.politica.avaliar(this.statusAposConcluir(8.0), Plano.BASICO, 0));
        assertEquals(ResultadoProgressao.CURSO_BONUS_BASICO,
                this.politica.avaliar(this.statusAposConcluir(7.1), Plano.BASICO, 5));
        assertEquals(ResultadoProgressao.CURSO_BONUS_BASICO,
                this.politica.avaliar(this.statusAposConcluir(10.0), Plano.BASICO, 10));
    }

    @Test
    void bdd2_alunoBasicoCom11ConcluidosConcluindo12oComNotaSuperiorASeteDeveRetornarUpgradePremium(){
        assertEquals(ResultadoProgressao.UPGRADE_PREMIUM,
                this.politica.avaliar(this.statusAposConcluir(8.0), Plano.BASICO, 11));
        assertEquals(ResultadoProgressao.UPGRADE_PREMIUM,
                this.politica.avaliar(this.statusAposConcluir(9.5), Plano.BASICO, 15));
    }

    @Test
    void bdd3_notaIgualOuInferiorASeteDeveRetornarReprovadoSemProgresso(){
        assertEquals(ResultadoProgressao.REPROVADO_SEM_PROGRESSO,
                this.politica.avaliar(this.statusAposConcluir(7.0), Plano.BASICO, 5));
        assertEquals(ResultadoProgressao.REPROVADO_SEM_PROGRESSO,
                this.politica.avaliar(this.statusAposConcluir(6.5), Plano.BASICO, 11));
        assertEquals(ResultadoProgressao.REPROVADO_SEM_PROGRESSO,
                this.politica.avaliar(this.statusAposConcluir(6.5), Plano.PREMIUM, 3));
    }

    @Test
    void alunoPremiumAprovadoDeveRetornarSemRecompensa(){
        assertEquals(ResultadoProgressao.SEM_RECOMPENSA,
                this.politica.avaliar(this.statusAposConcluir(9.0), Plano.PREMIUM, 3));
        assertEquals(ResultadoProgressao.SEM_RECOMPENSA,
                this.politica.avaliar(this.statusAposConcluir(9.0), Plano.PREMIUM, 11));
    }

}
