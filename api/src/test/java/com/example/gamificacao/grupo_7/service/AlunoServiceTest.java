package com.example.gamificacao.grupo_7.service;

import com.example.gamificacao.grupo_7.dto.aluno.AlunoRequestDTO;
import com.example.gamificacao.grupo_7.dto.aluno.AlunoResponseDTO;
import com.example.gamificacao.grupo_7.enums.Plano;
import com.example.gamificacao.grupo_7.exception.aluno.AlunoNotFoundException;
import com.example.gamificacao.grupo_7.exception.aluno.EmailAlreadyExistsException;
import com.example.gamificacao.grupo_7.mapper.AlunoMapper;
import com.example.gamificacao.grupo_7.model.Aluno;
import com.example.gamificacao.grupo_7.model.validation_object.RA;
import com.example.gamificacao.grupo_7.repository.AlunoRepository;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@ExtendWith(MockitoExtension.class)
public class AlunoServiceTest {

    @Mock
    AlunoRepository alunoRepository;
    @Mock
    AlunoMapper alunoMapper;

    @InjectMocks
    AlunoService alunoService;

    private Aluno buildAlunoSalvo(){
        return Aluno.builder()
                .id(UUID.randomUUID())
                .name("teste")
                .email("email@teste.com")
                .password("123456")
                .ra(new RA("2026123"))
                .plano(Plano.BASICO)
                .moedas(0)
                .build();
    }

    @Test
    void deveCriarAluno(){

        log.info("Iniciando teste de criacao de aluno");

        AlunoRequestDTO request = new AlunoRequestDTO(
                "teste",
                "email@teste.com",
                "123456"
        );

        var entity = this.buildAlunoSalvo();
        var responseEsperada = new AlunoResponseDTO(entity.getId(), "teste", entity.getRa(), Plano.BASICO);

        when(this.alunoRepository.existsByEmail("email@teste.com")).thenReturn(false);
        when(this.alunoMapper.toEntity(request)).thenReturn(entity);
        when(this.alunoRepository.save(entity)).thenReturn(entity);
        when(this.alunoMapper.toResponseDTO(entity)).thenReturn(responseEsperada);

        var alunoCriado = this.alunoService.criaAluno(request);

        assertEquals("teste", alunoCriado.name());
        assertEquals(Plano.BASICO, alunoCriado.plano());
        verify(this.alunoRepository).save(entity);
    }

    @Test
    void naoDeveCriarAlunoComEmailJaCadastrado(){
        AlunoRequestDTO request = new AlunoRequestDTO(
                "teste",
                "email@teste.com",
                "123456"
        );

        when(this.alunoRepository.existsByEmail("email@teste.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> this.alunoService.criaAluno(request));
        verify(this.alunoRepository, never()).save(any());
    }

    @Test
    void deveBuscarAlunoPorId(){
        var aluno = this.buildAlunoSalvo();
        var responseEsperada = new AlunoResponseDTO(aluno.getId(), "teste", aluno.getRa(), Plano.BASICO);

        when(this.alunoRepository.findById(aluno.getId())).thenReturn(Optional.of(aluno));
        when(this.alunoMapper.toResponseDTO(aluno)).thenReturn(responseEsperada);

        var encontrado = this.alunoService.getAlunoById(aluno.getId());

        assertEquals(aluno.getId(), encontrado.id());
        assertEquals("teste", encontrado.name());
    }

    @Test
    void deveLancarExcecaoQuandoAlunoNaoExistePorId(){
        UUID idInexistente = UUID.randomUUID();

        when(this.alunoRepository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThrows(AlunoNotFoundException.class, () -> this.alunoService.getAlunoById(idInexistente));
    }

    @Test
    void deveBuscarAlunoPorRA(){
        var aluno = this.buildAlunoSalvo();
        var responseEsperada = new AlunoResponseDTO(aluno.getId(), "teste", aluno.getRa(), Plano.BASICO);

        when(this.alunoRepository.findByRa(aluno.getRa())).thenReturn(Optional.of(aluno));
        when(this.alunoMapper.toResponseDTO(aluno)).thenReturn(responseEsperada);

        var encontrado = this.alunoService.getAlunoByRA(aluno.getRa().getRa());

        assertEquals(aluno.getId(), encontrado.id());
    }

    @Test
    void deveLancarExcecaoQuandoAlunoNaoExistePorRA(){
        when(this.alunoRepository.findByRa(any(RA.class))).thenReturn(Optional.empty());

        assertThrows(AlunoNotFoundException.class, () -> this.alunoService.getAlunoByRA("2026999"));
    }

    @Test
    void deveListarAlunos(){
        var aluno = this.buildAlunoSalvo();
        var responseEsperada = new AlunoResponseDTO(aluno.getId(), "teste", aluno.getRa(), Plano.BASICO);

        when(this.alunoRepository.findAll()).thenReturn(List.of(aluno));
        when(this.alunoMapper.toResponseDTO(aluno)).thenReturn(responseEsperada);

        var alunos = this.alunoService.getAlunos();

        assertEquals(1, alunos.size());
        assertEquals("teste", alunos.getFirst().name());
    }

}
