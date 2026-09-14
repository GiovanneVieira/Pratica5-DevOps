package com.example.gamificacao.grupo_7.enums;

import lombok.Getter;

@Getter
public enum Plano {
    PREMIUM("premium"),
    BASICO("plano");

    private String plano;

    Plano(String plano){
        this.plano = plano;
    }

}
