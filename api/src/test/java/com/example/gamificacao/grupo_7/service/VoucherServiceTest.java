package com.example.gamificacao.grupo_7.service;

import com.example.gamificacao.grupo_7.dto.voucher.VoucherResponseDTO;
import com.example.gamificacao.grupo_7.enums.Plano;
import com.example.gamificacao.grupo_7.enums.VoucherStatus;
import com.example.gamificacao.grupo_7.exception.aluno.AlunoNotFoundException;
import com.example.gamificacao.grupo_7.mapper.VoucherMapper;
import com.example.gamificacao.grupo_7.model.Aluno;
import com.example.gamificacao.grupo_7.model.Voucher;
import com.example.gamificacao.grupo_7.model.validation_object.RA;
import com.example.gamificacao.grupo_7.model.validation_object.VoucherValue;
import com.example.gamificacao.grupo_7.repository.AlunoRepository;
import com.example.gamificacao.grupo_7.repository.VoucherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TDD6: testes do servico de consulta dos vouchers persistidos - o painel
 * "Seus vouchers" faz refresh e nao pode depender do estado da sessao.
 */
@ExtendWith(MockitoExtension.class)
public class VoucherServiceTest {

    private static final UUID ALUNO_ID = UUID.randomUUID();

    @Mock
    VoucherRepository voucherRepository;
    @Mock
    AlunoRepository alunoRepository;
    @Mock
    VoucherMapper voucherMapper;

    @InjectMocks
    VoucherService voucherService;

    private Aluno buildAluno(){
        return Aluno.builder()
                .id(ALUNO_ID)
                .name("Aluno Teste")
                .email("aluno@teste.com")
                .password("123456")
                .ra(new RA("2026123"))
                .plano(Plano.PREMIUM)
                .moedas(3)
                .build();
    }

    private Voucher buildVoucher(Aluno aluno, String descricao){
        return Voucher.builder()
                .id(UUID.randomUUID())
                .nome("Voucher Projetos Reais")
                .valor(new VoucherValue(100.0))
                .descricao(descricao)
                .aluno(aluno)
                .status(VoucherStatus.VALIDO)
                .build();
    }

    /**
     *   Dado um aluno com vouchers persistidos (upgrade e recorrencia)
     *   Quando os vouchers do aluno sao listados
     *   Entao todos sao retornados mapeados para o DTO de resposta
     */
    @Test
    void deveListarVouchersPersistidosDoAluno(){
        var aluno = this.buildAluno();
        var vouchers = List.of(
                this.buildVoucher(aluno, "concedido no upgrade para o plano Premium"),
                this.buildVoucher(aluno, "concedido a cada 12 cursos concluidos")
        );
        when(this.alunoRepository.findById(ALUNO_ID)).thenReturn(Optional.of(aluno));
        when(this.voucherRepository.findAllByAlunoId(ALUNO_ID)).thenReturn(vouchers);
        when(this.voucherMapper.toResponseDTO(any(Voucher.class))).thenAnswer(inv -> {
            var voucher = inv.getArgument(0, Voucher.class);
            return new VoucherResponseDTO(
                    voucher.getId(),
                    voucher.getNome(),
                    voucher.getValor().getValorEmCentavos() / 100.0,
                    voucher.getDescricao(),
                    voucher.getStatus(),
                    voucher.getExpiresAt()
            );
        });

        var resposta = this.voucherService.listarVouchers(ALUNO_ID);

        assertEquals(2, resposta.size());
        assertEquals("Voucher Projetos Reais", resposta.getFirst().nome());
        assertEquals(100.0, resposta.getFirst().valorEmReais());
        assertEquals(VoucherStatus.VALIDO, resposta.getFirst().status());
    }

    /**
     *   Dado um id de aluno que nao existe
     *   Quando os vouchers sao listados
     *   Entao deve lancar AlunoNotFoundException (404 na API)
     */
    @Test
    void deveLancarExcecaoQuandoAlunoNaoExisteAoListarVouchers(){
        when(this.alunoRepository.findById(ALUNO_ID)).thenReturn(Optional.empty());

        assertThrows(AlunoNotFoundException.class, () -> this.voucherService.listarVouchers(ALUNO_ID));
        verify(this.voucherRepository, never()).findAllByAlunoId(any(UUID.class));
    }

}
