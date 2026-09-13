package com.example.gamificacao.grupo_7.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AlunoTest {
//Teste RED antes da implementação
    @Test
    public void deveNaoLiberarCursoAdicionalQuandoNotaFinalForMenorOuIgualA7() {
        Aluno aluno = new Aluno();
        aluno.setName("Aluno Teste");

        Curso curso = new Curso();
        curso.setAluno(aluno);

        // regra que ainda não existe
        aluno.concluirCurso(curso, 7.0);

        assertEquals(0, aluno.getCursosAdicionaisLiberados());
        assertEquals(0, aluno.getProgressoPremium());
    }
}