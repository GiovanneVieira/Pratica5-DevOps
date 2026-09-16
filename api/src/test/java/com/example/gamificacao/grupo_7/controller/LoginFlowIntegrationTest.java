package com.example.gamificacao.grupo_7.controller;

import com.example.gamificacao.grupo_7.AbstractIntegrationTest;
import com.example.gamificacao.grupo_7.dto.aluno.AlunoRequestDTO;
import com.example.gamificacao.grupo_7.dto.aluno.AlunoResponseDTO;
import com.example.gamificacao.grupo_7.dto.login.LoginRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Login (ciclo ATDD do endpoint de autenticacao):
 *
 *   Dado um aluno cadastrado com senha hasheada em BCrypt
 *   Quando envia email e senha corretos para POST /login
 *   Entao recebe 200 com os dados do aluno
 *   E com senha ou email incorretos recebe 401
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LoginFlowIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    private RestClient client;

    @BeforeEach
    void setUp(){
        this.client = RestClient.builder()
                .baseUrl("http://localhost:" + this.port)
                .build();
    }

    private AlunoResponseDTO criaAluno(String email, String senha){
        return this.client.post()
                .uri("/alunos")
                .body(new AlunoRequestDTO("Aluno Login", email, senha))
                .retrieve()
                .body(AlunoResponseDTO.class);
    }

    @Test
    void loginComEmailESenhaCorretosDeveRetornarDadosDoAluno(){
        this.criaAluno("login.ok@teste.com", "senha123");

        var resposta = this.client.post()
                .uri("/login")
                .body(new LoginRequestDTO("login.ok@teste.com", "senha123"))
                .retrieve()
                .toEntity(AlunoResponseDTO.class);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertNotNull(resposta.getBody().id());
        assertNotNull(resposta.getBody().ra());
        assertEquals("Aluno Login", resposta.getBody().name());
    }

    @Test
    void loginComSenhaIncorretaDeveRetornar401(){
        this.criaAluno("login.senha.errada@teste.com", "senha123");

        var excecao = org.junit.jupiter.api.Assertions.assertThrows(RestClientResponseException.class,
                () -> this.client.post()
                        .uri("/login")
                        .body(new LoginRequestDTO("login.senha.errada@teste.com", "senha-errada"))
                        .retrieve()
                        .toEntity(AlunoResponseDTO.class));

        assertEquals(HttpStatus.UNAUTHORIZED, excecao.getStatusCode());
    }

    @Test
    void loginComEmailInexistenteDeveRetornar401(){
        var excecao = org.junit.jupiter.api.Assertions.assertThrows(RestClientResponseException.class,
                () -> this.client.post()
                        .uri("/login")
                        .body(new LoginRequestDTO("ninguem@teste.com", "senha123"))
                        .retrieve()
                        .toEntity(AlunoResponseDTO.class));

        assertEquals(HttpStatus.UNAUTHORIZED, excecao.getStatusCode());
    }

}
