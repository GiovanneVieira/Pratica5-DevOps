package com.example.gamificacao.grupo_7.enums;

import lombok.Getter;

@Getter
public enum CursoStatus {
    INICIADO("iniciado"),
    REPROVADO("reprovado"),
    CONCLUIDO("concluído");

    private String status;
    CursoStatus(String status){
        this.status = status;
    }

}
