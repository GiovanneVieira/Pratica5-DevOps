package com.example.gamificacao.grupo_7.controller;

import com.example.gamificacao.grupo_7.AbstractIntegrationTest;
import com.example.gamificacao.grupo_7.dto.aluno.AlunoRequestDTO;
import com.example.gamificacao.grupo_7.dto.aluno.AlunoResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Correcao do RA null (integracao de ponta a ponta):
 *
 *   Dado um novo aluno cadastrado via POST /alunos
 *   Entao a resposta deve conter um RA gerado automaticamente (6 digitos)
 *   E o aluno deve ser encontravel por GET /alunos/ra/{ra}
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AlunoFlowIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    private RestClient client;

    @BeforeEach
    void setUp(){
        this.client = RestClient.builder()
                .baseUrl("http://localhost:" + this.port)
                .build();
    }

    @Test
    void alunoCriadoDeveReceberRAEPoderSerBuscadoPorRA(){
        var resposta = this.client.post()
                .uri("/alunos")
                .body(new AlunoRequestDTO("Aluno RA Teste", "aluno.ra@teste.com", "123456"))
                .retrieve()
                .toEntity(AlunoResponseDTO.class);

        assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
        var alunoCriado = resposta.getBody();
        assertNotNull(alunoCriado.ra(), "RA deve vir preenchido na criacao");
        assertTrue(alunoCriado.ra().matches("\\d{6}"),
                "RA deve ter 6 digitos: ano da matricula (2, ex. 26) + sorteio (4)");

        var encontrado = this.client.get()
                .uri("/alunos/ra/" + alunoCriado.ra())
                .retrieve()
                .body(AlunoResponseDTO.class);

        assertEquals(alunoCriado.id(), encontrado.id());
    }

}
