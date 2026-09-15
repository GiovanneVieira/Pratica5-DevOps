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
 * de BDDs: liberacao de cursos bonus (BDD1) e o pacote de upgrade para Premium
 * com 3 cursos, 3 moedas e voucher para projetos reais (BDD2).
 */
@Service
@RequiredArgsConstructor
public class RecompensasService {

    private static final int QUANTIDADE_MOEDAS_PREMIUM = 3;
    private static final String NOME_VOUCHER_PREMIUM = "Voucher Projetos Reais";
    private static final double VALOR_VOUCHER_PREMIUM = 100.0;
    private static final String DESCRICAO_VOUCHER_PREMIUM =
            "Voucher para participacao em projetos reais, concedido no upgrade para o plano Premium";

    private final MatriculaRepository matriculaRepository;
    private final VoucherRepository voucherRepository;
    private final AlunoRepository alunoRepository;
    private final CursoMapper cursoMapper;
    private final VoucherMapper voucherMapper;
    private final MatriculaMapper matriculaMapper;

    public Recompensa liberarCursosBonus(Aluno aluno){
        return new Recompensa(this.matricularCursos(aluno, this.criaCursosBonus()), null);
    }

    public Recompensa concederRecompensasPremium(Aluno aluno){
        var recompensas = this.montarRecompensasPremium(aluno);
        this.aplicarUpgradePremium(aluno, recompensas.moedas());
        Voucher voucherSalvo = this.voucherRepository.save(recompensas.voucher());
        var cursosLiberados = this.matricularCursos(aluno, recompensas.cursos());
        return new Recompensa(cursosLiberados, voucherSalvo);
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
