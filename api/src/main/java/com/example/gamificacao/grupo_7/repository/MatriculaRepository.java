package com.example.gamificacao.grupo_7.repository;

import com.example.gamificacao.grupo_7.model.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatriculaRepository extends JpaRepository<Matricula, UUID> {

    List<Matricula> findAllByAlunoId(UUID alunoId);

    Optional<Matricula> findByIdAndAlunoId(UUID matriculaId, UUID alunoId);

}
