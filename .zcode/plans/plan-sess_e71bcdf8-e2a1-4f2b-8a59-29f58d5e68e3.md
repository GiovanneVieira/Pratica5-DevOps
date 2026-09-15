# Plano: Implementar os 3 cenários BDD da planilha na arquitetura MVC em camadas

## Fonte dos requisitos
Os 3 cenários BDD da aba `pb` de `Template_ATDD_Gamificacao.xlsx`:

| # | Cenário | Regra |
|---|---------|-------|
| BDD1 | Aluno básico com **menos de 11** cursos concluídos conclui com nota **> 7,0** | Libera **3 novos cursos** (status INICIADO) e **mantém plano Básico** |
| BDD2 | Aluno básico com **11 cursos** concluídos termina o **12º** com nota **> 7,0** | Plano vira **Premium** + **3 cursos** + **3 moedas** + **voucher** para projetos reais |
| BDD3 | Aluno conclui curso com nota **≤ 7,0** | Curso **REPROVADO**, nenhum curso liberado, progresso Premium **não incrementado** |

Decisões confirmadas: regras de negócio na **camada Service** (entidade enxuta), testes **apenas JUnit** (Cucumber fica de fora), Spring Security com **permitAll** básico.

---

## Fase 0 — Estabilizar o build (hoje não compila)

1. **`AlunoController.java`** — corrigir mapeamento ambíguo que impede o startup:
   - `GET /alunos/{id}` (UUID) e `GET /alunos/ra/{ra}` (recebe `String`, conversão para `RA` no service — `@PathVariable RA` não tem converter registrado).
   - Adicionar `GET /alunos` expondo `AlunoService.getAlunos()` (já existe sem endpoint).
   - Remover import inútil `org.apache.coyote.Response`.
2. **`AlunoMapper.toResponseDTO`** — bug: retorna `email` no campo `name`.
3. **`VoucherMapper.buildEntity`** — bug: recebe `aluno` mas nunca o atribui ao voucher.
4. **`AlunoServiceTest.deveCriarAluno`** — completar o teste que não compila (variável `alunoCriado` indefinida): stubs `when(alunoMapper.toEntity/toResponseDTO)` + `when(alunoRepository.save)` + chamada real ao service; trocar import JUnit 4 (`org.junit.Assert`) pelo JUnit 5 e limpar imports não usados (Testcontainers, `@SpringBootTest`, etc.).
5. **`config/SecurityConfig.java`** (novo) — `SecurityFilterChain` com `permitAll` para todos os requests + desabilitar CSRF (API REST), para o starter security recém-adicionado não bloquear a API.
6. **`exception/GlobalExceptionHandler.java`** (novo, `@RestControllerAdvice`) — hoje não existe tratamento: `AlunoNotFoundException` → 404, `EmailAlreadyExistsException` → 409, `BusinessException` de regra (plano/moedas/cursos) → 422, `MethodArgumentNotValidException` → 400.
7. **`application.properties`** — remover `spring.sql.init.mode=always` (não existe `data.sql`; sem efeito).

## Fase 1 — Persistência e DTOs para o fluxo de cursos

1. **`MatriculaRepository`** (novo) — `findAllByAlunoId(UUID)`, `findByIdAndAlunoId(...)`, e `countByAlunoIdAndCursoStatus(UUID, CursoStatus)` (contagem de concluídos para as regras BDD1/BDD2, usando a associação já existente `Matricula → Aluno` / `Matricula → Curso`).
2. **`VoucherRepository`** (novo) — `JpaRepository<Voucher, UUID>` para persistir o voucher do BDD2.
3. **DTOs novos** (`dto/matricula/`):
   - `MatriculaRequestDTO(String nomeCurso)` com `@NotBlank` — matricular aluno.
   - `ConcluirCursoRequestDTO(Double notaFinal)` com `@NotNull @DecimalMin("0.0") @DecimalMax("10.0")`.
   - `ConcluirCursoResponseDTO` — status do curso, plano resultante, cursos liberados, moedas e voucher (campos presentes só no BDD2).
   - `MatriculaResponseDTO` — id, nome do curso, status, nota.
4. **Exceções novas** (`exception/matricula/`) — `MatriculaNotFoundException` (404) e `CursoJaConcluidoException` (422).

## Fase 2 — Regras BDD em `MatriculaService` (novo service)

Caso de uso central `concluirCurso(alunoId, matriculaId, notaFinal)`, orquestrando via repositories (entidades ficam enxutas):

1. Carrega aluno e matrícula (exceções 404 se ausentes); rejeita curso já CONCLUIDO.
2. Aplica a nota e chama o já existente `Curso.conclui()` → nota > 7 = CONCLUIDO, senão REPROVADO.
3. **BDD3**: se status ficou REPROVADO (nota ≤ 7,0) → persiste e retorna sem liberar nada e sem progresso.
4. **BDD1**: se plano = BASICO e concluídos *antes* desta conclusão < `CursosParametros.LIMITE_CURSOS_UPGRADE_PREMIUM` (11) → cria 3 matrículas novas com cursos INICIADO ("Curso Liberado N"); plano permanece BASICO.
5. **BDD2**: se concluídos *após* conclusão ≥ `CURSOS_NECESSARIOS_PREMIUM` (12) e plano = BASICO → muda para PREMIUM, `moedas += 3`, cria voucher via `VoucherMapper` ("projetos reais", VALIDO, +7 dias) e concede os 3 cursos bônus.
6. `matricularAluno(alunoId, nomeCurso)` — cria `Matricula` + `Curso` INICIADO (necessário para montar os estados "Dado" dos cenários pela API).
7. `listarMatriculas(alunoId)` — para inspecionar o resultado dos cenários.

`RecompensasPremiumDTO` será reaproveitado como value object interno do pacote de recompensas do BDD2 (voucher + cursos + moedas), evitando deixá-lo órfão.

## Fase 3 — Endpoints REST

**`MatriculaController`** (novo, `@RestController`):
- `POST /alunos/{alunoId}/matriculas` — matricula em curso (201).
- `PATCH /matriculas/{matriculaId}/conclusao` com body `ConcluirCursoRequestDTO` — dispara as regras BDD (200 com resposta completa).
- `GET /alunos/{alunoId}/matriculas` — lista matrículas.

## Fase 4 — Testes JUnit (os 3 cenários da tabela)

1. **`MatriculaServiceTest`** (novo, Mockito puro seguindo o padrão do `AlunoServiceTest`):
   - **BDD1**: básico com 5 concluídos + nota 8 → cria 3 cursos INICIADO, plano BASICO, sem moedas/voucher.
   - **BDD2**: básico com 11 concluídos + nota 8 no 12º → PREMIUM, moedas = 3, voucher criado, 3 cursos liberados.
   - **BDD3**: nota 7,0 e nota 6,5 → curso REPROVADO, nenhuma matrícula nova, plano BASICO, moedas 0.
   - Casos de exceção: aluno/matrícula inexistentes, curso já concluído.
2. **`AlunoServiceTest`** — além do `deveCriarAluno`: email duplicado (lança `EmailAlreadyExistsException`), `getAlunoById`/`getAlunoByRA` com sucesso e com `AlunoNotFoundException`.
3. **`MatriculaFlowIntegrationTest`** (novo, `@SpringBootTest` + Testcontainers PostgreSQL já no pom) — sobe contexto com banco real via `@DynamicPropertySource` e exercita o fluxo HTTP completo dos 3 cenários (cria aluno → matricula → conclui com as notas → verifica respostas). Isso também corrige `ApplicationTests.contextLoads`, que hoje depende de um Postgres manual em localhost:5433.
4. **`AlunoTest`** — remover o bloco de testes comentados (referenciam métodos que não voltarão à entidade); manter classe apenas se houver teste de domínio real (`Curso.conclui()` merece um teste unitário direto).
5. Meta: `./mvnw verify` passa com as regras PMD e JaCoCo (cobertura ≥ 60%) já configuradas no pom.

## Fase 5 — Limpeza

1. **`Aluno.java`** — apagar o código comentado (linhas 57–279) e o comentário de rodapé; entidade fica só com campos e getters/setters.
2. **`README.md`** — atualizar seção de endpoints (`/alunos`, `/alunos/ra/{ra}`, rotas de matrícula) e remover a promessa de "testes BDD com Cucumber" (não existe e não será implementado).
3. Manter as dependências Cucumber no pom (inofensivas) — sem uso.

## Ordem de execução
Fase 0 → 1 → 2 → 3 → 4 → 5, rodando `./mvnw test` ao fim de cada fase. Ao final, apresento o resultado de `./mvnw verify` completo; commit só mediante confirmação.