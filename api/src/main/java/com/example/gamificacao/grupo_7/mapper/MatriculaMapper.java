package com.example.gamificacao.grupo_7.mapper;

import com.example.gamificacao.grupo_7.dto.matricula.MatriculaResponseDTO;
import com.example.gamificacao.grupo_7.dto.matricula.VoucherResponseDTO;
import com.example.gamificacao.grupo_7.model.Aluno;
import com.example.gamificacao.grupo_7.model.Curso;
import com.example.gamificacao.grupo_7.model.Matricula;
import com.example.gamificacao.grupo_7.model.Voucher;
import com.example.gamificacao.grupo_7.model.validation_object.VoucherValue;
import org.springframework.stereotype.Component;

@Component
public class MatriculaMapper {

    public Matricula toEntity(Aluno aluno, Curso curso){
        return Matricula.builder()
                .aluno(aluno)
                .curso(curso)
                .build();
    }

    public MatriculaResponseDTO toResponseDTO(Matricula matricula){
        var curso = matricula.getCurso();
        return new MatriculaResponseDTO(
                matricula.getId(),
                curso.getName(),
                curso.getStatus(),
                curso.getNotaFinal()
        );
    }

    public VoucherResponseDTO toVoucherResponseDTO(Voucher voucher){
        return new VoucherResponseDTO(
                voucher.getId(),
                voucher.getNome(),
                voucher.getValor().getValorEmCentavos() / VoucherValue.CENTAVOS_POR_REAL,
                voucher.getDescricao(),
                voucher.getStatus(),
                voucher.getExpiresAt()
        );
    }

}
