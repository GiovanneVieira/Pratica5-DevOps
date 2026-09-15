package com.example.gamificacao.grupo_7.service;

import com.example.gamificacao.grupo_7.dto.matricula.MatriculaRequestDTO;
import com.example.gamificacao.grupo_7.dto.matricula.MatriculaResponseDTO;
import com.example.gamificacao.grupo_7.exception.aluno.AlunoNotFoundException;
import com.example.gamificacao.grupo_7.mapper.CursoMapper;
import com.example.gamificacao.grupo_7.mapper.MatriculaMapper;
import com.example.gamificacao.grupo_7.model.Aluno;
import com.example.gamificacao.grupo_7.repository.AlunoRepository;
import com.example.gamificacao.grupo_7.repository.MatriculaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Responsavel pelo ciclo de vida das matriculas: criar matricula em novo curso
 * e listar as matriculas de um aluno.
 */
@Service
@RequiredArgsConstructor
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final AlunoRepository alunoRepository;
    private final CursoMapper cursoMapper;
    private final MatriculaMapper matriculaMapper;

    @Transactional
    public MatriculaResponseDTO matricularAluno(UUID alunoId, MatriculaRequestDTO requestDTO){
        var aluno = this.buscaAluno(alunoId);
        var curso = this.cursoMapper.buildEntity(requestDTO.nomeCurso(), 0.0);
        var matricula = this.matriculaRepository.save(this.matriculaMapper.toEntity(aluno, curso));
        return this.matriculaMapper.toResponseDTO(matricula);
    }

    public List<MatriculaResponseDTO> listarMatriculas(UUID alunoId){
        this.buscaAluno(alunoId);
        return this.matriculaRepository.findAllByAlunoId(alunoId)
                .stream()
                .map(this.matriculaMapper::toResponseDTO)
                .toList();
    }

    private Aluno buscaAluno(UUID alunoId){
        return this.alunoRepository.findById(alunoId)
                .orElseThrow(() -> new AlunoNotFoundException("Aluno com id " + alunoId + " nao encontrado"));
    }

}
