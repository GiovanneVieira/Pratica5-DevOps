package com.example.gamificacao.grupo_7.model;

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
    public void adicionarMoedaParaAlunoNaoPremiumDeveDarThrowEmPlanoInvalidoException(){

        Aluno aluno = alunoMapper.buildEntity("aluno-teste");
        assertThrows(PlanoInvalidoException.class, () -> {
            aluno.adicionaMoedas(3);
        });
    }

    @Test
    public void moedaInvalidaDeveDarThrowEmMoedaInvalidaException(){
        Aluno aluno = alunoMapper.buildEntity("aluno-teste");
        aluno.setPlano(Plano.PREMIUM);

        assertThrows(MoedaInvalidaException.class, () -> {
            aluno.adicionaMoedas(-3);
        });
    }

    @Test
    public void ganharVoucherNullDeveDarThrowEmIllegalArgumentException(){
        Aluno aluno = alunoMapper.buildEntity("aluno-teste");
        assertThrows(IllegalArgumentException.class, () -> {
            aluno.ganhaVoucher(null);
        });
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
        assertThrows(PlanoInvalidoException.class, () -> {
            aluno.ganhaVoucher(voucher);
        });
    }

    @Test
    public void virarPremiumTendoMenosDe12CursosCompletosDeveDarThrowEmCursosConcluidosInsuficientesException(){

        Aluno aluno = alunoMapper.buildEntity("aluno-teste");
        aluno.adicionaCursos(criaCursos(11));
        aluno.getCursos().forEach(curso -> {
            curso.setNotaFinal(7.5);
            aluno.concluirCurso(curso);
        });

        assertThrows(CursosConcluidosInsuficientesException.class, aluno::virarPremium);

    }

    @Test
    public void tentarConcluirUmCursoNullDeveDarThrowEmIllegalArgumentException(){
        Aluno aluno = alunoMapper.buildEntity("aluno-teste");
        assertThrows(IllegalArgumentException.class, () -> {
            aluno.concluirCurso(null);
        });
    }

    @Test
    public void alunoPremiumAoTentarVirarPremiumDeveDarThrowEmPlanoInvalidoException(){
        Aluno aluno = alunoMapper.buildEntity("aluno-teste");
        aluno.setPlano(Plano.PREMIUM);

        assertThrows(PlanoInvalidoException.class, aluno::virarPremium);
    }

    /**
     *                              TDD2 RED
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


   /* *
     *                      TDD2 GREEN
     * Logica inical implementada apenas para o teste dar passar, porem extremamente vulneravel e com erros de relation entre entidades,
     *
    **/
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


    /*      Método Auxiliar para criar cursos   */

    private List<Curso> criaCursos(Integer quantidade){
        List<Curso> cursos = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            Curso curso = Curso.builder()
                    .id(UUID.randomUUID())
                    .name("Curso " + (i + 1))
                    .status(CursoStatus.INICIADO)
                    .notaFinal(0.0)
                    .build();
            cursos.add(curso);
        }
        return cursos;
    }

    /**
     *                      TDD2 BLUE
     * Lógica refatorada e implementada para contar os cursos concluidos e liberar o plano premium quando o aluno concluir 12 cursos.
     */
    @Test
    public void alunoComPlanoBasicoAndCursosConcluidosEquals11ConcluirCursoViraPremium(){

        Aluno alunoTest = alunoMapper.buildEntity("aluno-test");
        alunoTest.adicionaCursos(criaCursos(11));
        alunoTest.getCursos().forEach(curso -> {
            curso.setNotaFinal(7.5);
            alunoTest.concluirCurso(curso);
        });

        Curso curso12 = cursoMapper.buildEntity("curso-teste-12", 7.5);
        alunoTest.adicionaCurso(curso12);
        alunoTest.concluirCurso(curso12);
        alunoTest.virarPremium();
        alunoTest.adicionaMoedas(3);

        Voucher voucher = voucherMapper.buildEntity(
                "voucher-teste",
                10.00,
                "teste",
                alunoTest
        );

        alunoTest.ganhaVoucher(voucher);

        List<Curso>cursosGanhos = criaCursos(3);
        alunoTest.adicionaCursos(cursosGanhos);

        assertEquals(Plano.PREMIUM, alunoTest.getPlano());
        assertEquals(12, alunoTest.countCursosByStatus(CursoStatus.CONCLUIDO));
        assertEquals(15, alunoTest.getCursos().size());
        assertEquals(3, alunoTest.getMoedas());
        assertEquals(3, alunoTest.countCursosByStatus(CursoStatus.INICIADO));
        assertTrue(alunoTest.countVouchers() > 0);
    }



    /** TDD3 GREEN **/
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

    /* TDD3 BLUE */
    @Test
    public void alunoComNotaIgualASeteNaoLiberaCursoAdicionalNemIncrementaPremium() {

        Aluno aluno = alunoMapper.buildEntity("Aluno Teste");

        Curso curso = cursoMapper.buildEntity("Curso Teste", 7.0);
        aluno.adicionaCurso(curso);
        aluno.concluirCurso(curso);
        assertEquals(CursoStatus.REPROVADO, curso.getStatus());
        assertEquals(0, aluno.countCursosByStatus(CursoStatus.CONCLUIDO));

    }

}
