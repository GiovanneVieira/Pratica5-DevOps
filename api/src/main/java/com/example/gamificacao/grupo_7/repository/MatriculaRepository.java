package com.example.gamificacao.grupo_7.repository;

import com.example.gamificacao.grupo_7.enums.CursoStatus;
import com.example.gamificacao.grupo_7.model.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatriculaRepository extends JpaRepository<Matricula, UUID> {

    List<Matricula> findAllByAlunoId(UUID alunoId);

    Optional<Matricula> findByIdAndAlunoId(UUID matriculaId, UUID alunoId);

    @Query("select count(m) from Matricula m where m.aluno.id = :alunoId and m.curso.status = :status")
    long countByAlunoIdAndCursoStatus(@Param("alunoId") UUID alunoId, @Param("status") CursoStatus status);

}
