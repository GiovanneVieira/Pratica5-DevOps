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
    private List<Curso> cursosConcluidos = new ArrayList<>();
    private Integer moedas;
    private List<Voucher> vouchers = new ArrayList<>();
    private List<Curso> cursosEmAndamento = new ArrayList<>();

    public Aluno(String name){
        this.id = UUID.randomUUID();
        this.name = name;
        this.plano = Plano.BASICO;
    }

    public void ganhaVoucher(Voucher voucher){
        this.vouchers.add(voucher);
    }

    public void adicionaCurso(Curso curso){
        this.cursosEmAndamento.add(curso);
    }

    public void concluiCurso(UUID cursoId){

    }

    public void adicionaMoedas(Integer moedas){
        this.moedas += moedas;
    }



}
