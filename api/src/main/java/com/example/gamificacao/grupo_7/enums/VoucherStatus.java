package com.example.gamificacao.grupo_7.enums;

public enum VoucherStatus {
    VALIDO("valido"),
    UTILIZADO("utilizado"),
    EXPIRADO("expirado");

    private String status;

    VoucherStatus(String status){
        this.status = status;
    }

}
