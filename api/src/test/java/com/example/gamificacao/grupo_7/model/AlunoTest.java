package com.example.gamificacao.grupo_7.model;

import com.example.gamificacao.grupo_7.dto.RecompensasPremiumDTO;
import com.example.gamificacao.grupo_7.enums.CursoStatus;
import com.example.gamificacao.grupo_7.enums.Plano;
import com.example.gamificacao.grupo_7.exception.aluno.CursosConcluidosInsuficientesException;
import com.example.gamificacao.grupo_7.exception.aluno.PlanoInvalidoException;
import com.example.gamificacao.grupo_7.exception.moeda.MoedaInvalidaException;
import com.example.gamificacao.grupo_7.mapper.AlunoMapper;
import com.example.gamificacao.grupo_7.mapper.CursoMapper;
import com.example.gamificacao.grupo_7.mapper.VoucherMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@Slf4j
@ExtendWith(SpringExtension.class)
public class AlunoTest {

    private final AlunoMapper alunoMapper;
    private final CursoMapper cursoMapper;
    private final VoucherMapper voucherMapper;

    public AlunoTest(){
        this.alunoMapper = new AlunoMapper();
        this.cursoMapper = new CursoMapper();
        this.voucherMapper = new VoucherMapper();
    }

    @BeforeAll
    public static void setup() {
        log.info("Iniciando testes de Aluno");
    }


    // =========================================================================
    // TESTES BÁSICOS DE ENTIDADE / INTEGRIDADE
    // =========================================================================

    @Test
    public void deveCriarAluno(){
        Aluno aluno1 = alunoMapper.buildEntity("aluno-teste");
        assertInstanceOf(Aluno.class, aluno1);
    }

    @Test
    public void novoAlunoDeveTerPlanoPadrao(){
        Aluno aluno1 = alunoMapper.buildEntity("aluno-teste");
        assertEquals(Plano.BASICO, aluno1.getPlano());
    }

    @Test
    public void novoAlunoDeveTerCursosConcluidosZerados(){
        Aluno aluno1 = alunoMapper.buildEntity("aluno-teste");
        assertEquals(0, aluno1.countCursosByStatus(CursoStatus.CONCLUIDO));
    }

    @Test
    public void adicionarCursosVazioOuNullDeveDarThrowEmIllegalArgumentException(){
        Aluno aluno = alunoMapper.buildEntity("aluno-teste");
        assertThrows(IllegalArgumentException.class, () -> aluno.adicionaCurso(null));
        assertThrows(IllegalArgumentException.class, () -> aluno.adicionaCursos(new ArrayList<>()));
    }

    // =========================================================================
    // TDD1: Liberação de 3 novos cursos ao concluir curso com nota > 7.0
    // =========================================================================

    @Test
    public void tentarConcluirUmCursoNullDeveDarThrowEmIllegalArgumentException(){
        Aluno aluno = alunoMapper.buildEntity("aluno-teste");
        assertThrows(IllegalArgumentException.class, () -> aluno.concluirCurso(null));
    }

    @Test
    public void concluirCursoJaConcluidoDeveDarThrowEmIllegalArgumentException(){
        Aluno aluno = alunoMapper.buildEntity("aluno-teste");
        Curso curso = cursoMapper.buildEntity("curso-teste", 7.5);
        aluno.adicionaCurso(curso);
        aluno.concluirCurso(curso);

        assertThrows(IllegalArgumentException.class, () -> aluno.concluirCurso(curso));
    }

    /**
     * TDD1 - RED / GREEN / BLUE
     * Cenário 1:
     * DADO um aluno com assinatura básica ativa
     * E com menos de 11 cursos concluídos (ex: 5 cursos)
     * QUANDO o aluno conclui um curso
     * E obtém nota final superior a 7.0
     * ENTÃO o sistema deve liberar o acesso a 3 novos cursos
     * E manter a assinatura no plano básico
     */
    @Test
    public void alunoComAssinaturaBasicaAndMenosDe11CursosConcluidosAoConcluirCursoComNotaSuperiorASeteDeveLiberar3NovosCursosEManterPlanoBasico() {
        // DADO um aluno com assinatura básica ativa
        Aluno aluno = alunoMapper.buildEntity("Aluno Teste");

        // E com menos de 11 cursos concluídos (ex: 5 cursos)
        aluno.adicionaCursos(criaCursos(5, CursoStatus.CONCLUIDO, 8.0));

        assertEquals(5, aluno.countCursosByStatus(CursoStatus.CONCLUIDO));
        assertEquals(0, aluno.countCursosByStatus(CursoStatus.INICIADO));

        // QUANDO o aluno conclui um curso
        // E obtém nota final superior a 7.0
        Curso curso = cursoMapper.buildEntity("Curso 6", 8.0);
        aluno.adicionaCurso(curso);
        aluno.concluirCurso(curso);

        // ENTÃO o sistema deve liberar o acesso a 3 novos cursos
        assertEquals(3, aluno.countCursosByStatus(CursoStatus.INICIADO));
        assertEquals(9, aluno.getCursos().size());

        // E manter a assinatura no plano básico
        assertEquals(Plano.BASICO, aluno.getPlano());
    }

    // =========================================================================
    // TDD2: Upgrade para Plano Premium e Concessão de Recompensas
    // =========================================================================

    /**
     * TDD2 RED
     */
    /*@Test
    public void alunoComPlanoBasicoAndCursosConcluidosEquals11ConcluirCursoViraPremiumAndGanhaCursosMoedasAndVoucher(){
        Aluno alunoTest = alunoMapper.buildEntity("aluno-test");
        Curso curso = cursoMapper.buildEntity("curso-teste", 7.5);
        alunoTest.setCursosConcluidos(11);
        alunoTest.concluirCurso(curso, 7.5);

        assertEquals(Plano.PREMIUM, alunoTest.getPlano());
        assertEquals(3, alunoTest.getMoedas());
        assertFalse(alunoTest.getVouchers().isEmpty());
    }*/

    /**
     * TDD2 GREEN
     */
    /*@Test
    public void alunoComPlanoBasicoAndCursosConcluidosEquals11ConcluirCursoViraPremiumAndGanhaCursosMoedasAndVoucher(){
        Aluno alunoTest = alunoMapper.buildEntity("aluno-test");
        Curso curso = cursoMapper.buildEntity("curso-teste", 7.5);
        Voucher voucher = voucherMapper.buildEntity(
                "vouchertest",
                10.00,
                "voucher teste",
                alunoTest,
                curso
        );

        alunoTest.setCursosConcluidos(11);
        alunoTest.concluirCurso(curso, 7.5);
        alunoTest.virarPremium();
        alunoTest.adicionaMoedas(3);
        alunoTest.ganhaVoucher(voucher);

        assertEquals(Plano.PREMIUM, alunoTest.getPlano());
        assertEquals(3, alunoTest.getMoedas());
        assertFalse(alunoTest.getVouchers().isEmpty());
    }*/

    @Test
    public void virarPremiumTendoMenosDe12CursosCompletosDeveDarThrowEmCursosConcluidosInsuficientesException(){
        Aluno aluno = alunoMapper.buildEntity("aluno-teste");
        aluno.adicionaCursos(criaCursos(11, CursoStatus.CONCLUIDO, 7.5));

        assertThrows(CursosConcluidosInsuficientesException.class, aluno::virarPremium);
    }

    @Test
    public void alunoPremiumAoTentarVirarPremiumDeveDarThrowEmPlanoInvalidoException(){
        Aluno aluno = alunoMapper.buildEntity("aluno-teste");
        aluno.setPlano(Plano.PREMIUM);

        assertThrows(PlanoInvalidoException.class, aluno::virarPremium);
    }

    @Test
    public void adicionarMoedaParaAlunoNaoPremiumDeveDarThrowEmPlanoInvalidoException(){
        Aluno aluno = alunoMapper.buildEntity("aluno-teste");
        assertThrows(PlanoInvalidoException.class, () -> aluno.adicionaMoedas(3));
    }

    @Test
    public void moedaInvalidaDeveDarThrowEmMoedaInvalidaException(){
        Aluno aluno = alunoMapper.buildEntity("aluno-teste");
        aluno.setPlano(Plano.PREMIUM);

        assertThrows(MoedaInvalidaException.class, () -> aluno.adicionaMoedas(-3));
    }

    @Test
    public void ganharVoucherNullDeveDarThrowEmIllegalArgumentException(){
        Aluno aluno = alunoMapper.buildEntity("aluno-teste");
        assertThrows(IllegalArgumentException.class, () -> aluno.ganhaVoucher(null));
    }

    @Test
    public void ganharVoucherEmAlunoNaoPremiumDeveDarThrowEmPlanoInvalidoException(){
        Aluno aluno = alunoMapper.buildEntity("aluno-teste");
        Voucher voucher = voucherMapper.buildEntity(
                "voucher-teste",
                10.00,
                "teste",
                aluno
        );
        assertThrows(PlanoInvalidoException.class, () -> aluno.ganhaVoucher(voucher));
    }

    @Test
    public void receberRecompensasDePremiumEmAlunoNaoPremiumDeveDarThrowEmPlanoInvalidoException(){
        Aluno aluno = alunoMapper.buildEntity("aluno-teste");
        Voucher voucher = voucherMapper.buildEntity(
                "voucher-teste",
                10.00,
                "teste",
                aluno
        );
        var recompensas = new RecompensasPremiumDTO(voucher, criaCursos(3), 3);
        assertThrows(PlanoInvalidoException.class, () -> aluno.receberRecompensasDePremium(recompensas));
    }

    @Test
    public void receberRecompensasDePremiumComVoucherNullDeveDarThrowEmIllegalArgumentException(){
        Aluno aluno = alunoMapper.buildEntity("aluno-teste");
        aluno.setPlano(Plano.PREMIUM);

        var recompensas = new RecompensasPremiumDTO(null, criaCursos(3), 3);
        assertThrows(IllegalArgumentException.class, () -> aluno.receberRecompensasDePremium(recompensas));
    }

    @Test
    public void receberRecompensasDePremiumComCursosNullDeveDarThrowEmIllegalArgumentException(){
        Aluno aluno = alunoMapper.buildEntity("aluno-teste");
        aluno.setPlano(Plano.PREMIUM);
        Voucher voucher = voucherMapper.buildEntity(
                "voucher-teste",
                10.00,
                "teste",
                aluno
        );

        var recompensas = new RecompensasPremiumDTO(voucher, null, 3);
        assertThrows(IllegalArgumentException.class, () -> aluno.receberRecompensasDePremium(recompensas));
    }

    @Test
    public void receberRecompensasDePremiumComMoedasNegativasDeveDarThrowEmMoedaInvalidaException(){
        Aluno aluno = alunoMapper.buildEntity("aluno-teste");
        aluno.setPlano(Plano.PREMIUM);
        Voucher voucher = voucherMapper.buildEntity(
                "voucher-teste",
                10.00,
                "teste",
                aluno
        );
        
        var recompensas = new RecompensasPremiumDTO(voucher, criaCursos(3), -3);
        assertThrows(MoedaInvalidaException.class, () -> aluno.receberRecompensasDePremium(recompensas));
    }

    /**
     * TDD2 BLUE
     * Lógica refatorada e implementada para contar os cursos concluídos e liberar o plano premium quando o aluno concluir 12 cursos.
     */
    @Test
    public void alunoComPlanoBasicoAndCursosConcluidosEquals11ConcluirCursoViraPremium(){
        Aluno aluno = alunoMapper.buildEntity("aluno-test");

        aluno.adicionaCursos(criaCursos(11, CursoStatus.CONCLUIDO, 7.5));

        Curso curso12 = cursoMapper.buildEntity("curso-teste-12", 7.5);
        aluno.adicionaCurso(curso12);
        aluno.concluirCurso(curso12);

        aluno.virarPremium();

        Voucher voucherGanho = voucherMapper.buildEntity(
                "voucher-teste",
                10.00,
                "teste",
                aluno
        );

        var recompensas = new RecompensasPremiumDTO(
                voucherGanho,
                criaCursos(3),
                3
        );

        aluno.receberRecompensasDePremium(recompensas);

        assertEquals(Plano.PREMIUM, aluno.getPlano());
        assertEquals(12, aluno.countCursosByStatus(CursoStatus.CONCLUIDO));
        assertEquals(15, aluno.getCursos().size());
        assertEquals(3, aluno.getMoedas());
        assertEquals(3, aluno.countCursosByStatus(CursoStatus.INICIADO));
        assertTrue(aluno.countVouchers() > 0);
    }

    // =========================================================================
    // TDD3: Regra de Nota <= 7.0 não libera cursos bônus nem avança status concluído
    // =========================================================================

    /**
     * TDD3 GREEN
     */
    /*@Test
    public void alunoComNotaIgualASeteNaoLiberaCursoAdicionalNemIncrementaPremium() {
        Aluno aluno = Aluno.builder()
                .name("Aluno Teste")
                .cursosAdicionaisLiberados(0)
                .progressoPremium(0)
                .build();

        Curso curso = Curso.builder()
                .status(CursoStatus.EM_ANDAMENTO)
                .build();

        aluno.concluirCurso(curso, 7.0);

        assertEquals(CursoStatus.CONCLUIDO, curso.getStatus());
        assertEquals(0, aluno.getCursosAdicionaisLiberados());
        assertEquals(0, aluno.getProgressoPremium());
    }*/

    /**
     * TDD3 BLUE
     */
    @Test
    public void alunoComNotaIgualASeteNaoLiberaCursoAdicionalNemIncrementaPremium() {
        Aluno aluno = alunoMapper.buildEntity("Aluno Teste");

        Curso curso = cursoMapper.buildEntity("Curso Teste", 7.0);
        aluno.adicionaCurso(curso);
        aluno.concluirCurso(curso);
        assertEquals(CursoStatus.REPROVADO, curso.getStatus());
        assertEquals(0, aluno.countCursosByStatus(CursoStatus.CONCLUIDO));
    }

    // =========================================================================
    // MÉTODOS AUXILIARES DE TESTE
    // =========================================================================

    private List<Curso> criaCursos(Integer quantidade){
        return criaCursos(quantidade, CursoStatus.INICIADO, 0.0);
    }

    private List<Curso> criaCursos(Integer quantidade, CursoStatus status, Double notaFinal){
        List<Curso> cursos = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            Curso curso = Curso.builder()
                    .id(UUID.randomUUID())
                    .name("Curso " + (i + 1))
                    .status(status)
                    .notaFinal(notaFinal)
                    .build();
            cursos.add(curso);
        }
        return cursos;
    }

}
