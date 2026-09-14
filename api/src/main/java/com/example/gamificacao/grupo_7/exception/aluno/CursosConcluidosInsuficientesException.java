package com.example.gamificacao.grupo_7.exception.aluno;

import com.example.gamificacao.grupo_7.exception.BusinessException;

public class CursosConcluidosInsuficientesException extends BusinessException {
    public CursosConcluidosInsuficientesException(String message) {
        super(message);
    }
}
