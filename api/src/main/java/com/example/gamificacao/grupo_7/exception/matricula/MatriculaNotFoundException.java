package com.example.gamificacao.grupo_7.exception.matricula;

import com.example.gamificacao.grupo_7.exception.BusinessException;

public class MatriculaNotFoundException extends BusinessException {
    public MatriculaNotFoundException(String message) {
        super(message);
    }
}
