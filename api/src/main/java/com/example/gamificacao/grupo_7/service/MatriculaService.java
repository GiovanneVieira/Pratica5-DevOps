package com.example.gamificacao.grupo_7.service;

import com.example.gamificacao.grupo_7.dto.matricula.MatriculaRequestDTO;
import com.example.gamificacao.grupo_7.dto.matricula.MatriculaResponseDTO;
import com.example.gamificacao.grupo_7.exception.aluno.AlunoNotFoundException;
import com.example.gamificacao.grupo_7.exception.matricula.MatriculaNotFoundException;
import com.example.gamificacao.grupo_7.mapper.CursoMapper;
import com.example.gamificacao.grupo_7.mapper.MatriculaMapper;
import com.example.gamificacao.grupo_7.model.Aluno;
import com.example.gamificacao.grupo_7.model.Matricula;
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

    /**
     * Desistencia de curso: remove a matricula (e o curso associado, via
     * cascade do @OneToOne) do historico do aluno. Serve tanto para desistir
     * de um curso em andamento quanto para apagar um concluido/reprovado.
     */
    @Transactional
    public void desistirMatricula(UUID alunoId, UUID matriculaId){
        this.buscaAluno(alunoId);
        var matricula = this.buscaMatricula(alunoId, matriculaId);
        this.matriculaRepository.delete(matricula);
    }

    private Aluno buscaAluno(UUID alunoId){
        return this.alunoRepository.findById(alunoId)
                .orElseThrow(() -> new AlunoNotFoundException("Aluno com id " + alunoId + " nao encontrado"));
    }

    private Matricula buscaMatricula(UUID alunoId, UUID matriculaId){
        return this.matriculaRepository.findByIdAndAlunoId(matriculaId, alunoId)
                .orElseThrow(() -> new MatriculaNotFoundException(
                        "Matricula " + matriculaId + " nao encontrada para o aluno " + alunoId));
    }

}
