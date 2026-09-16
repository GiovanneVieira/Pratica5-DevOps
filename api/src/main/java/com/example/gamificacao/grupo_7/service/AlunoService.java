package com.example.gamificacao.grupo_7.service;

import com.example.gamificacao.grupo_7.dto.aluno.AlunoRequestDTO;
import com.example.gamificacao.grupo_7.dto.aluno.AlunoResponseDTO;
import com.example.gamificacao.grupo_7.dto.login.LoginRequestDTO;
import com.example.gamificacao.grupo_7.exception.aluno.AlunoNotFoundException;
import com.example.gamificacao.grupo_7.exception.aluno.EmailAlreadyExistsException;
import com.example.gamificacao.grupo_7.exception.login.AuthenticationFailedException;
import com.example.gamificacao.grupo_7.mapper.AlunoMapper;
import com.example.gamificacao.grupo_7.model.validation_object.RA;
import com.example.gamificacao.grupo_7.repository.AlunoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final AlunoMapper alunoMapper;
    private final PasswordEncoder passwordEncoder;

    public AlunoResponseDTO criaAluno(AlunoRequestDTO requestDTO){
        if(this.alunoRepository.existsByEmail(requestDTO.email())){
            throw new EmailAlreadyExistsException("O email " + requestDTO.email() + " ja existe");
        }
        var newAluno = alunoMapper.toEntity(requestDTO);
        var savedAluno = this.alunoRepository.save(newAluno);

        return this.alunoMapper.toResponseDTO(savedAluno);
    }

    public AlunoResponseDTO getAlunoById(UUID id){
        var aluno = this.alunoRepository.findById(id).orElseThrow(() -> new AlunoNotFoundException("Aluno com id " + id + " nao encontrado"));
        return this.alunoMapper.toResponseDTO(aluno);
    }

    public AlunoResponseDTO getAlunoByRA(String ra){
        var aluno = this.alunoRepository.findByRa(new RA(ra)).orElseThrow(() -> new AlunoNotFoundException("Aluno com RA " + ra + " nao encontrado"));
        return this.alunoMapper.toResponseDTO(aluno);
    }

    public List<AlunoResponseDTO> getAlunos(){

        return this.alunoRepository
                .findAll()
                .stream()
                .map(alunoMapper::toResponseDTO)
                .toList();

    }

    /**
     * Login simples com BCrypt: compara a senha do input com o hash
     * armazenado via passwordEncoder.matches. Mensagem generica tanto para
     * email inexistente quanto para senha incorreta, para nao vazar qual
     * campo errou.
     */
    public AlunoResponseDTO login(LoginRequestDTO requestDTO){
        var aluno = this.alunoRepository.findByEmail(requestDTO.email())
                .orElseThrow(() -> new AuthenticationFailedException("Credenciais invalidas"));

        if (!this.passwordEncoder.matches(requestDTO.password(), aluno.getPassword())){
            throw new AuthenticationFailedException("Credenciais invalidas");
        }

        return this.alunoMapper.toResponseDTO(aluno);
    }
}
