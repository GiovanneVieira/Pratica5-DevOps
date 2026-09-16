package com.example.gamificacao.grupo_7.service;

import com.example.gamificacao.grupo_7.enums.CursoStatus;
import com.example.gamificacao.grupo_7.enums.Plano;
import com.example.gamificacao.grupo_7.enums.VoucherStatus;
import com.example.gamificacao.grupo_7.mapper.CursoMapper;
import com.example.gamificacao.grupo_7.mapper.MatriculaMapper;
import com.example.gamificacao.grupo_7.mapper.VoucherMapper;
import com.example.gamificacao.grupo_7.model.Aluno;
import com.example.gamificacao.grupo_7.model.Curso;
import com.example.gamificacao.grupo_7.model.Matricula;
import com.example.gamificacao.grupo_7.model.Voucher;
import com.example.gamificacao.grupo_7.model.validation_object.RA;
import com.example.gamificacao.grupo_7.model.validation_object.VoucherValue;
import com.example.gamificacao.grupo_7.repository.AlunoRepository;
import com.example.gamificacao.grupo_7.repository.MatriculaRepository;
import com.example.gamificacao.grupo_7.repository.VoucherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecompensasServiceTest {

    @Mock
    MatriculaRepository matriculaRepository;
    @Mock
    VoucherRepository voucherRepository;
    @Mock
    AlunoRepository alunoRepository;
    @Mock
    CursoMapper cursoMapper;
    @Mock
    VoucherMapper voucherMapper;
    @Mock
    MatriculaMapper matriculaMapper;

    @InjectMocks
    RecompensasService recompensasService;

    private Aluno buildAlunoBasico(){
        return Aluno.builder()
                .id(UUID.randomUUID())
                .name("Aluno Teste")
                .email("aluno@teste.com")
                .password("123456")
                .ra(new RA("2026123"))
                .plano(Plano.BASICO)
                .moedas(0)
                .build();
    }

    private void stubCriacaoDeMatriculas(Aluno aluno){
        when(this.cursoMapper.buildEntity(anyString(), anyDouble()))
                .thenAnswer(inv -> Curso.builder()
                        .id(UUID.randomUUID())
                        .name(inv.getArgument(0, String.class))
                        .status(CursoStatus.INICIADO)
                        .notaFinal(0.0)
                        .build());
        when(this.matriculaMapper.toEntity(eq(aluno), any(Curso.class)))
                .thenAnswer(inv -> Matricula.builder()
                        .id(UUID.randomUUID())
                        .aluno(inv.getArgument(0, Aluno.class))
                        .curso(inv.getArgument(1, Curso.class))
                        .build());
        when(this.matriculaRepository.save(any(Matricula.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    /**
     * TDD1 - GREEN (passo "Entao" do cenario):
     *
     * Entao o sistema deve liberar o acesso a 3 novos cursos
     * E manter a assinatura no plano basico
     *
     * Verifica: 3 matriculas criadas com status INICIADO, nenhum voucher emitido
     * e nenhuma alteracao de plano/moedas no aluno.
     */
    @Test
    void liberarCursosBonusDeveCriarTresMatriculasIniciadasSemVoucher(){
        var aluno = this.buildAlunoBasico();
        this.stubCriacaoDeMatriculas(aluno);

        var recompensa = this.recompensasService.liberarCursosBonus(aluno);

        assertEquals(3, recompensa.cursosLiberados().size());
        recompensa.cursosLiberados()
                .forEach(matricula -> assertEquals(CursoStatus.INICIADO, matricula.getCurso().getStatus()));
        assertNull(recompensa.voucher());
        verify(this.voucherRepository, never()).save(any());
        verify(this.alunoRepository, never()).save(any());
    }

    /**
     * TDD2 - GREEN (passo "Entao" do cenario):
     *
     * Entao a assinatura deve ser alterada para "Premium"
     * E conceder 3 cursos, 3 moedas e voucher para projetos reais
     *
     * Verifica: plano PREMIUM aplicado e persistido, 3 moedas creditadas,
     * voucher VALIDO emitido e 3 matriculas de cursos bonus criadas.
     */
    @Test
    void concederRecompensasPremiumDeveAplicarPlanoMoedasVoucherECursos(){
        var aluno = this.buildAlunoBasico();
        this.stubCriacaoDeMatriculas(aluno);
        var voucher = Voucher.builder()
                .id(UUID.randomUUID())
                .nome("Voucher Projetos Reais")
                .valor(new VoucherValue(100.0))
                .descricao("Voucher para projetos reais")
                .aluno(aluno)
                .status(VoucherStatus.VALIDO)
                .build();
        when(this.voucherMapper.buildEntity(anyString(), anyDouble(), anyString(), eq(aluno))).thenReturn(voucher);
        when(this.voucherRepository.save(voucher)).thenReturn(voucher);

        var recompensa = this.recompensasService.concederRecompensasPremium(aluno);

        assertEquals(Plano.PREMIUM, aluno.getPlano());
        assertEquals(3, aluno.getMoedas());
        assertEquals(3, recompensa.cursosLiberados().size());
        assertSame(voucher, recompensa.voucher());
        verify(this.alunoRepository).save(aluno);
        verify(this.voucherRepository).save(voucher);
    }

    /**
     * TDD5 - GREEN (recorrencia do BDD2, "a cada 12 cursos concluidos"):
     *
     *   Dado um aluno Premium que fechou um novo ciclo de 12 concluidos
     *   Quando a recompensa recorrente e concedida
     *   Entao 3 novas moedas sao creditadas
     *   E um novo voucher para projetos reais e emitido
     *   E nem o plano nem os cursos bonus sao alterados
     */
    @Test
    void concederRecompensasRecorrentesDeveCreditarMoedasEEmitirVoucherSemCursosBonus(){
        var aluno = this.buildAlunoBasico();
        aluno.setPlano(Plano.PREMIUM);
        aluno.setMoedas(3);
        var voucher = Voucher.builder()
                .id(UUID.randomUUID())
                .nome("Voucher Projetos Reais")
                .valor(new VoucherValue(100.0))
                .descricao("Voucher para participacao em projetos reais, concedido a cada 12 cursos concluidos")
                .aluno(aluno)
                .status(VoucherStatus.VALIDO)
                .build();
        when(this.voucherMapper.buildEntity(anyString(), anyDouble(), anyString(), eq(aluno))).thenReturn(voucher);
        when(this.voucherRepository.save(voucher)).thenReturn(voucher);

        var recompensa = this.recompensasService.concederRecompensasRecorrentes(aluno);

        assertEquals(6, aluno.getMoedas(), "Recompensa recorrente credita mais 3 moedas");
        assertEquals(Plano.PREMIUM, aluno.getPlano(), "Plano permanece Premium (sem novo upgrade)");
        assertEquals(List.of(), recompensa.cursosLiberados(), "Recorrencia nao libera cursos bonus");
        assertSame(voucher, recompensa.voucher());
        assertEquals(3, recompensa.moedasConcedidas());
        verify(this.alunoRepository).save(aluno);
        verify(this.voucherRepository).save(voucher);
        verify(this.matriculaRepository, never()).save(any());
    }

}
