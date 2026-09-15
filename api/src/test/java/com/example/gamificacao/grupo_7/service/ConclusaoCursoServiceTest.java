package com.example.gamificacao.grupo_7.service;

import com.example.gamificacao.grupo_7.dto.matricula.ConcluirCursoRequestDTO;
import com.example.gamificacao.grupo_7.dto.matricula.MatriculaResponseDTO;
import com.example.gamificacao.grupo_7.dto.matricula.VoucherResponseDTO;
import com.example.gamificacao.grupo_7.enums.CursoStatus;
import com.example.gamificacao.grupo_7.enums.Plano;
import com.example.gamificacao.grupo_7.enums.VoucherStatus;
import com.example.gamificacao.grupo_7.exception.aluno.AlunoNotFoundException;
import com.example.gamificacao.grupo_7.exception.matricula.CursoJaConcluidoException;
import com.example.gamificacao.grupo_7.exception.matricula.MatriculaNotFoundException;
import com.example.gamificacao.grupo_7.mapper.MatriculaMapper;
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
        when(this.matriculaRepository.countByAlunoIdAndCursoStatus(ALUNO_ID, CursoStatus.CONCLUIDO)).thenReturn(5L);
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
        when(this.matriculaMapper.toVoucherResponseDTO(any(Voucher.class)))
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
                .thenReturn(new Recompensa(List.of(this.buildMatriculaAtiva(aluno)), null));

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
        var matricula = this.buildMatriculaAtiva(aluno);
        this.stubBuscas(aluno, matricula);
        when(this.matriculaRepository.countByAlunoIdAndCursoStatus(ALUNO_ID, CursoStatus.CONCLUIDO)).thenReturn(11L);
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
                .thenReturn(new Recompensa(List.of(this.buildMatriculaAtiva(aluno)), voucher));

        var resposta = this.conclusaoCursoService.concluir(ALUNO_ID, MATRICULA_ID, new ConcluirCursoRequestDTO(8.0));

        assertEquals(CursoStatus.CONCLUIDO, resposta.status());
        assertEquals(1, resposta.cursosLiberados().size());
        assertNotNull(resposta.voucher());
        assertEquals(VoucherStatus.VALIDO, resposta.voucher().status());
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
        verifyNoInteractions(this.recompensasService);
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
        verify(this.matriculaRepository, never()).countByAlunoIdAndCursoStatus(any(UUID.class), any(CursoStatus.class));
        verifyNoInteractions(this.politicaProgressao, this.recompensasService);
    }

    @Test
    void deveContarConcluidosAntesDeAplicarANota(){
        var aluno = this.buildAlunoBasico();
        var matricula = this.buildMatriculaAtiva(aluno);
        this.stubBuscas(aluno, matricula);
        when(this.politicaProgressao.avaliar(any(CursoStatus.class), any(Plano.class), anyLong()))
                .thenReturn(ResultadoProgressao.REPROVADO_SEM_PROGRESSO);

        this.conclusaoCursoService.concluir(ALUNO_ID, MATRICULA_ID, new ConcluirCursoRequestDTO(5.0));

        // A contagem deve refletir os concluidos ANTES desta conclusao (5L, nao 6L)
        verify(this.matriculaRepository).countByAlunoIdAndCursoStatus(ALUNO_ID, CursoStatus.CONCLUIDO);
        verify(this.politicaProgressao).avaliar(any(CursoStatus.class), any(Plano.class), eq(5L));
    }

}
