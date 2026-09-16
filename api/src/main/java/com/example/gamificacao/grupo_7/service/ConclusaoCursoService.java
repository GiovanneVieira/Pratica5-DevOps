package com.example.gamificacao.grupo_7.service;

import com.example.gamificacao.grupo_7.dto.matricula.ConcluirCursoRequestDTO;
import com.example.gamificacao.grupo_7.dto.matricula.ConcluirCursoResponseDTO;
import com.example.gamificacao.grupo_7.enums.CursoStatus;
import com.example.gamificacao.grupo_7.exception.aluno.AlunoNotFoundException;
import com.example.gamificacao.grupo_7.exception.matricula.CursoJaConcluidoException;
import com.example.gamificacao.grupo_7.exception.matricula.MatriculaNotFoundException;
import com.example.gamificacao.grupo_7.mapper.MatriculaMapper;
import com.example.gamificacao.grupo_7.model.Aluno;
import com.example.gamificacao.grupo_7.model.Matricula;
import com.example.gamificacao.grupo_7.model.Recompensa;
import com.example.gamificacao.grupo_7.repository.AlunoRepository;
import com.example.gamificacao.grupo_7.repository.MatriculaRepository;
import com.example.gamificacao.grupo_7.service.progressao.PoliticaProgressao;
import com.example.gamificacao.grupo_7.service.progressao.ResultadoProgressao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Orquestra o caso de uso de conclusao de curso, ponto de convergencia dos tres
 * ciclos ATDD (TDD1, TDD2 e TDD3) da planilha Template_ATDD_Gamificacao.xlsx:
 * valida a matricula, aplica a nota, consulta a PoliticaProgressao (quem conhece
 * as regras de cada cenario) e concede a recompensa correspondente via
 * RecompensasService. Nao conhece os detalhes de nenhuma regra nem recompensa.
 *
 * Fluxo em comum aos cenarios:
 *   Dado um aluno com assinatura basica ativa e uma matricula em curso da grade
 *   Quando o aluno conclui o curso com uma nota final
 *   Entao a PoliticaProgressao decide o resultado e as recompensas sao aplicadas
 */
@Service
@RequiredArgsConstructor
public class ConclusaoCursoService {

    private final MatriculaRepository matriculaRepository;
    private final AlunoRepository alunoRepository;
    private final MatriculaMapper matriculaMapper;
    private final PoliticaProgressao politicaProgressao;
    private final RecompensasService recompensasService;

    @Transactional
    public ConcluirCursoResponseDTO concluir(UUID alunoId, UUID matriculaId, ConcluirCursoRequestDTO requestDTO){
        var aluno = this.buscaAluno(alunoId);
        var matricula = this.buscaMatricula(alunoId, matriculaId);
        this.validaCursoNaoConcluido(matricula);

        long concluidosAntes = this.matriculaRepository.countByAlunoIdAndCursoStatus(alunoId, CursoStatus.CONCLUIDO);
        this.aplicaNotaFinal(matricula, requestDTO.notaFinal());

        var resultado = this.politicaProgressao.avaliar(matricula.getCurso().getStatus(), aluno.getPlano(), concluidosAntes);
        var recompensa = this.concedeRecompensa(aluno, resultado);

        return this.montaResposta(matricula, aluno, recompensa);
    }

    private Recompensa concedeRecompensa(Aluno aluno, ResultadoProgressao resultado){
        return switch (resultado) {
            case UPGRADE_PREMIUM -> this.recompensasService.concederRecompensasPremium(aluno);
            case CURSO_BONUS_BASICO -> this.recompensasService.liberarCursosBonus(aluno);
            case REPROVADO_SEM_PROGRESSO, SEM_RECOMPENSA -> Recompensa.vazia();
        };
    }

    private void aplicaNotaFinal(Matricula matricula, Double notaFinal){
        var curso = matricula.getCurso();
        curso.setNotaFinal(notaFinal);
        curso.conclui();
        this.matriculaRepository.save(matricula);
    }

    private void validaCursoNaoConcluido(Matricula matricula){
        var curso = matricula.getCurso();
        if (curso.getStatus() == CursoStatus.CONCLUIDO){
            throw new CursoJaConcluidoException("O curso " + curso.getName() + " ja foi concluido");
        }
    }

    private ConcluirCursoResponseDTO montaResposta(Matricula matricula, Aluno aluno, Recompensa recompensa){
        var cursosLiberados = recompensa.cursosLiberados()
                .stream()
                .map(this.matriculaMapper::toResponseDTO)
                .toList();

        var voucherDTO = recompensa.voucher() != null
                ? this.matriculaMapper.toVoucherResponseDTO(recompensa.voucher())
                : null;

        return new ConcluirCursoResponseDTO(
                matricula.getId(),
                matricula.getCurso().getName(),
                matricula.getCurso().getStatus(),
                aluno.getPlano(),
                aluno.getMoedas(),
                cursosLiberados,
                voucherDTO
        );
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
