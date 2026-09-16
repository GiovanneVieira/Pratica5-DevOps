package com.example.gamificacao.grupo_7.service.progressao;

import com.example.gamificacao.grupo_7.enums.CursoStatus;
import com.example.gamificacao.grupo_7.enums.Plano;
import com.example.gamificacao.grupo_7.model.Curso;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testes de aceitacao da regra de decisao dos tres ciclos ATDD, derivados dos
 * cenarios Given/When/Then da planilha Template_ATDD_Gamificacao.xlsx (aba pb).
 * Teste puro, sem mocks: aplica notas reais via Curso.conclui() e verifica o
 * ResultadoProgressao esperado de cada cenario.
 */
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

    /**
     * TDD1 - cenario de aceitacao (RED do ciclo original):
     *
     * Dado um aluno com assinatura basica ativa
     * E com menos de 11 cursos concluidos
     * Quando o aluno conclui um curso
     * E obtem nota final superior a 7,0
     * Entao o sistema deve liberar o acesso a 3 novos cursos
     * E manter a assinatura no plano basico
     */
    @Test
    void bdd1_alunoBasicoComMenosDe11ConcluidosENotaSuperiorASeteDeveRetornarCursoBonusBasico(){
        assertEquals(ResultadoProgressao.CURSO_BONUS_BASICO,
                this.politica.avaliar(this.statusAposConcluir(8.0), Plano.BASICO, 0));
        assertEquals(ResultadoProgressao.CURSO_BONUS_BASICO,
                this.politica.avaliar(this.statusAposConcluir(7.1), Plano.BASICO, 5));
        assertEquals(ResultadoProgressao.CURSO_BONUS_BASICO,
                this.politica.avaliar(this.statusAposConcluir(10.0), Plano.BASICO, 10));
    }

    /**
     * TDD2 - cenario de aceitacao (RED do ciclo original):
     *
     * Dado um aluno com plano basico e 11 cursos concluidos
     * E com todas as avaliacoes validadas
     * Quando o aluno conclui o seu 12o curso
     * E obtem nota final superior a 7,0
     * Entao a assinatura deve ser alterada para "Premium"
     * E conceder 3 cursos, 3 moedas e voucher para projetos reais
     */
    @Test
    void bdd2_alunoBasicoCom11ConcluidosConcluindo12oComNotaSuperiorASeteDeveRetornarUpgradePremium(){
        assertEquals(ResultadoProgressao.UPGRADE_PREMIUM,
                this.politica.avaliar(this.statusAposConcluir(8.0), Plano.BASICO, 11));
        assertEquals(ResultadoProgressao.UPGRADE_PREMIUM,
                this.politica.avaliar(this.statusAposConcluir(9.5), Plano.BASICO, 15));
    }

    /**
     * TDD3 - cenario de aceitacao (RED do ciclo original):
     *
     * Dado um aluno com assinatura basica ativa
     * E matriculado em um curso da grade
     * Quando o aluno conclui o curso
     * E obtem nota final igual ou inferior a 7,0
     * Entao nenhum curso adicional deve ser liberado
     * E o progresso para o plano Premium nao deve ser incrementado
     */
    @Test
    void bdd3_notaIgualOuInferiorASeteDeveRetornarReprovadoSemProgresso(){
        assertEquals(ResultadoProgressao.REPROVADO_SEM_PROGRESSO,
                this.politica.avaliar(this.statusAposConcluir(7.0), Plano.BASICO, 5));
        assertEquals(ResultadoProgressao.REPROVADO_SEM_PROGRESSO,
                this.politica.avaliar(this.statusAposConcluir(6.5), Plano.BASICO, 11));
        assertEquals(ResultadoProgressao.REPROVADO_SEM_PROGRESSO,
                this.politica.avaliar(this.statusAposConcluir(6.5), Plano.PREMIUM, 3));
    }

    /**
     * Caso de borda fora da planilha: aluno ja Premium que conclui curso com
     * nota superior a 7,0 nao recebe novas recompensas (as recompensas dos
     * cenarios TDD1/TDD2 se aplicam apenas ao plano basico).
     */
    @Test
    void alunoPremiumAprovadoDeveRetornarSemRecompensa(){
        assertEquals(ResultadoProgressao.SEM_RECOMPENSA,
                this.politica.avaliar(this.statusAposConcluir(9.0), Plano.PREMIUM, 3));
        assertEquals(ResultadoProgressao.SEM_RECOMPENSA,
                this.politica.avaliar(this.statusAposConcluir(9.0), Plano.PREMIUM, 11));
    }

}
