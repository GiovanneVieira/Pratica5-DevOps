package com.example.gamificacao.grupo_7.model;

import com.example.gamificacao.grupo_7.enums.CursoStatus;
import com.example.gamificacao.grupo_7.enums.Plano;
import com.example.gamificacao.grupo_7.exception.aluno.NotaFinalNaoSuficienteException;
import com.example.gamificacao.grupo_7.mapper.AlunoMapper;
import com.example.gamificacao.grupo_7.mapper.CursoMapper;
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

    private static Aluno alunoCom11CursosConcluidos;
    private final AlunoMapper alunoMapper;
    private final CursoMapper cursoMapper;

    public AlunoTest(){
        this.alunoMapper = new AlunoMapper();
        this.cursoMapper = new CursoMapper();
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

        assertEquals(0, aluno1.getCursosConcluidos());
    }

    /**
     *                              TDD2 RED
     *
    @Test
    public void alunoComPlanoBasicoAndCursosConcluidosEquals11ConcluirCursoViraPremiumRed(){

        Aluno alunoTest = alunoMapper.buildEntity("aluno-test");

        Curso curso = cursoMapper.buildEntity("curso-teste", alunoTest, 7.5);

        alunoTest.setCursosConcluidos(11);

        alunoTest.concluirCurso(curso, 7.5);

        assertEquals(Plano.PREMIUM, alunoTest.getPlano());

    }
    **/

    /**
     *                      TDD2 GREEN
     * Logica inical implementada apenas para o teste dar passar, porem extremamente vulneravel,
     *
    **/
    /*
    @Test
    public void alunoComPlanoBasicoAndCursosConcluidosEquals11ConcluirCursoViraPremiumGreen(){

        Aluno alunoTest = alunoMapper.buildEntity("aluno-test");

        Curso curso = cursoMapper.buildEntity("curso-teste", alunoTest, 7.5);

        alunoTest.setCursosConcluidos(11);

        alunoTest.concluirCurso(curso, 7.5);

        alunoTest.virarPremium();

        assertEquals(Plano.PREMIUM, alunoTest.getPlano());

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
        alunoTest.getCursos().forEach(curso -> alunoTest.concluirCurso(curso, 7.5));

        Curso curso12 = cursoMapper.buildEntity("curso-teste-12", 7.5);
        alunoTest.adicionaCurso(curso12);
        alunoTest.concluirCurso(curso12, 7.5);
        alunoTest.virarPremium();

        assertEquals(Plano.PREMIUM, alunoTest.getPlano());
        assertEquals(12, alunoTest.getCursosConcluidos());
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

    /*TDD3 BLUE*/
    @Test
    public void alunoComNotaIgualASeteNaoLiberaCursoAdicionalNemIncrementaPremium() {

        Aluno aluno = alunoMapper.buildEntity("Aluno Teste");

        Curso curso = cursoMapper.buildEntity("Curso Teste", 0.0);

        assertThrows(NotaFinalNaoSuficienteException.class, () -> {
            aluno.concluirCurso(curso, 7.0);
        });

        assertEquals(CursoStatus.INICIADO, curso.getStatus());
        assertEquals(0, aluno.getCursosConcluidos());

    }

}
