package com.example.gamificacao.grupo_7.service;

import com.example.gamificacao.grupo_7.dto.RecompensasPremiumDTO;
import com.example.gamificacao.grupo_7.enums.CursosParametros;
import com.example.gamificacao.grupo_7.enums.Plano;
import com.example.gamificacao.grupo_7.mapper.CursoMapper;
import com.example.gamificacao.grupo_7.mapper.MatriculaMapper;
import com.example.gamificacao.grupo_7.mapper.VoucherMapper;
import com.example.gamificacao.grupo_7.model.Aluno;
import com.example.gamificacao.grupo_7.model.Curso;
import com.example.gamificacao.grupo_7.model.Matricula;
import com.example.gamificacao.grupo_7.model.Recompensa;
import com.example.gamificacao.grupo_7.model.Voucher;
import com.example.gamificacao.grupo_7.repository.AlunoRepository;
import com.example.gamificacao.grupo_7.repository.MatriculaRepository;
import com.example.gamificacao.grupo_7.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsavel por conceder as recompensas de progressao definidas na planilha
 * Template_ATDD_Gamificacao.xlsx (aba pb):
 *
 * TDD1 / BDD1 - liberacao de 3 cursos bonus ao concluir curso com nota > 7.0
 *               por aluno basico com menos de 11 cursos concluidos;
 * TDD2 / BDD2 - pacote de upgrade para Premium no 12o curso concluido com
 *               nota > 7.0: 3 cursos, 3 moedas e voucher para projetos reais.
 *
 * Cada metodo publico corresponde ao passo GREEN de um dos ciclos ATDD:
 * a PoliticaProgressao decide qual recompensa conceder e este servico a aplica.
 */
@Service
@RequiredArgsConstructor
public class RecompensasService {

    private static final int QUANTIDADE_MOEDAS_PREMIUM = 3;
    private static final String NOME_VOUCHER_PREMIUM = "Voucher Projetos Reais";
    private static final double VALOR_VOUCHER_PREMIUM = 100.0;
    private static final String DESCRICAO_VOUCHER_PREMIUM =
            "Voucher para participacao em projetos reais, concedido no upgrade para o plano Premium";
    private static final String DESCRICAO_VOUCHER_RECURRENTE =
            "Voucher para participacao em projetos reais, concedido a cada 12 cursos concluidos";

    private final MatriculaRepository matriculaRepository;
    private final VoucherRepository voucherRepository;
    private final AlunoRepository alunoRepository;
    private final CursoMapper cursoMapper;
    private final VoucherMapper voucherMapper;
    private final MatriculaMapper matriculaMapper;

    /**
     * TDD1 - GREEN / BLUE:
     * "Entao o sistema deve liberar o acesso a 3 novos cursos E manter a
     * assinatura no plano basico" - cria 3 matriculas com status INICIADO,
     * sem tocar em plano, moedas ou voucher.
     */
    public Recompensa liberarCursosBonus(Aluno aluno){
        return new Recompensa(this.matricularCursos(aluno, this.criaCursosBonus()), null, 0);
    }

    /**
     * TDD2 - GREEN / BLUE:
     * "Entao a assinatura deve ser alterada para Premium E conceder 3 cursos,
     * 3 moedas e voucher para projetos reais" - aplica o upgrade, credita as
     * moedas, emite o voucher (VALIDO, expira em 7 dias) e matricula os 3
     * cursos bonus.
     */
    public Recompensa concederRecompensasPremium(Aluno aluno){
        var recompensas = this.montarRecompensasPremium(aluno);
        this.aplicarUpgradePremium(aluno, recompensas.moedas());
        Voucher voucherSalvo = this.voucherRepository.save(recompensas.voucher());
        var cursosLiberados = this.matricularCursos(aluno, recompensas.cursos());
        return new Recompensa(cursosLiberados, voucherSalvo, recompensas.moedas());
    }

    /**
     * TDD5 - GREEN (recorrencia do BDD2, "a cada 12 cursos concluidos"):
     * credita 3 novas moedas e emite um novo voucher para projetos reais
     * (VALIDO, expira em 7 dias), sem alterar o plano e sem matricular
     * cursos bonus.
     */
    public Recompensa concederRecompensasRecorrentes(Aluno aluno){
        aluno.setMoedas(aluno.getMoedas() + QUANTIDADE_MOEDAS_PREMIUM);
        this.alunoRepository.save(aluno);
        Voucher voucher = this.voucherMapper.buildEntity(
                NOME_VOUCHER_PREMIUM,
                VALOR_VOUCHER_PREMIUM,
                DESCRICAO_VOUCHER_RECURRENTE,
                aluno
        );
        return new Recompensa(List.of(), this.voucherRepository.save(voucher), QUANTIDADE_MOEDAS_PREMIUM);
    }

    private List<Curso> criaCursosBonus(){
        List<Curso> cursos = new ArrayList<>();
        for (int i = 1; i <= CursosParametros.QUANTIDADE_CURSOS_BONUS.getValor(); i++){
            cursos.add(this.cursoMapper.buildEntity("Curso Liberado " + i, 0.0));
        }
        return cursos;
    }

    private RecompensasPremiumDTO montarRecompensasPremium(Aluno aluno){
        Voucher voucher = this.voucherMapper.buildEntity(
                NOME_VOUCHER_PREMIUM,
                VALOR_VOUCHER_PREMIUM,
                DESCRICAO_VOUCHER_PREMIUM,
                aluno
        );
        return new RecompensasPremiumDTO(voucher, this.criaCursosBonus(), QUANTIDADE_MOEDAS_PREMIUM);
    }

    private void aplicarUpgradePremium(Aluno aluno, Integer moedas){
        aluno.setPlano(Plano.PREMIUM);
        aluno.setMoedas(aluno.getMoedas() + moedas);
        this.alunoRepository.save(aluno);
    }

    private List<Matricula> matricularCursos(Aluno aluno, List<Curso> cursos){
        return cursos
                .stream()
                .map(curso -> this.matriculaRepository.save(this.matriculaMapper.toEntity(aluno, curso)))
                .toList();
    }

}
