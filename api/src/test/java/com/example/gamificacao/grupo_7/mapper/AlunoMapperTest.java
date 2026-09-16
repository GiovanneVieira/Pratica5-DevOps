package com.example.gamificacao.grupo_7.mapper;

import com.example.gamificacao.grupo_7.dto.aluno.AlunoRequestDTO;
import com.example.gamificacao.grupo_7.enums.Plano;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Correcao de bug via ATDD (ciclo curto RED-GREEN-BLUE):
 *
 *   Dado um novo aluno sendo cadastrado na plataforma
 *   Quando a entidade e montada a partir do DTO de request
 *   Entao o aluno deve receber um RA gerado automaticamente
 *   E a senha deve ser persistida como hash BCrypt (nunca em texto puro)
 *   E o plano inicial deve ser BASICO com zero moedas
 */
public class AlunoMapperTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AlunoMapper alunoMapper = new AlunoMapper(this.passwordEncoder);

    @Test
    void deveAtribuirRAHashDeSenhaPlanoBasicoEMoedasZeroNaCriacaoDoAluno(){
        var request = new AlunoRequestDTO("Joao da Silva", "joao@email.com", "123456");

        var aluno = this.alunoMapper.toEntity(request);

        assertNotNull(aluno.getRa(), "RA deve ser atribuido na criacao do aluno");
        assertNotNull(aluno.getRa().getRa());
        assertTrue(aluno.getRa().getRa().matches("\\d{6}"),
                "RA deve ter 6 digitos: ano da matricula (2, ex. 26) + sorteio (4)");
        assertEquals(Plano.BASICO, aluno.getPlano());
        assertEquals(0, aluno.getMoedas());

        assertNotEquals("123456", aluno.getPassword(), "Senha nao pode ser persistida em texto puro");
        assertTrue(aluno.getPassword().startsWith("$2"),
                "Hash BCrypt deve comecar com $2 (versoes $2a/$2b/$2y)");
        assertTrue(this.passwordEncoder.matches("123456", aluno.getPassword()),
                "Senha original deve validar contra o hash gerado");
    }

}
