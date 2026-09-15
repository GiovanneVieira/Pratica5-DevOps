package com.example.gamificacao.grupo_7.model;

import com.example.gamificacao.grupo_7.enums.Plano;
import com.example.gamificacao.grupo_7.model.validation_object.RA;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Entity(name = "aluno")
@Table(name = "alunos")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Aluno {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "ra", nullable = false)
    @Embedded
    private RA ra;

    @Enumerated(EnumType.STRING)
    @Column(name = "plano", nullable = false)
    private Plano plano;

    @Column(name = "moedas")
    private Integer moedas;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

//    // Atributo mantido apenas para os testes realizados na primeira entrega da Atividade.
//    private List<Curso> cursos = new ArrayList<>();
//    private CursosParametros cursosParametros;

    // =========================================================================
    // MÉTODOS BÁSICOS DE DOMÍNIO / AUXILIARES
    // =========================================================================

    /*public void adicionaCurso(Curso curso){
        if(curso == null){
            throw new IllegalArgumentException("O curso não pode ser nulo");
        }
        garantirListaCursosInicializada();
        this.cursos.add(curso);
    }

    public void adicionaCursos(List<Curso> cursos){
        if(cursos == null || cursos.isEmpty()){
            throw new IllegalArgumentException("A lista de cursos não pode ser nula ou vazia");
        }
        garantirListaCursosInicializada();
        this.cursos.addAll(cursos);
    }

    public long countCursosByStatus(CursoStatus status){
        if(this.cursos == null){
            return 0;
        }
        return this.cursos
                .stream()
                .filter(curso -> curso.getStatus() == status)
                .count();
    }

    public long countVouchers(){
        return this.vouchers != null ? this.vouchers.size() : 0;
    }

    private void garantirListaCursosInicializada() {
        if (this.cursos == null) {
            this.cursos = new ArrayList<>();
        }
    }*/

    // =========================================================================
    // TDD1: Liberação de 3 novos cursos ao concluir curso com nota > 7.0
    // (Cenário: Aluno básico com menos de 11 cursos concluídos)
    // =========================================================================

    /**
     * TDD1 - BLUE (REFACTOR)
     * Conclui um curso e aplica as regras de progressão:
     * Quando um curso for concluído com nota superior a 7.0 por um aluno com plano BÁSICO
     * e menos de 11 cursos concluídos:
     * - Libera o acesso a 3 novos cursos com status INICIADO.
     * - Mantém o plano como BÁSICO.
     */
    /*public void concluirCurso(Curso curso) {
        if (curso == null) {
            throw new IllegalArgumentException("O curso não pode ser nulo");
        }

        if (curso.getStatus() == CursoStatus.CONCLUIDO){
            throw new IllegalArgumentException("O curso já foi concluído");
        }

        garantirListaCursosInicializada();
        long concluidosAntes = this.countCursosByStatus(CursoStatus.CONCLUIDO);

        curso.conclui();

        if (deveLiberarNovosCursos(curso, concluidosAntes)) {
            liberarNovosCursos(this.cursosParametros.getValor());
        }
    }

    private boolean deveLiberarNovosCursos(Curso curso, long concluidosAntes) {
        return curso.getStatus() == CursoStatus.CONCLUIDO
                && this.plano == Plano.BASICO
                && concluidosAntes < this.cursosParametros.getValor();
    }

    private void liberarNovosCursos(int quantidade) {
        garantirListaCursosInicializada();
        for (int i = 0; i < quantidade; i++) {
            Curso novoCursoLiberado = Curso.builder()
                    .id(UUID.randomUUID())
                    .name("Curso Liberado " + (i + 1))
                    .status(CursoStatus.INICIADO)
                    .notaFinal(0.0)
                    .build();
            this.cursos.add(novoCursoLiberado);
        }
    }*/

    // =========================================================================
    // TDD2: Upgrade para Plano Premium e Concessão de Recompensas
    // (Virar premium após 12 cursos concluídos, receber moedas, vouchers e bônus)
    // =========================================================================

    /* Método Green */
    /*public void ganhaVoucher(Voucher voucher){
        this.vouchers.add(voucher);
    }*/

    /* Método Green */
    /*public void adicionaMoedas(Integer moedas){
        this.moedas += moedas;
    }*/

    /*
     * TDD2 - GREEN
     * Método inicial para alterar o plano do aluno para Premium, vulnerável devido à falta de verificação das regras de negócio.
     */
    /*public void virarPremium(){
        if(this.getPlano() == Plano.PREMIUM){
            return;
        }
        this.setPlano(Plano.PREMIUM);
    }*/

    /**
     * TDD2 - BLUE
     * Método BLUE para alterar o plano do aluno para Premium,
     * verificando se ele possui cursos concluídos suficientes
     * e utilizando boas práticas como early return.
     */
    /*public void virarPremium(){
        if(this.getPlano() == Plano.PREMIUM){
            throw new PlanoInvalidoException("O aluno ja é premium");
        }

        if(this.countCursosByStatus(CursoStatus.CONCLUIDO) < this.cursosParametros.getValor()){
            throw new CursosConcluidosInsuficientesException("O aluno não possui cursos concluidos suficientes para virar premium");
        }

        this.setPlano(Plano.PREMIUM);
    }
*/
    /**
     * TDD2 - BLUE
     * Adiciona moedas ao aluno, verificando se o aluno é premium e se a quantidade de moedas não é negativa.
     */
    /*public void adicionaMoedas(Integer moedas){
        if(moedas < 0){
            throw new MoedaInvalidaException("Não é possível adicionar moedas negativas");
        }

        if(this.getPlano() != Plano.PREMIUM){
            throw new PlanoInvalidoException("O aluno precisa ser premium para ganhar moedas");
        }

        this.moedas += moedas;
    }*/

    /**
     * TDD2 - BLUE
     * Adiciona um voucher ao aluno, verificando se o aluno é premium e se o voucher não é nulo.
     */
    /*public void ganhaVoucher(Voucher voucher){
        if(voucher == null){
            throw new IllegalArgumentException("O voucher não pode ser nulo");
        }

        if(this.getPlano() != Plano.PREMIUM){
            throw new PlanoInvalidoException("O aluno precisa ser premium para ganhar vouchers");
        }

        this.vouchers.add(voucher);
    }*/

    /**
     * TDD2 - BLUE
     * Entrega o pacote de recompensas premium através do RecompensasPremiumDTO validado.
     */
    /*public void receberRecompensasDePremium(@Valid RecompensasPremiumDTO recompensasPremiumDTO){
        if(recompensasPremiumDTO == null){
            throw new IllegalArgumentException("As recompensas não podem ser nulas");
        }

        if(this.getPlano() == Plano.BASICO){
            throw new PlanoInvalidoException("O aluno precisa ser premium para receber recompensas");
        }

        if(recompensasPremiumDTO.voucher() == null){
            throw new IllegalArgumentException("O voucher não pode ser nulo");
        }

        if(recompensasPremiumDTO.cursos() == null || recompensasPremiumDTO.cursos().isEmpty()){
            throw new IllegalArgumentException("A lista de cursos não pode ser nula ou vazia");
        }

        if(recompensasPremiumDTO.moedas() == null || recompensasPremiumDTO.moedas() < 0){
            throw new MoedaInvalidaException("Não é possível adicionar moedas negativas");
        }

        this.adicionaCursos(recompensasPremiumDTO.cursos());
        this.ganhaVoucher(recompensasPremiumDTO.voucher());
        this.adicionaMoedas(recompensasPremiumDTO.moedas());
    }*/

    // =========================================================================
    // TDD3: Regra de Nota <= 7.0 não libera cursos bônus nem avança status concluído
    // =========================================================================

    /**
     * TDD3 - GREEN
     * Conclui um curso. Para a regra da história, uma nota final
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
     * TDD3 - BLUE
     * A validação de nota <= 7.0 reprova o curso (curso.conclui()) e
     * consequentemente não satisfaz deveLiberarNovosCursos, integrando
     * com sucesso o fluxo seguro de conclusão.
     */


}

/*
        Classe Entidade Alvo principal para estudo da metodologia de desenvolvimento ATDD, extremamente sobrecarregada com regras de negocios,
    O que nao é recomendado. De acordo com o padrão MVC, Clean Code e Clean Architecture,
    a camada de dominio deve ser o mais enxuta possivel, com regras de negocio simples e claras,
    e a camada de serviço deve ser responsavel por orquestrar as regras de negocio e aplicar as regras de negocio complexas.
    Apenas foi feito desta maneira para facilitar a visualizacao dos testes unitarios nesta etapa de desenvolvimento
    Futuramente o Refac certamente acontecerá.
 */
