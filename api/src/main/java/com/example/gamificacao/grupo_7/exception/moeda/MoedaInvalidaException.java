package com.example.gamificacao.grupo_7.exception.moeda;

import com.example.gamificacao.grupo_7.exception.BusinessException;

public class MoedaInvalidaException extends BusinessException {
    public MoedaInvalidaException(String message) {
        super(message);
    }
}
