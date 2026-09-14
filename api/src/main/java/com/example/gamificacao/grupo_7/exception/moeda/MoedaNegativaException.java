package com.example.gamificacao.grupo_7.exception.moeda;

import com.example.gamificacao.grupo_7.exception.BusinessException;

public class MoedaNegativaException extends BusinessException {
    public MoedaNegativaException(String message) {
        super(message);
    }
}
