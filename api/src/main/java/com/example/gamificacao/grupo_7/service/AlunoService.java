package com.example.gamificacao.grupo_7.service;

import com.example.gamificacao.grupo_7.model.Aluno;
import com.example.gamificacao.grupo_7.repository.AlunoRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlunoService {

    private final AlunoRepository alunoRepository;

    public Aluno createAluno(Aluno aluno){
        return this.alunoRepository.save(aluno);
    }

}
