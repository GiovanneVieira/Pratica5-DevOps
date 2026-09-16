package com.example.gamificacao.grupo_7.repository;

import com.example.gamificacao.grupo_7.model.Aluno;
import com.example.gamificacao.grupo_7.model.validation_object.RA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlunoRepository extends JpaRepository<Aluno, UUID> {
    boolean existsByEmail(String email);

    Optional<Aluno> findByEmail(String email);

    @Query("select a from aluno a where a.ra = :ra")
    Optional<Aluno> findByRa(RA ra);

}
