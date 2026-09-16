package com.example.gamificacao.grupo_7.controller;

import com.example.gamificacao.grupo_7.AbstractIntegrationTest;
import com.example.gamificacao.grupo_7.dto.aluno.AlunoRequestDTO;
import com.example.gamificacao.grupo_7.dto.aluno.AlunoResponseDTO;
import com.example.gamificacao.grupo_7.dto.matricula.ConcluirCursoRequestDTO;
import com.example.gamificacao.grupo_7.dto.matricula.ConcluirCursoResponseDTO;
import com.example.gamificacao.grupo_7.dto.matricula.MatriculaRequestDTO;
import com.example.gamificacao.grupo_7.dto.matricula.MatriculaResponseDTO;
import com.example.gamificacao.grupo_7.dto.voucher.VoucherResponseDTO;
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

import static org.junit.jupiter.api.Assertions.*;

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

    /**
     * Desistencia de curso (ciclo ATDD do endpoint de remocao):
     *
     *   Dado um aluno matriculado em um curso
     *   Quando ele desiste (DELETE /alunos/{id}/matriculas/{matriculaId})
     *   Entao recebe 204
     *   E a matricula (e o curso) saem do historico
     *   E matricula de outro aluno permanece intacta
     */
    @Test
    void desistirDeCursoDeveRemoverMatriculaDoHistorico(){
        var alunoId = this.criaAluno("desistencia@teste.com");
        var outroAlunoId = this.criaAluno("outro@teste.com");
        var matriculaId = this.matricula(alunoId, "Curso a Desistir");
        var matriculaDeOutro = this.matricula(outroAlunoId, "Curso do Outro");

        var resposta = this.client.delete()
                .uri("/alunos/" + alunoId + "/matriculas/" + matriculaId)
                .retrieve()
                .toBodilessEntity();

        assertEquals(HttpStatus.NO_CONTENT, resposta.getStatusCode());
        assertEquals(List.of(), this.listaMatriculas(alunoId));
        assertEquals(1, this.listaMatriculas(outroAlunoId).size());
        assertEquals("Curso do Outro", this.listaMatriculas(outroAlunoId).getFirst().nomeCurso());
        assertEquals(matriculaDeOutro, this.listaMatriculas(outroAlunoId).getFirst().id());

        // Desistir de matricula ja removida deve devolver 404
        var excecao = assertThrows(
                org.springframework.web.client.RestClientResponseException.class,
                () -> this.client.delete()
                        .uri("/alunos/" + alunoId + "/matriculas/" + matriculaId)
                        .retrieve()
                        .toBodilessEntity());
        assertEquals(HttpStatus.NOT_FOUND, excecao.getStatusCode());
    }

    /**
     * TDD4 - cenario de aceitacao (integracao de ponta a ponta): apagar um
     * curso concluido do historico nao apaga o progresso do aluno.
     *
     * Dado um aluno basico com 11 cursos concluidos (progresso persistido = 11)
     * Quando ele apaga um curso concluido do historico
     * E conclui um novo curso com nota superior a 7,0
     * Entao o progresso acumulado (11 + 1) deve transforma-lo em Premium
     *   independentemente de quantos cursos restaram no historico
     * E o progresso oficial persistido deve ser 12
     */
    @Test
    void tdd4_apagarCursoConcluidoDoHistoricoDeveManterOProgressoDoAluno(){
        var alunoId = this.criaAluno("tdd4@teste.com");

        for (int i = 1; i <= 12; i++){
            this.matricula(alunoId, "Curso " + i);
        }
        var matriculas = this.listaMatriculas(alunoId);
        for (int i = 0; i < 11; i++){
            this.conclui(alunoId, matriculas.get(i).id(), 9.0);
        }

        var apagada = matriculas.get(0).id();
        var respostaDelete = this.client.delete()
                .uri("/alunos/" + alunoId + "/matriculas/" + apagada)
                .retrieve()
                .toBodilessEntity();
        assertEquals(HttpStatus.NO_CONTENT, respostaDelete.getStatusCode());
        var historicoAposApagar = this.listaMatriculas(alunoId);
        assertTrue(historicoAposApagar.stream().noneMatch(m -> m.id().equals(apagada)),
                "Curso apagado deve sair do historico");

        var iniciada = historicoAposApagar.stream()
                .filter(m -> m.status() == CursoStatus.INICIADO)
                .findFirst()
                .orElseThrow();
        var resposta = this.conclui(alunoId, iniciada.id(), 9.0);

        assertEquals(CursoStatus.CONCLUIDO, resposta.status());
        assertEquals(Plano.PREMIUM, resposta.plano());
        assertEquals(3, resposta.moedas());
        assertNotNull(resposta.voucher());
        assertEquals(12, this.buscaAluno(alunoId).cursosConcluidos());
    }

    /**
     * TDD5 - cenario de aceitacao (integracao de ponta a ponta): recorrencia
     * do BDD2 - as recompensas voltam a cada 12 cursos concluidos.
     *
     * Dado um aluno Premium que ja recebeu o pacote do 12o curso
     * Quando ele conclui mais 11 cursos com nota superior a 7,0
     * E conclui o 24o curso com nota superior a 7,0
     * Entao deve receber novamente 3 moedas (saldo 6) e um novo voucher
     *   sem novo upgrade de plano e sem cursos bonus
     */
    @Test
    void tdd5_alunoPremiumAoConcluir24oCursoRecebeNovamenteMoedasEVoucher(){
        var alunoId = this.criaAluno("tdd5@teste.com");

        this.matricula(alunoId, "Curso Semente");
        var matriculas = this.listaMatriculas(alunoId);
        this.conclui(alunoId, matriculas.get(0).id(), 9.0); // 1 concluido, 3 bonus

        // progressos 2 a 12: cada conclusao libera mais 3 matriculas INICIADAS
        for (int progresso = 2; progresso <= 12; progresso++){
            var iniciada = this.primeiraIniciada(alunoId);
            this.conclui(alunoId, iniciada, 9.0);
        }
        var alunoAposUpgrade = this.buscaAluno(alunoId);
        assertEquals(Plano.PREMIUM, alunoAposUpgrade.plano());
        assertEquals(3, alunoAposUpgrade.moedas());

        // progressos 13 a 23: premium fora dos multiplos de 12, sem recompensas
        for (int progresso = 13; progresso <= 23; progresso++){
            var iniciada = this.primeiraIniciada(alunoId);
            this.conclui(alunoId, iniciada, 9.0);
        }

        var resposta = this.conclui(alunoId, this.primeiraIniciada(alunoId), 9.0); // 24o

        assertEquals(CursoStatus.CONCLUIDO, resposta.status());
        assertEquals(Plano.PREMIUM, resposta.plano());
        assertEquals(6, resposta.moedas());
        assertEquals(3, resposta.moedasRecebidas());
        assertFalse(resposta.upgradePremium());
        assertEquals(List.of(), resposta.cursosLiberados());
        assertNotNull(resposta.voucher());

        var alunoFinal = this.buscaAluno(alunoId);
        assertEquals(24, alunoFinal.cursosConcluidos());
        assertEquals(6, alunoFinal.moedas());
    }

    private UUID primeiraIniciada(UUID alunoId){
        return this.listaMatriculas(alunoId)
                .stream()
                .filter(m -> m.status() == CursoStatus.INICIADO)
                .findFirst()
                .orElseThrow()
                .id();
    }

    /**
     * TDD6 - cenario de aceitacao (integracao de ponta a ponta): os vouchers
     * conquistados precisam ser consultaveis depois de conquistados (o painel
     * "Seus vouchers" faz refresh e nao pode depender da sessao).
     *
     * Dado um aluno Premium que recebeu o voucher do 12o curso
     * E o voucher recorrente do 24o
     * Quando a UI busca GET /alunos/{id}/vouchers
     * Entao deve receber os 2 vouchers persistidos com nome, valor e status
     * E um aluno inexistente recebe 404
     */
    @Test
    void tdd6_buscarVouchersDeveRetornarOsVouchersPersistidosDoAluno(){
        var alunoId = this.criaAluno("tdd6@teste.com");

        this.matricula(alunoId, "Curso Semente");
        this.conclui(alunoId, this.listaMatriculas(alunoId).getFirst().id(), 9.0);
        for (int progresso = 2; progresso <= 24; progresso++){
            this.conclui(alunoId, this.primeiraIniciada(alunoId), 9.0);
        }

        VoucherResponseDTO[] vouchers = this.client.get()
                .uri("/alunos/" + alunoId + "/vouchers")
                .retrieve()
                .body(VoucherResponseDTO[].class);

        assertEquals(2, vouchers.length);
        assertEquals("Voucher Projetos Reais", vouchers[0].nome());
        assertEquals(100.0, vouchers[0].valorEmReais());
        assertEquals(VoucherStatus.VALIDO, vouchers[0].status());
        assertNotNull(vouchers[0].expiresAt());
        assertEquals(VoucherStatus.VALIDO, vouchers[1].status());

        var excecao = assertThrows(
                org.springframework.web.client.RestClientResponseException.class,
                () -> this.client.get()
                        .uri("/alunos/" + UUID.randomUUID() + "/vouchers")
                        .retrieve()
                        .body(VoucherResponseDTO[].class));
        assertEquals(HttpStatus.NOT_FOUND, excecao.getStatusCode());
    }

}
