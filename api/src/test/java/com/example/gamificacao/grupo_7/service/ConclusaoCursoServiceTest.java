package com.example.gamificacao.grupo_7.service;

import com.example.gamificacao.grupo_7.dto.matricula.ConcluirCursoRequestDTO;
import com.example.gamificacao.grupo_7.dto.matricula.MatriculaResponseDTO;
import com.example.gamificacao.grupo_7.dto.voucher.VoucherResponseDTO;
import com.example.gamificacao.grupo_7.enums.CursoStatus;
import com.example.gamificacao.grupo_7.enums.Plano;
import com.example.gamificacao.grupo_7.enums.VoucherStatus;
import com.example.gamificacao.grupo_7.exception.aluno.AlunoNotFoundException;
import com.example.gamificacao.grupo_7.exception.matricula.CursoJaConcluidoException;
import com.example.gamificacao.grupo_7.exception.matricula.MatriculaNotFoundException;
import com.example.gamificacao.grupo_7.mapper.MatriculaMapper;
import com.example.gamificacao.grupo_7.mapper.VoucherMapper;
import com.example.gamificacao.grupo_7.model.Aluno;
import com.example.gamificacao.grupo_7.model.Curso;
import com.example.gamificacao.grupo_7.model.Matricula;
import com.example.gamificacao.grupo_7.model.Recompensa;
import com.example.gamificacao.grupo_7.model.Voucher;
import com.example.gamificacao.grupo_7.model.validation_object.RA;
import com.example.gamificacao.grupo_7.model.validation_object.VoucherValue;
import com.example.gamificacao.grupo_7.repository.AlunoRepository;
import com.example.gamificacao.grupo_7.repository.MatriculaRepository;
import com.example.gamificacao.grupo_7.service.progressao.PoliticaProgressao;
import com.example.gamificacao.grupo_7.service.progressao.ResultadoProgressao;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Testes de orquestracao do caso de uso de conclusao de curso (ciclos ATDD
 * TDD1/TDD2/TDD3): a PoliticaProgressao e o RecompensasService sao mockados
 * para verificar apenas a coordenacao - quem decide e a politica, quem concede
 * e o servico de recompensas. As regras em si sao cobertas por
 * PoliticaProgressaoPadraoTest e RecompensasServiceTest; o fluxo HTTP completo
 * por MatriculaFlowIntegrationTest.
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
public class ConclusaoCursoServiceTest {

    private static final UUID ALUNO_ID = UUID.randomUUID();
    private static final UUID MATRICULA_ID = UUID.randomUUID();

    @Mock
    MatriculaRepository matriculaRepository;
    @Mock
    AlunoRepository alunoRepository;
    @Mock
    MatriculaMapper matriculaMapper;
    @Mock
    VoucherMapper voucherMapper;
    @Mock
    PoliticaProgressao politicaProgressao;
    @Mock
    RecompensasService recompensasService;

    @InjectMocks
    ConclusaoCursoService conclusaoCursoService;

    private Aluno buildAlunoBasico(){
        return Aluno.builder()
                .id(ALUNO_ID)
                .name("Aluno Teste")
                .email("aluno@teste.com")
                .password("123456")
                .ra(new RA("2026123"))
                .plano(Plano.BASICO)
                .moedas(0)
                .cursosConcluidos(5)
                .build();
    }

    private Matricula buildMatriculaAtiva(Aluno aluno){
        var curso = Curso.builder()
                .id(UUID.randomUUID())
                .name("Curso Teste")
                .status(CursoStatus.INICIADO)
                .notaFinal(0.0)
                .build();
        return Matricula.builder()
                .id(MATRICULA_ID)
                .aluno(aluno)
                .curso(curso)
                .build();
    }

    private void stubBuscas(Aluno aluno, Matricula matricula){
        when(this.alunoRepository.findById(ALUNO_ID)).thenReturn(Optional.of(aluno));
        when(this.matriculaRepository.findByIdAndAlunoId(MATRICULA_ID, ALUNO_ID)).thenReturn(Optional.of(matricula));
        when(this.matriculaRepository.save(any(Matricula.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private void stubMapperCursosLiberados(){
        when(this.matriculaMapper.toResponseDTO(any(Matricula.class)))
                .thenAnswer(inv -> {
                    var matricula = inv.getArgument(0, Matricula.class);
                    return new MatriculaResponseDTO(
                            matricula.getId(),
                            matricula.getCurso().getName(),
                            matricula.getCurso().getStatus(),
                            matricula.getCurso().getNotaFinal()
                    );
                });
    }

    private void stubMapperVoucher(){
        when(this.voucherMapper.toResponseDTO(any(Voucher.class)))
                .thenAnswer(inv -> {
                    var voucher = inv.getArgument(0, Voucher.class);
                    return new VoucherResponseDTO(
                            voucher.getId(),
                            voucher.getNome(),
                            voucher.getValor().getValorEmCentavos() / 100.0,
                            voucher.getDescricao(),
                            voucher.getStatus(),
                            voucher.getExpiresAt()
                    );
                });
    }

    @Test
    void deveLiberarCursosBonusQuandoPoliticaIndicaCursoBonusBasico(){
        log.info("BDD1 (orquestracao): politica indica curso bonus basico");
        var aluno = this.buildAlunoBasico();
        var matricula = this.buildMatriculaAtiva(aluno);
        this.stubBuscas(aluno, matricula);
        this.stubMapperCursosLiberados();
        when(this.politicaProgressao.avaliar(any(CursoStatus.class), any(Plano.class), anyLong()))
                .thenReturn(ResultadoProgressao.CURSO_BONUS_BASICO);
        when(this.recompensasService.liberarCursosBonus(aluno))
                .thenReturn(new Recompensa(List.of(this.buildMatriculaAtiva(aluno)), null, 0));

        var resposta = this.conclusaoCursoService.concluir(ALUNO_ID, MATRICULA_ID, new ConcluirCursoRequestDTO(8.0));

        assertEquals(CursoStatus.CONCLUIDO, resposta.status());
        assertEquals(1, resposta.cursosLiberados().size());
        assertNull(resposta.voucher());
        verify(this.politicaProgressao).avaliar(CursoStatus.CONCLUIDO, Plano.BASICO, 5L);
        verify(this.recompensasService).liberarCursosBonus(aluno);
        verify(this.recompensasService, never()).concederRecompensasPremium(any());
    }

    @Test
    void deveConcederRecompensasPremiumQuandoPoliticaIndicaUpgrade(){
        log.info("BDD2 (orquestracao): politica indica upgrade premium");
        var aluno = this.buildAlunoBasico();
        aluno.setCursosConcluidos(11);
        var matricula = this.buildMatriculaAtiva(aluno);
        this.stubBuscas(aluno, matricula);
        this.stubMapperCursosLiberados();
        this.stubMapperVoucher();
        when(this.politicaProgressao.avaliar(any(CursoStatus.class), any(Plano.class), anyLong()))
                .thenReturn(ResultadoProgressao.UPGRADE_PREMIUM);
        var voucher = Voucher.builder()
                .id(UUID.randomUUID())
                .nome("Voucher Projetos Reais")
                .valor(new VoucherValue(100.0))
                .descricao("Voucher para projetos reais")
                .aluno(aluno)
                .status(VoucherStatus.VALIDO)
                .build();
        when(this.recompensasService.concederRecompensasPremium(aluno))
                .thenReturn(new Recompensa(List.of(this.buildMatriculaAtiva(aluno)), voucher, 3));

        var resposta = this.conclusaoCursoService.concluir(ALUNO_ID, MATRICULA_ID, new ConcluirCursoRequestDTO(8.0));

        assertEquals(CursoStatus.CONCLUIDO, resposta.status());
        assertEquals(1, resposta.cursosLiberados().size());
        assertNotNull(resposta.voucher());
        assertEquals(VoucherStatus.VALIDO, resposta.voucher().status());
        assertTrue(resposta.upgradePremium(), "Upgrade para Premium deve ser sinalizado no contrato");
        assertEquals(3, resposta.moedasRecebidas());
        verify(this.politicaProgressao).avaliar(CursoStatus.CONCLUIDO, Plano.BASICO, 11L);
        verify(this.recompensasService).concederRecompensasPremium(aluno);
        verify(this.recompensasService, never()).liberarCursosBonus(any());
    }

    @Test
    void deveRetornarSemRecompensasQuandoReprovado(){
        log.info("BDD3 (orquestracao): nota 7.0 reprova e nao concede recompensas");
        var aluno = this.buildAlunoBasico();
        var matricula = this.buildMatriculaAtiva(aluno);
        this.stubBuscas(aluno, matricula);
        when(this.politicaProgressao.avaliar(any(CursoStatus.class), any(Plano.class), anyLong()))
                .thenReturn(ResultadoProgressao.REPROVADO_SEM_PROGRESSO);

        var resposta = this.conclusaoCursoService.concluir(ALUNO_ID, MATRICULA_ID, new ConcluirCursoRequestDTO(7.0));

        assertEquals(CursoStatus.REPROVADO, resposta.status());
        assertEquals(Plano.BASICO, resposta.plano());
        assertEquals(List.of(), resposta.cursosLiberados());
        assertNull(resposta.voucher());
        verify(this.politicaProgressao).avaliar(CursoStatus.REPROVADO, Plano.BASICO, 5L);
        verifyNoInteractions(this.recompensasService);
    }

    @Test
    void deveRetornarSemRecompensasQuandoAlunoJaPremium(){
        var aluno = this.buildAlunoBasico();
        aluno.setPlano(Plano.PREMIUM);
        aluno.setMoedas(10);
        var matricula = this.buildMatriculaAtiva(aluno);
        this.stubBuscas(aluno, matricula);
        when(this.politicaProgressao.avaliar(any(CursoStatus.class), any(Plano.class), anyLong()))
                .thenReturn(ResultadoProgressao.SEM_RECOMPENSA);

        var resposta = this.conclusaoCursoService.concluir(ALUNO_ID, MATRICULA_ID, new ConcluirCursoRequestDTO(9.0));

        assertEquals(CursoStatus.CONCLUIDO, resposta.status());
        assertEquals(Plano.PREMIUM, resposta.plano());
        assertEquals(List.of(), resposta.cursosLiberados());
        assertNull(resposta.voucher());
        assertFalse(resposta.upgradePremium());
        assertEquals(0, resposta.moedasRecebidas());
        verifyNoInteractions(this.recompensasService);
    }

    /**
     * TDD5 (orquestracao): recorrencia do BDD2 - a cada 12 cursos concluidos
     * com nota superior a 7,0 o aluno Premium recebe 3 moedas e voucher de novo.
     */
    @Test
    void deveConcederRecompensasRecorrentesQuandoPoliticaIndicaMultiploDe12(){
        log.info("TDD5 (orquestracao): politica indica recompensa recorrente (multiplo de 12)");
        var aluno = this.buildAlunoBasico();
        aluno.setPlano(Plano.PREMIUM);
        aluno.setMoedas(3);
        aluno.setCursosConcluidos(23);
        var matricula = this.buildMatriculaAtiva(aluno);
        this.stubBuscas(aluno, matricula);
        this.stubMapperVoucher();
        when(this.politicaProgressao.avaliar(any(CursoStatus.class), any(Plano.class), anyLong()))
                .thenReturn(ResultadoProgressao.RECOMPENSA_RECURRENTE);
        var voucher = Voucher.builder()
                .id(UUID.randomUUID())
                .nome("Voucher Projetos Reais")
                .valor(new VoucherValue(100.0))
                .descricao("Voucher para participacao em projetos reais, concedido a cada 12 cursos concluidos")
                .aluno(aluno)
                .status(VoucherStatus.VALIDO)
                .build();
        when(this.recompensasService.concederRecompensasRecorrentes(aluno))
                .thenAnswer(inv -> {
                    // simula o efeito do servico real sobre o aluno mockado
                    aluno.setMoedas(aluno.getMoedas() + 3);
                    return new Recompensa(List.of(), voucher, 3);
                });

        var resposta = this.conclusaoCursoService.concluir(ALUNO_ID, MATRICULA_ID, new ConcluirCursoRequestDTO(9.0));

        assertEquals(CursoStatus.CONCLUIDO, resposta.status());
        assertEquals(Plano.PREMIUM, resposta.plano());
        assertEquals(6, resposta.moedas());
        assertEquals(3, resposta.moedasRecebidas());
        assertFalse(resposta.upgradePremium(), "Recorrencia nao e um novo upgrade de plano");
        assertEquals(List.of(), resposta.cursosLiberados());
        assertNotNull(resposta.voucher());
        assertEquals(24, aluno.getCursosConcluidos());
        verify(this.recompensasService).concederRecompensasRecorrentes(aluno);
        verify(this.recompensasService, never()).concederRecompensasPremium(any());
        verify(this.recompensasService, never()).liberarCursosBonus(any());
    }

    @Test
    void deveLancarExcecaoQuandoAlunoNaoExiste(){
        when(this.alunoRepository.findById(ALUNO_ID)).thenReturn(Optional.empty());

        assertThrows(AlunoNotFoundException.class,
                () -> this.conclusaoCursoService.concluir(ALUNO_ID, MATRICULA_ID, new ConcluirCursoRequestDTO(8.0)));
    }

    @Test
    void deveLancarExcecaoQuandoMatriculaNaoExiste(){
        var aluno = this.buildAlunoBasico();
        when(this.alunoRepository.findById(ALUNO_ID)).thenReturn(Optional.of(aluno));
        when(this.matriculaRepository.findByIdAndAlunoId(MATRICULA_ID, ALUNO_ID)).thenReturn(Optional.empty());

        assertThrows(MatriculaNotFoundException.class,
                () -> this.conclusaoCursoService.concluir(ALUNO_ID, MATRICULA_ID, new ConcluirCursoRequestDTO(8.0)));
    }

    @Test
    void deveLancarExcecaoQuandoCursoJaFoiConcluido(){
        var aluno = this.buildAlunoBasico();
        var matricula = this.buildMatriculaAtiva(aluno);
        matricula.getCurso().setStatus(CursoStatus.CONCLUIDO);
        when(this.alunoRepository.findById(ALUNO_ID)).thenReturn(Optional.of(aluno));
        when(this.matriculaRepository.findByIdAndAlunoId(MATRICULA_ID, ALUNO_ID)).thenReturn(Optional.of(matricula));

        assertThrows(CursoJaConcluidoException.class,
                () -> this.conclusaoCursoService.concluir(ALUNO_ID, MATRICULA_ID, new ConcluirCursoRequestDTO(8.0)));
        assertEquals(5, aluno.getCursosConcluidos(), "Curso ja concluido nao deve alterar o progresso");
        verifyNoInteractions(this.politicaProgressao, this.recompensasService);
    }

    /**
     * TDD4 - progresso persistido (RED):
     *
     *   Dado um aluno com 5 cursos concluidos no progresso persistido
     *   Quando ele conclui um novo curso com nota superior a 7,0
     *   Entao a politica deve ser consultada com o progresso de ANTES (5)
     *   E o progresso persistido do aluno deve passar a 6
     *   E o aluno deve ser persistido com o novo progresso
     */
    @Test
    void deveIncrementarProgressoPersistidoDoAlunoAoConcluirComNotaSuperiorASete(){
        var aluno = this.buildAlunoBasico();
        var matricula = this.buildMatriculaAtiva(aluno);
        this.stubBuscas(aluno, matricula);
        when(this.politicaProgressao.avaliar(any(CursoStatus.class), any(Plano.class), anyLong()))
                .thenReturn(ResultadoProgressao.CURSO_BONUS_BASICO);
        when(this.recompensasService.liberarCursosBonus(aluno)).thenReturn(Recompensa.vazia());

        this.conclusaoCursoService.concluir(ALUNO_ID, MATRICULA_ID, new ConcluirCursoRequestDTO(8.0));

        verify(this.politicaProgressao).avaliar(CursoStatus.CONCLUIDO, Plano.BASICO, 5L);
        assertEquals(6, aluno.getCursosConcluidos(), "Conclusao com nota superior a 7,0 deve incrementar o progresso");
        verify(this.alunoRepository).save(aluno);
    }

    /**
     * TDD4 - progresso persistido (RED):
     *
     *   Dado um aluno com 5 cursos concluidos no progresso persistido
     *   Quando ele conclui um curso com nota igual ou inferior a 7,0 (reprovado)
     *   Entao o progresso persistido deve permanecer 5
     */
    @Test
    void naoDeveIncrementarProgressoPersistidoQuandoReprovado(){
        var aluno = this.buildAlunoBasico();
        var matricula = this.buildMatriculaAtiva(aluno);
        this.stubBuscas(aluno, matricula);
        when(this.politicaProgressao.avaliar(any(CursoStatus.class), any(Plano.class), anyLong()))
                .thenReturn(ResultadoProgressao.REPROVADO_SEM_PROGRESSO);

        this.conclusaoCursoService.concluir(ALUNO_ID, MATRICULA_ID, new ConcluirCursoRequestDTO(5.0));

        assertEquals(5, aluno.getCursosConcluidos(), "Reprovacao nao deve incrementar o progresso");
        verify(this.alunoRepository, never()).save(aluno);
    }

}
