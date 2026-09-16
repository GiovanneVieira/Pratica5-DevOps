package com.example.gamificacao.grupo_7.controller;

import com.example.gamificacao.grupo_7.AbstractIntegrationTest;
import com.example.gamificacao.grupo_7.dto.aluno.AlunoRequestDTO;
import com.example.gamificacao.grupo_7.dto.aluno.AlunoResponseDTO;
import com.example.gamificacao.grupo_7.dto.matricula.ConcluirCursoRequestDTO;
import com.example.gamificacao.grupo_7.dto.matricula.ConcluirCursoResponseDTO;
import com.example.gamificacao.grupo_7.dto.matricula.MatriculaRequestDTO;
import com.example.gamificacao.grupo_7.dto.matricula.MatriculaResponseDTO;
import com.example.gamificacao.grupo_7.enums.CursoStatus;
import com.example.gamificacao.grupo_7.enums.Plano;
import com.example.gamificacao.grupo_7.enums.VoucherStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Testes de aceitacao de integracao dos ciclos ATDD (planilha
 * Template_ATDD_Gamificacao.xlsx, aba pb): exercitam o fluxo HTTP completo
 * (criar aluno -> matricular -> concluir com nota) contra PostgreSQL real via
 * Testcontainers, verificando os efeitos de cada cenario de ponta a ponta.
 *
 * Cenários cobertos (TDD1, TDD2 e TDD3) documentados em cada teste abaixo.
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MatriculaFlowIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    private RestClient client;

    @BeforeEach
    void setUp(){
        this.client = RestClient.builder()
                .baseUrl("http://localhost:" + this.port)
                .build();
    }

    private UUID criaAluno(String email){
        ResponseEntity<AlunoResponseDTO> resposta = this.client.post()
                .uri("/alunos")
                .body(new AlunoRequestDTO("Aluno " + email, email, "123456"))
                .retrieve()
                .toEntity(AlunoResponseDTO.class);
        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        return resposta.getBody().id();
    }

    private UUID matricula(UUID alunoId, String nomeCurso){
        ResponseEntity<MatriculaResponseDTO> resposta = this.client.post()
                .uri("/alunos/" + alunoId + "/matriculas")
                .body(new MatriculaRequestDTO(nomeCurso))
                .retrieve()
                .toEntity(MatriculaResponseDTO.class);
        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        return resposta.getBody().id();
    }

    private List<MatriculaResponseDTO> listaMatriculas(UUID alunoId){
        MatriculaResponseDTO[] resposta = this.client.get()
                .uri("/alunos/" + alunoId + "/matriculas")
                .retrieve()
                .body(MatriculaResponseDTO[].class);
        return List.of(resposta);
    }

    private ConcluirCursoResponseDTO conclui(UUID alunoId, UUID matriculaId, double notaFinal){
        return this.client.patch()
                .uri("/alunos/" + alunoId + "/matriculas/" + matriculaId + "/conclusao")
                .body(new ConcluirCursoRequestDTO(notaFinal))
                .retrieve()
                .body(ConcluirCursoResponseDTO.class);
    }

    private AlunoResponseDTO buscaAluno(UUID alunoId){
        return this.client.get()
                .uri("/alunos/" + alunoId)
                .retrieve()
                .body(AlunoResponseDTO.class);
    }

    /**
     * TDD1 - cenario de aceitacao (integracao de ponta a ponta):
     *
     * Dado um aluno com assinatura basica ativa
     * E com menos de 11 cursos concluidos (5 concluidos previamente)
     * Quando o aluno conclui um curso
     * E obtem nota final superior a 7,0
     * Entao o sistema deve liberar o acesso a 3 novos cursos
     * E manter a assinatura no plano basico
     */
    @Test
    void bdd1_alunoBasicoComMenosDe11ConcluidosAoConcluirComNotaSuperiorASeteLibera3CursosEManterPlanoBasico(){
        var alunoId = this.criaAluno("bdd1@teste.com");

        for (int i = 1; i <= 6; i++){
            this.matricula(alunoId, "Curso " + i);
        }
        var matriculas = this.listaMatriculas(alunoId);

        // 5 cursos concluidos previamente com nota superior a 7.0
        for (int i = 0; i < 5; i++){
            this.conclui(alunoId, matriculas.get(i).id(), 9.0);
        }

        var resposta = this.conclui(alunoId, matriculas.get(5).id(), 9.0);

        assertEquals(CursoStatus.CONCLUIDO, resposta.status());
        assertEquals(Plano.BASICO, resposta.plano());
        assertEquals(3, resposta.cursosLiberados().size());
        resposta.cursosLiberados().forEach(curso -> assertEquals(CursoStatus.INICIADO, curso.status()));
        assertEquals(0, resposta.moedas());
        assertNull(resposta.voucher());
        assertEquals(Plano.BASICO, this.buscaAluno(alunoId).plano());
    }

    /**
     * TDD2 - cenario de aceitacao (integracao de ponta a ponta):
     *
     * Dado um aluno com plano basico e 11 cursos concluidos
     * E com todas as avaliacoes validadas
     * Quando o aluno conclui o seu 12o curso
     * E obtem nota final superior a 7,0
     * Entao a assinatura deve ser alterada para "Premium"
     * E conceder 3 cursos, 3 moedas e voucher para projetos reais
     */
    @Test
    void bdd2_alunoBasicoAoConcluir12oCursoComNotaSuperiorASeteViraPremiumCom3Cursos3MoedasEVoucher(){
        var alunoId = this.criaAluno("bdd2@teste.com");

        for (int i = 1; i <= 12; i++){
            this.matricula(alunoId, "Curso " + i);
        }
        var matriculas = this.listaMatriculas(alunoId);

        // 11 cursos concluidos previamente: o aluno esta a um curso do upgrade
        for (int i = 0; i < 11; i++){
            this.conclui(alunoId, matriculas.get(i).id(), 9.0);
        }

        var resposta = this.conclui(alunoId, matriculas.get(11).id(), 9.0);

        assertEquals(CursoStatus.CONCLUIDO, resposta.status());
        assertEquals(Plano.PREMIUM, resposta.plano());
        assertEquals(3, resposta.moedas());
        assertEquals(3, resposta.cursosLiberados().size());
        assertNotNull(resposta.voucher());
        assertEquals(VoucherStatus.VALIDO, resposta.voucher().status());
        assertEquals(Plano.PREMIUM, this.buscaAluno(alunoId).plano());
    }

    /**
     * TDD3 - cenario de aceitacao (integracao de ponta a ponta), nota igual a 7,0:
     *
     * Dado um aluno com assinatura basica ativa
     * E matriculado em um curso da grade
     * Quando o aluno conclui o curso
     * E obtem nota final igual ou inferior a 7,0
     * Entao nenhum curso adicional deve ser liberado
     * E o progresso para o plano Premium nao deve ser incrementado
     */
    @Test
    void bdd3_alunoAoConcluirComNotaIgualASeteNaoLiberaCursosNemProgredirParaPremium(){
        this.verificaCenarioNotaInsuficiente("bdd3a@teste.com", 7.0);
    }

    /**
     * TDD3 - cenario de aceitacao (integracao de ponta a ponta), nota inferior a 7,0:
     * mesmo cenario do teste anterior, cobrindo o outro limite da regra.
     */
    @Test
    void bdd3_alunoAoConcluirComNotaInferiorASeteNaoLiberaCursosNemProgredirParaPremium(){
        this.verificaCenarioNotaInsuficiente("bdd3b@teste.com", 6.5);
    }

    private void verificaCenarioNotaInsuficiente(String email, double notaFinal){
        var alunoId = this.criaAluno(email);
        var matriculaId = this.matricula(alunoId, "Curso da Grade");

        var resposta = this.conclui(alunoId, matriculaId, notaFinal);

        assertEquals(CursoStatus.REPROVADO, resposta.status());
        assertEquals(Plano.BASICO, resposta.plano());
        assertEquals(0, resposta.moedas());
        assertEquals(List.of(), resposta.cursosLiberados());
        assertNull(resposta.voucher());

        var matriculasAposConclusao = this.listaMatriculas(alunoId);
        assertEquals(1, matriculasAposConclusao.size());
        assertEquals(CursoStatus.REPROVADO, matriculasAposConclusao.getFirst().status());
        assertEquals(Plano.BASICO, this.buscaAluno(alunoId).plano());
    }

}
