package com.example.gamificacao.grupo_7.exception.aluno;

import com.example.gamificacao.grupo_7.exception.BusinessException;

public class NotaFinalNaoSuficienteException extends BusinessException {
    public NotaFinalNaoSuficienteException(String message) {
        super(message);
    }
}
