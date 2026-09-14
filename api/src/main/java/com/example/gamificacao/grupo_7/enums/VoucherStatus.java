package com.example.gamificacao.grupo_7.enums;

import lombok.Getter;

@Getter
public enum VoucherStatus {
    VALIDO("valido"),
    UTILIZADO("utilizado"),
    EXPIRADO("expirado");

    private final String status;

    VoucherStatus(String status){
        this.status = status;
    }

}
