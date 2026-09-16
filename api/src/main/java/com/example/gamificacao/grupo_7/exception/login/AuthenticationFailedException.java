package com.example.gamificacao.grupo_7.exception.login;

import com.example.gamificacao.grupo_7.exception.BusinessException;

public class AuthenticationFailedException extends BusinessException {
    public AuthenticationFailedException(String message) {
        super(message);
    }
}
