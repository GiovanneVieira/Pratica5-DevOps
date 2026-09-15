package com.example.gamificacao.grupo_7.service;

import com.example.gamificacao.grupo_7.dto.matricula.MatriculaRequestDTO;
import com.example.gamificacao.grupo_7.dto.matricula.MatriculaResponseDTO;
import com.example.gamificacao.grupo_7.enums.CursoStatus;
import com.example.gamificacao.grupo_7.enums.Plano;
import com.example.gamificacao.grupo_7.exception.aluno.AlunoNotFoundException;
import com.example.gamificacao.grupo_7.mapper.CursoMapper;
import com.example.gamificacao.grupo_7.mapper.MatriculaMapper;
import com.example.gamificacao.grupo_7.model.Aluno;
import com.example.gamificacao.grupo_7.model.Curso;
import com.example.gamificacao.grupo_7.model.Matricula;
import com.example.gamificacao.grupo_7.model.validation_object.RA;
import com.example.gamificacao.grupo_7.repository.AlunoRepository;
import com.example.gamificacao.grupo_7.repository.MatriculaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MatriculaServiceTest {

    private static final UUID ALUNO_ID = UUID.randomUUID();
    private static final UUID MATRICULA_ID = UUID.randomUUID();

    @Mock
    MatriculaRepository matriculaRepository;
    @Mock
    AlunoRepository alunoRepository;
    @Mock
    CursoMapper cursoMapper;
    @Mock
    MatriculaMapper matriculaMapper;

    @InjectMocks
    MatriculaService matriculaService;

    private Aluno buildAluno(){
        return Aluno.builder()
                .id(ALUNO_ID)
                .name("Aluno Teste")
                .email("aluno@teste.com")
                .password("123456")
                .ra(new RA("2026123"))
                .plano(Plano.BASICO)
                .moedas(0)
                .build();
    }

    @Test
    void deveMatricularAlunoEmNovoCurso(){
        var aluno = this.buildAluno();
        var curso = Curso.builder()
                .name("Curso Teste")
                .status(CursoStatus.INICIADO)
                .notaFinal(0.0)
                .build();
        var matricula = Matricula.builder()
                .id(MATRICULA_ID)
                .aluno(aluno)
                .curso(curso)
                .build();

        when(this.alunoRepository.findById(ALUNO_ID)).thenReturn(Optional.of(aluno));
        when(this.cursoMapper.buildEntity("Curso Teste", 0.0)).thenReturn(curso);
        when(this.matriculaMapper.toEntity(aluno, curso)).thenReturn(matricula);
        when(this.matriculaRepository.save(matricula)).thenReturn(matricula);
        when(this.matriculaMapper.toResponseDTO(matricula))
                .thenReturn(new MatriculaResponseDTO(MATRICULA_ID, "Curso Teste", CursoStatus.INICIADO, 0.0));

        var resposta = this.matriculaService.matricularAluno(ALUNO_ID, new MatriculaRequestDTO("Curso Teste"));

        assertEquals(MATRICULA_ID, resposta.id());
        assertEquals(CursoStatus.INICIADO, resposta.status());
    }

    @Test
    void deveListarMatriculasDoAluno(){
        var aluno = this.buildAluno();
        var matricula = Matricula.builder()
                .id(MATRICULA_ID)
                .aluno(aluno)
                .curso(Curso.builder().name("Curso Teste").status(CursoStatus.CONCLUIDO).notaFinal(9.0).build())
                .build();

        when(this.alunoRepository.findById(ALUNO_ID)).thenReturn(Optional.of(aluno));
        when(this.matriculaRepository.findAllByAlunoId(ALUNO_ID)).thenReturn(List.of(matricula));
        when(this.matriculaMapper.toResponseDTO(matricula))
                .thenReturn(new MatriculaResponseDTO(MATRICULA_ID, "Curso Teste", CursoStatus.CONCLUIDO, 9.0));

        var matriculas = this.matriculaService.listarMatriculas(ALUNO_ID);

        assertEquals(1, matriculas.size());
        assertEquals(CursoStatus.CONCLUIDO, matriculas.getFirst().status());
    }

    @Test
    void deveLancarExcecaoQuandoAlunoNaoExisteAoMatricular(){
        when(this.alunoRepository.findById(ALUNO_ID)).thenReturn(Optional.empty());

        assertThrows(AlunoNotFoundException.class,
                () -> this.matriculaService.matricularAluno(ALUNO_ID, new MatriculaRequestDTO("Curso Teste")));
        verify(this.matriculaRepository, never()).save(any(Matricula.class));
    }

}
