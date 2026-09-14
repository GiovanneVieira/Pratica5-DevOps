package com.example.gamificacao.grupo_7.model;

import com.example.gamificacao.grupo_7.enums.CursoStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AlunoTest {

    @Test
    public void deveCriarAluno(){

        Aluno aluno1 = new Aluno("aluno-teste");

        assertTrue(aluno1.getClass().equals(Aluno.class));
    }

    @Test
    public void novoAlunoDeveTerPlanoPadrao(){

        Aluno aluno1 = new Aluno("aluno-teste");

        assertEquals(com.example.gamificacao.grupo_7.enums.Plano.BASICO, aluno1.getPlano());
    }

    @Test
    public void novoAlunoDeveTerCursosConcluidosZerados(){

        Aluno aluno1 = new Aluno("aluno-teste");

        assertEquals(0, aluno1.getCursosConcluidos());
    }

    @Test
    public void alunoComNotaIgualASeteNaoLiberaCursoAdicionalNemIncrementaPremium() {
        Aluno aluno = Aluno.builder()
                .name("Aluno Teste")
                .assinaturaBasicaAtiva(true)
                .cursosAdicionaisLiberados(0)
                .progressoPremium(0)
                .build();

        Curso curso = Curso.builder()
                .aluno(aluno)
                .status(CursoStatus.EM_ANDAMENTO)
                .build();

        aluno.concluirCurso(curso, 7.0);

        assertTrue(aluno.isAssinaturaBasicaAtiva());
        assertEquals(CursoStatus.CONCLUIDO, curso.getStatus());
        assertEquals(0, aluno.getCursosAdicionaisLiberados());
        assertEquals(0, aluno.getProgressoPremium());
    }
}
