package com.example.gamificacao.grupo_7.service;

import com.example.gamificacao.grupo_7.dto.voucher.VoucherResponseDTO;
import com.example.gamificacao.grupo_7.exception.aluno.AlunoNotFoundException;
import com.example.gamificacao.grupo_7.mapper.VoucherMapper;
import com.example.gamificacao.grupo_7.repository.AlunoRepository;
import com.example.gamificacao.grupo_7.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * TDD6 - GREEN: consulta dos vouchers conquistados pelo aluno. Os vouchers
 * sao persistidos no upgrade para Premium (BDD2) e a cada 12 cursos
 * concluidos (TDD5); este servico expoe o que ja esta no banco para a UI
 * nao depender do estado da sessao:
 *
 *   Dado um aluno com vouchers conquistados
 *   Quando a UI recarrega (refresh) e busca GET /alunos/{id}/vouchers
 *   Entao os vouchers persistidos sao retornados
 */
@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final AlunoRepository alunoRepository;
    private final VoucherMapper voucherMapper;

    public List<VoucherResponseDTO> listarVouchers(UUID alunoId){
        this.alunoRepository.findById(alunoId)
                .orElseThrow(() -> new AlunoNotFoundException("Aluno com id " + alunoId + " nao encontrado"));
        return this.voucherRepository.findAllByAlunoId(alunoId)
                .stream()
                .map(this.voucherMapper::toResponseDTO)
                .toList();
    }

}
