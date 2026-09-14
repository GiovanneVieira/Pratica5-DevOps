package com.example.gamificacao.grupo_7.model;

import com.example.gamificacao.grupo_7.enums.CursoStatus;
import com.example.gamificacao.grupo_7.enums.Plano;
import com.example.gamificacao.grupo_7.exception.aluno.CursosConcluidosInsuficientesException;
import com.example.gamificacao.grupo_7.exception.aluno.NotaFinalNaoSuficienteException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Aluno {

    private UUID id;

    private String name;

    private Plano plano;

    private Integer moedas;

    private List<Voucher> vouchers;

    private List<Curso> cursos;

    private int cursosConcluidos;


    public void ganhaVoucher(Voucher voucher){
        this.vouchers.add(voucher);
    }

    public void adicionaCurso(Curso curso){
        this.cursos.add(curso);
    }

    public void adicionaCursos(List<Curso> cursos){
        this.cursos.addAll(cursos);
    }

    /*
                                    TTD2 - GREEN
        Método inicial para alterar o plano do aluno para Premium.
    */
    /* public void virarPremium(){
        if(this.getPlano() == Plano.PREMIUM){
            throw new IllegalStateException("O aluno já é premium");
        }
        this.setPlano(Plano.PREMIUM);
    }*/

    /*
        Metodo BLUE para contar os cursos concluidos
        Utilizando Stream API para filtrar os cursos com status CONCLUIDO e contar a quantidade.
    */
    public long getCursosConcluidos(){
        return this.cursos
                .stream()
                .filter(curso -> curso.getStatus() == CursoStatus.CONCLUIDO)
                .count();
    }

    /*
        Método BLUE para alterar o plano do aluno para Premium,
        verificando se ele possui cursos concluidos suficientes
        e utilizando boas praticas como early return
    */
    public void virarPremium(){
        if(this.getCursosConcluidos() < 12){
            throw new CursosConcluidosInsuficientesException("O aluno não possui cursos concluidos suficientes para virar premium");
        }
        this.setPlano(Plano.PREMIUM);
    }

     /**
     *                      TDD3 - GREEN
     * Conclui um curso. Para a regra atual da história, uma nota final
     * igual ou inferior a 7,0 não libera curso adicional nem incrementa
     * o progresso para o plano Premium.
     */
    /*public void concluirCurso(Curso curso, double notaFinal) {
        if (curso == null) {
            throw new IllegalArgumentException("O curso não pode ser nulo");
        }

        curso.setStatus(CursoStatus.CONCLUIDO);
        cursosConcluidos++;

        if (notaFinal <= 7.0) {
            return;
        }
    }*/

    /**
     *                      TDD3 - BLUE
     * Conclui um curso. Para a regra atual da história, uma nota final
     * igual ou inferior a 7,0 não libera curso adicional nem incrementa
     * o progresso para o plano Premium.
     * Incluindo tratamento de erros para se caso a nota final seja menor ou igual a 7,0.
     */
    public void concluirCurso(Curso curso, double notaFinal) {
        if (curso == null) {
            throw new IllegalArgumentException("O curso não pode ser nulo");
        }

        if (notaFinal <= 7.0) {
            throw new NotaFinalNaoSuficienteException("A nota final do curso deve ser maior que 7,0 para liberar curso adicional e incrementar progresso para o plano Premium");
        }

        curso.conclui();
        cursosConcluidos++;

    }


    public void adicionaMoedas(Integer moedas){
        this.moedas += moedas;
    }
}
