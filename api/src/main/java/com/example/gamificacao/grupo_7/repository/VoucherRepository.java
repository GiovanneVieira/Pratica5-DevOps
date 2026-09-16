package com.example.gamificacao.grupo_7.repository;

import com.example.gamificacao.grupo_7.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, UUID> {

    List<Voucher> findAllByAlunoId(UUID alunoId);

}
