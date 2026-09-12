package com.example.gamificacao.grupo_7.enums;

import lombok.Getter;

@Getter
public enum CursoStatus {
    INICIADO("iniciado"),
    EM_ANDAMENTO("em andamento"),
    CONCLUIDO("concluído");

    CursoStatus(String status){
        this.status = status;
    }
    private String status;

}
