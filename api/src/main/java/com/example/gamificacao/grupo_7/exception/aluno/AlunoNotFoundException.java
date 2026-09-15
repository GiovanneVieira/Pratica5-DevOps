package com.example.gamificacao.grupo_7.exception.aluno;

import com.example.gamificacao.grupo_7.exception.BusinessException;

public class AlunoNotFoundException extends BusinessException {
    public AlunoNotFoundException(String s) {
        super(s);
    }
}
