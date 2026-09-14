package com.example.gamificacao.grupo_7.model;

import com.example.gamificacao.grupo_7.enums.CursoStatus;
import com.example.gamificacao.grupo_7.enums.Plano;
import com.example.gamificacao.grupo_7.exception.aluno.CursosConcluidosInsuficientesException;
import com.example.gamificacao.grupo_7.exception.aluno.PlanoInvalidoException;
import com.example.gamificacao.grupo_7.exception.moeda.MoedaInvalidaException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;


/*
        Classe Entidade Alvo principal para estudo da metodologia de desenvolvimento ATDD, extremamente sobrecarregada com regras de negocios,
    O que nao é recomendado. De acordo com o padrão MVC, Clean Code e Clean Architecture,
    a camada de dominio deve ser o mais enxuta possivel, com regras de negocio simples e claras,
    e a camada de serviço deve ser responsavel por orquestrar as regras de negocio e aplicar as regras de negocio complexas.
    Apenas foi feito desta maneira para facilitar a visualizacao dos testes unitarios nesta etapa de desenvolvimento
    Futuramente o Refac certamente acontecerá.
 */
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

//    Atributo para TDD3 Green
//    private int cursosConcluidos;

    /* Método Green */
    /*public void ganhaVoucher(Voucher voucher){
        this.vouchers.add(voucher);
    }*/

    /* Método Green */
    /*public void adicionaMoedas(Integer moedas){
        this.moedas += moedas;
    }*/


    /*
                                            Método BLUE
       Adiciona moedas ao aluno, verificando se o aluno é premium e se a quantidade de moedas não é negativa.
       Utiliza boas praticas como early return/throw e possui tratamento de excecoes
    */
    public void adicionaMoedas(Integer moedas){

        if(moedas < 0){
            throw new MoedaInvalidaException("Não é possível adicionar moedas negativas");
        }

        if(this.getPlano() != Plano.PREMIUM){
            throw new PlanoInvalidoException("O aluno precisa ser premium para ganhar moedas");
        }

        this.moedas += moedas;
    }

    /*
                                            Método BLUE
       Adiciona um voucher ao aluno, verificando se o aluno é premium e se o voucher não é nulo.
       Utiliza boas praticas como early return/throw e possui tratamento de excecoes
    */
    public void ganhaVoucher(Voucher voucher){

        if(voucher == null){
            throw new IllegalArgumentException("O voucher não pode ser nulo");
        }

        if(this.getPlano() != Plano.PREMIUM){
            throw new PlanoInvalidoException("O aluno precisa ser premium para ganhar vouchers");
        }

        this.vouchers.add(voucher);
    }

    public long countVouchers(){
        return vouchers.size();
    }

    /* Método Blue */
    public void adicionaCurso(Curso curso){
        this.cursos.add(curso);
    }
    /* Método Blue */
    public void adicionaCursos(List<Curso> cursos){
        this.cursos.addAll(cursos);
    }

    /*
                                    TDD2 - GREEN
        Método inicial para alterar o plano do aluno para Premium, vulnervael devido a falta de verificacao das regras de negocio.
    */
     /*public void virarPremium(){
        if(this.getPlano() == Plano.PREMIUM){
            return;
        }
        this.setPlano(Plano.PREMIUM);
    }*/

    /*
        Metodos BLUE para contar os cursos concluidos ou Iniciados
        Utilizando Stream API para filtrar os cursos com status CONCLUIDO e contar a quantidade.
    */
    public long countCursosByStatus(CursoStatus status){
        return this.cursos
                .stream()
                .filter(curso -> curso.getStatus() == status)
                .count();
    }
    /*
        TDD2 - BLUE
        Método BLUE para alterar o plano do aluno para Premium,
        verificando se ele possui cursos concluidos suficientes
        e utilizando boas praticas como early return
    */
    public void virarPremium(){

        if(this.getPlano() == Plano.PREMIUM){
            throw new PlanoInvalidoException("O aluno ja é premium");
        }

        if(this.countCursosByStatus(CursoStatus.CONCLUIDO) < 12){
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
    public void concluirCurso(Curso curso) {
        if (curso == null) {
            throw new IllegalArgumentException("O curso não pode ser nulo");
        }

        curso.conclui();
    }

}
