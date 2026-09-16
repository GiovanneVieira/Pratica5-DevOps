package com.example.gamificacao.grupo_7.controller;

import com.example.gamificacao.grupo_7.dto.voucher.VoucherResponseDTO;
import com.example.gamificacao.grupo_7.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * TDD6 - GREEN: endpoint de consulta dos vouchers do aluno, consumido pelo
 * painel "Seus vouchers" do dashboard (os vouchers persistidos precisam
 * sobreviver ao refresh da pagina).
 */
@RestController
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @GetMapping("/alunos/{alunoId}/vouchers")
    public ResponseEntity<List<VoucherResponseDTO>> getVouchers(@PathVariable UUID alunoId){
        return ResponseEntity.ok(this.voucherService.listarVouchers(alunoId));
    }

}
