package com.example.gamificacao.grupo_7.enums;

import lombok.Getter;

@Getter
public enum CursosParametros {
    QUANTIDADE_CURSOS_BONUS(3),
    LIMITE_CURSOS_UPGRADE_PREMIUM(11),
    CURSOS_NECESSARIOS_PREMIUM(12);

    private Integer valor;

    CursosParametros(Integer valor){
        this.valor = valor;
    }

}
