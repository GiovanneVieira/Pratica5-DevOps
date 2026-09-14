package com.example.gamificacao.grupo_7.model;

import com.example.gamificacao.grupo_7.enums.Plano;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    private List<Voucher> vouchers = new ArrayList<>();
    private List<Curso> cursos = new ArrayList<>();

    @Builder.Default
    private boolean assinaturaBasicaAtiva = false;

    @Builder.Default
    private int cursosAdicionaisLiberados = 0;

    @Builder.Default
    private int progressoPremium = 0;

    @Builder.Default
    private int cursosConcluidos = 0;

    public Aluno(String name){
        this.id = UUID.randomUUID();
        this.name = name;
        this.plano = Plano.BASICO;
        this.assinaturaBasicaAtiva = true;
        this.moedas = 0;
        this.vouchers = new ArrayList<>();
        this.cursos = new ArrayList<>();
    }

    public void ganhaVoucher(Voucher voucher){
        this.vouchers.add(voucher);
    }

    public void adicionaCurso(Curso curso){
        this.cursos.add(curso);
    }

    /**
     * Conclui um curso. Para a regra atual da história, uma nota final
     * igual ou inferior a 7,0 não libera curso adicional nem incrementa
     * o progresso para o plano Premium.
     */
    public void concluirCurso(Curso curso, double notaFinal) {
        if (curso == null) {
            throw new IllegalArgumentException("O curso não pode ser nulo");
        }

        curso.setStatus(com.example.gamificacao.grupo_7.enums.CursoStatus.CONCLUIDO);
        cursosConcluidos++;

        if (notaFinal <= 7.0) {
            return;
        }
    }

    public void concluiCurso(UUID cursoId){
        if (cursoId == null) {
            throw new IllegalArgumentException("O curso não pode ser nulo");
        }

        for (Curso curso : cursos) {
            if (cursoId.equals(curso.getId())) {
                concluirCurso(curso, 7.0);
                return;
            }
        }
    }

    public void adicionaMoedas(Integer moedas){
        this.moedas += moedas;
    }
}
