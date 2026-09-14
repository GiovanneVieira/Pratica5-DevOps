package com.example.gamificacao.grupo_7.model;

import com.example.gamificacao.grupo_7.enums.Plano;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AlunoTest {

    @Test
    public void deveCriarAluno(){

        Aluno aluno1 = new Aluno("aluno-teste");

        assertThat(aluno1.getClass()).hasSameClassAs(Aluno.class);

    }

    @Test
    public void novoAlunoDeveTerPlanoPadrao(){
        Aluno aluno1 = new Aluno("aluno-teste");

        assertThat(aluno1.getPlano()).isEqualTo(Plano.BASICO);
    }

    @Test
    public void novoAlunoDeveTerCursosConcluidosZerados(){
        Aluno aluno1 = new Aluno("aluno-teste");

        assertThat(aluno1.getCursosConcluidos()).isEqualTo(0);
    }

}
