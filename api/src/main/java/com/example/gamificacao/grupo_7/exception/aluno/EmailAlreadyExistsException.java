package com.example.gamificacao.grupo_7.exception.aluno;

import com.example.gamificacao.grupo_7.exception.BusinessException;

public class EmailAlreadyExistsException extends BusinessException {
    public EmailAlreadyExistsException(String s) {
        super(s);
    }
}
