# 🎮 Gamificação - Prática 5 DevOps (Grupo 7)

Projeto desenvolvido como parte da disciplina de Prática 5 de DevOps. Trata-se de uma aplicação full stack com arquitetura monorepo composta por:
- **Backend (`api/`)**: API REST desenvolvida em **Java 21** com **Spring Boot 4**, **Spring Data JPA**, **Hibernate** e conexão com banco de dados relacional.
- **Frontend (`client/`)**: Aplicação web desenvolvida com **React 19**, **TypeScript** e **Vite**.
- **Banco de Dados (`postgres`)**: **PostgreSQL 16 (Alpine)** executado em container Docker.
- **Orquestração & DevOps**: Ambientes containerizados com **Docker** e **Docker Compose**, contando com suporte a multi-stage builds e **Docker Compose Watch** para sincronização em tempo real (hot-reload).

---

## 📋 Sumário

- [Visão Geral e Arquitetura](#-visão-geral-e-arquitetura)
- [Mapeamento de Portas e Serviços](#-mapeamento-de-portas-e-serviços)
- [Pré-requisitos](#-pré-requisitos)
- [Como Clonar o Repositório](#-como-clonar-o-repositório)
- [Configuração de Variáveis de Ambiente](#-configuração-de-variáveis-de-ambiente)
- [Como Executar e Compilar via Docker](#-como-executar-e-compilar-via-docker-recomendado)
  - [Subindo o Ambiente Completo](#subindo-o-ambiente-completo)
  - [Desenvolvimento com Docker Watch (Hot-Reload)](#desenvolvimento-com-docker-watch-hot-reload)
  - [Compilação Manual das Imagens Docker (Multi-Stage Builds)](#compilação-manual-das-imagens-docker-multi-stage-builds)
  - [Executando Testes dentro dos Containers Docker](#executando-testes-dentro-dos-containers-docker)
  - [Comandos Úteis do Docker Compose](#comandos-úteis-do-docker-compose)
- [Como Compilar e Executar Normalmente (Local / Sem Docker)](#-como-compilar-e-executar-normalmente-local--sem-docker)
  - [Passo 1: Subir Apenas o Banco PostgreSQL](#passo-1-subir-apenas-o-banco-postgresql)
  - [Passo 2: Backend (Spring Boot)](#passo-2-backend-spring-boot)
  - [Passo 3: Frontend (React + Vite)](#passo-3-frontend-react--vite)
- [Como Conectar ao PostgreSQL pelo IntelliJ IDEA](#-como-conectar-ao-postgresql-pelo-intellij-idea)
- [Endpoints da API e Exemplos de Uso](#-endpoints-da-api-e-exemplos-de-uso)
- [Estrutura de Pastas do Repositório](#-estrutura-de-pastas-do-repositório)
- [Resolução de Problemas (Troubleshooting)](#-resolução-de-problemas-troubleshooting)

---

## 🏛 Visão Geral e Arquitetura

```mermaid
flowchart LR
    subgraph Host["Máquina Host / Navegador"]
        ClientBrowser["Navegador Web<br/>(localhost:5173)"]
        IDE["IntelliJ IDEA / DBeaver<br/>(localhost:5433)"]
        Curl["Requisições HTTP / Postman<br/>(localhost:8080)"]
    end

    subgraph DockerEnv["Docker Compose Network"]
        ClientContainer["Container Frontend: devops_client<br/>(Node 24 Alpine / Vite)"]
        ApiContainer["Container Backend: devops_api<br/>(Java 21 Eclipse Temurin / Spring Boot)"]
        DbContainer[("Container Banco: devops_postgres<br/>PostgreSQL 16 Alpine")]
    end

    ClientBrowser -->|"Acessa interface"| ClientContainer
    Curl -->|"Chamadas REST"| ApiContainer
    ClientContainer -->|"Comunicação com API"| ApiContainer
    ApiContainer -->|"Persistência JPA - porta 5432"| DbContainer
    IDE -->|"Conexão Externa - porta mapeada 5433"| DbContainer
```

---

## 🔌 Mapeamento de Portas e Serviços

| Serviço | Nome do Container | Porta no Container | Porta no Host (Localhost) | Descrição |
| :--- | :--- | :---: | :---: | :--- |
| **API** | `devops_api` | `8080` | `8080` | Servidor REST Spring Boot |
| **API (Debug)** | `devops_api` | `5005` | `5005` | Porta para Remote Debugging JVM |
| **Client** | `devops_client` | `5173` | `5173` | Servidor de desenvolvimento Vite |
| **PostgreSQL** | `devops_postgres` | `5432` | **`5433`** | Banco de dados (*Atenção à porta 5433 no host*) |

> [!WARNING]
> **Atenção à porta do PostgreSQL**: No host, o PostgreSQL está exposto na porta **`5433`** (para não conflitar com possíveis instâncias locais do Postgres rodando na 5432). Dentro da rede Docker dos containers, a porta interna usada pela API é a **`5432`**.

---

## 🧰 Pré-requisitos

### Para rodar via Docker:
- [Docker](https://www.docker.com/) (versão 24 ou superior)
- [Docker Compose](https://docs.docker.com/compose/) (versão 2.20 ou superior com suporte a `develop.watch`)

### Para compilar e rodar localmente (sem Docker nos apps):
- [Java JDK 21](https://adoptium.net/) configurado no sistema (`JAVA_HOME`)
- [Node.js](https://nodejs.org/) versão 20 LTS ou 24 LTS e gerenciador `npm`
- [Maven](https://maven.apache.org/) (opcional, pois o projeto já inclui o Maven Wrapper `mvnw` e `mvnw.cmd`)
- Uma instância do PostgreSQL ativa (pode ser executada via container)
- [Git](https://git-scm.com/) instalado no sistema

---

## 📥 Como Clonar o Repositório

Para obter o código-fonte na sua máquina local:

```bash
# 1. Clonar o repositório via HTTPS:
git clone https://github.com/GiovanneVieira/Pratica5-DevOps.git

# Ou clonar via SSH (caso possua chaves SSH cadastradas no GitHub):
git clone git@github.com:GiovanneVieira/Pratica5-DevOps.git

# 2. Acessar o diretório do projeto:
cd Pratica5-DevOps

# 3. (Obrigatório) Criar uma branch para desenvolver:
git checkout -b nome-da-branch
```

---

## ⚙ Configuração de Variáveis de Ambiente

Antes de iniciar os containers ou a compilação, crie os arquivos de ambiente baseando-se nos exemplos:

### Linux / macOS / Git Bash:
```bash
cp .env.api.example .env.api
cp .env.client.example .env.client
```

### Windows (PowerShell):
```powershell
Copy-Item .env.api.example .env.api
Copy-Item .env.client.example .env.client
```

### Conteúdo padrão dos arquivos:

- **`.env.api`**:
  ```env
  POSTGRES_DB=devops
  POSTGRES_USER=postgres
  POSTGRES_PASSWORD=admin
  DATABASE_URL=jdbc:postgresql://postgres:5432/devops
  ```

- **`.env.client`**:
  ```env
  VITE_API_URL=http://localhost:8080
  ```

---

## 🐳 Como Executar e Compilar via Docker (Recomendado)

### Subindo o Ambiente Completo

Para construir as imagens e iniciar todos os serviços (Postgres, API e Client) em segundo plano:

```bash
docker compose up -d --build
```

Após a inicialização:
- **Frontend**: [http://localhost:5173](http://localhost:5173)
- **API Health Check**: [http://localhost:8080/health](http://localhost:8080/health)
- **PostgreSQL**: `localhost:5433`

---

### Desenvolvimento com Docker Watch (Hot-Reload)

O arquivo `docker-compose.yaml` está configurado com `develop.watch`, permitindo sincronização automática de arquivos e recarregamento sem precisar reconstruir manualmente os containers:

```bash
docker compose watch
```
*(ou `docker compose up --watch`)*

- **API (`api/`)**:
  - Alterações em `pom.xml` disparam rebuild automático.
  - Alterações no código-fonte (`api/src`) são sincronizadas e reiniciam a aplicação (`sync+restart`).
- **Client (`client/`)**:
  - Alterações em `client/src` são sincronizadas instantaneamente (`sync`).
  - Alterações em `package.json` ou `package-lock.json` acionam o rebuild do container.

---

### Compilação Manual das Imagens Docker (Multi-Stage Builds)

Tanto o backend quanto o frontend utilizam arquivos Docker multi-estágio (`dockerfile`), permitindo compilar tanto imagens focadas em desenvolvimento quanto imagens otimizadas para produção:

#### 1. Backend (`api/dockerfile`)
- **Estágio de Desenvolvimento (`dev`)**:
  ```bash
  docker build -t devops-api:dev --target dev ./api
  ```
- **Estágio de Produção (`prod`)** *(gera o `.jar`, cria usuário não-root `spring` e utiliza a JRE mínima `eclipse-temurin:21-jre-alpine`)*:
  ```bash
  docker build -t devops-api:prod --target prod ./api
  ```

#### 2. Frontend (`client/dockerfile`)
- **Estágio de Desenvolvimento (`dev`)**:
  ```bash
  docker build -t devops-client:dev --target dev ./client
  ```
- **Estágio de Produção (`production`)** *(roda compilação TypeScript, `vite build`, cria usuário não-root `nodejs` e empacota os arquivos na pasta `dist/`)*:
  ```bash
  docker build -t devops-client:prod --target production ./client
  ```

---

### Executando Testes dentro dos Containers Docker

Você pode executar toda a suíte de testes (testes unitários JUnit e testes BDD com Cucumber) diretamente dentro dos containers Docker, sem precisar de Java ou Node instalados na máquina host:

#### 1. Com os containers em execução (`docker compose exec`)

Se o ambiente já estiver rodando (`docker compose up -d`):

- **Executar todos os testes do Backend (API)**:
  ```bash
  docker compose exec api mvn test
  ```

- **Executar uma classe de testes específica**:
  ```bash
  docker compose exec api mvn test -Dtest=ApplicationTests
  docker compose exec api mvn test -Dtest=AlunoTest
  ```

- **Executar linter e checagem de tipos do Frontend**:
  ```bash
  docker compose exec client npm run lint
  docker compose exec client npm run build
  ```

#### 2. Em container temporário sob demanda (`docker compose run`)

Caso o ambiente não esteja rodando, você pode disparar um container efêmero apenas para a execução dos testes:

- **Testes do Backend**:
  ```bash
  docker compose run --rm api mvn test
  ```

- **Linter do Frontend**:
  ```bash
  docker compose run --rm client npm run lint
  ```

#### 3. Testes durante o Build da Imagem (CI / CD)

O estágio `builder` do arquivo `api/dockerfile` executa `mvn package`, o que roda todos os testes automaticamente. Se qualquer teste falhar, o processo de build é interrompido garantindo a integridade da imagem:

```bash
docker build -t devops-api:builder --target builder ./api
```

---

### Comandos Úteis do Docker Compose

```bash
# Visualizar logs de todos os containers em tempo real
docker compose logs -f

# Visualizar logs apenas da API
docker compose logs -f api

# Visualizar logs apenas do banco de dados
docker compose logs -f postgres

# Parar todos os containers mantendo os dados do banco
docker compose down

# Parar todos os containers e REMOVER o volume com os dados do banco
docker compose down -v
```

---

## 💻 Como Compilar e Executar Normalmente (Local / Sem Docker)

Se preferir rodar e debugar as aplicações diretamente no seu sistema operacional:

### Passo 1: Subir Apenas o Banco PostgreSQL

Você pode subir apenas o container do banco de dados via Docker sem subir a API e o Client:

```bash
docker compose up postgres -d
```
O PostgreSQL estará disponível em `localhost:5433` (usuário: `postgres`, senha: `admin`, database: `devops`).

---

### Passo 2: Backend (Spring Boot)

> [!TIP]
> **Dificuldades com o IntelliSense / Autocomplete no IntelliJ IDEA?**
> Por se tratar de um monorepo com o backend em uma subpasta (`api/`), o IntelliJ IDEA pode não indexar o Maven de imediato, fazendo com que classes, imports ou anotações do Spring fiquem vermelhas ou sem autocompletar.
> 
> **Como resolver**:
> 1. No painel lateral esquerdo (**Project**), localize e expanda a pasta `api`.
> 2. Clique com o **botão direito** sobre o arquivo `pom.xml`.
> 3. Clique em **"Add as Maven Project"** (ou se já foi adicionado, selecione **Maven ➔ Reload Project**).
> 
> <p align="center">
>   <img src="docs/images/add_as_maven_project.png" alt="Adicionar como Projeto Maven no IntelliJ" width="400"/>
> </p>

1. Acesse o diretório `api`:
   ```bash
   cd api
   ```

2. **Configuração de conexão com o banco**:
   *(Nota: O arquivo `application.properties` já vem configurado por padrão para conectar em `localhost:5433`. Caso queira sobrescrever ou customizar portas/credenciais, defina as variáveis de ambiente abaixo)*:
   - **Linux / macOS**:
     ```bash
     export DATABASE_URL="jdbc:postgresql://localhost:5433/devops"
     export POSTGRES_USER="postgres"
     export POSTGRES_PASSWORD="admin"
     ```
   - **Windows (PowerShell)**:
     ```powershell
     $env:DATABASE_URL="jdbc:postgresql://localhost:5433/devops"
     $env:POSTGRES_USER="postgres"
     $env:POSTGRES_PASSWORD="admin"
     ```

3. **Compilar o projeto**:
   - Linux/macOS:
     ```bash
     ./mvnw clean compile
     ```
   - Windows:
     ```powershell
     .\mvnw.cmd clean compile
     ```

4. **Executar os testes automatizados (JUnit & BDD / Cucumber)**:
   - Linux/macOS:
     ```bash
     ./mvnw test
     ```
   - Windows:
     ```powershell
     .\mvnw.cmd test
     ```
   *Executa os testes de unidade JUnit e os testes de aceitação BDD com Cucumber (especificados a partir da planilha `Template_ATDD_Gamificacao.xlsx`).*


5. **Gerar o pacote de produção (JAR)**:
   - Linux/macOS:
     ```bash
     ./mvnw clean package
     ```
   - Windows:
     ```powershell
     .\mvnw.cmd clean package
     ```
   *O arquivo executável será gerado em: `api/target/gamificacao.grupo-7-0.0.1-SNAPSHOT.jar`.*

6. **Executar a API**:
   - Via plugin do Spring Boot:
     ```bash
     # Linux / macOS
     ./mvnw spring-boot:run

     # Windows
     .\mvnw.cmd spring-boot:run
     ```
   - Ou executando o arquivo JAR diretamente:
     ```bash
     java -jar target/gamificacao.grupo-7-0.0.1-SNAPSHOT.jar
     ```

---

### Passo 3: Frontend (React + Vite)

1. Acesse o diretório `client`:
   ```bash
   cd client
   ```

2. Instale as dependências:
   ```bash
   npm install
   ```

3. Inicie o servidor de desenvolvimento:
   ```bash
   npm run dev
   ```
   Acesse a aplicação no navegador em: `http://localhost:5173`.

4. **Compilar para produção**:
   ```bash
   npm run build
   ```
   *A compilação verifica a tipagem TypeScript (`tsc -b`) e cria a pasta `dist/` com o bundle final otimizado.*

5. **Executar a verificação de linter**:
   ```bash
   npm run lint
   ```

---

## 🐘 Como Conectar ao PostgreSQL pelo IntelliJ IDEA

Siga o passo a passo detalhado para gerenciar e visualizar as tabelas do banco de dados diretamente pelo IntelliJ IDEA:

### 1. Certifique-se de que o container do banco está ativo
Antes de tentar a conexão, verifique se o PostgreSQL está em execução:
```bash
docker compose up postgres -d
```

### 2. Abra a janela de Database no IntelliJ
- No canto direito da tela do IntelliJ IDEA, clique na aba vertical **Database**.
- *(Se não estiver visível, acesse o menu superior: **View** ➔ **Tool Windows** ➔ **Database**)*.

> [!NOTE]
> A ferramenta nativa de **Database** faz parte do **IntelliJ IDEA Ultimate**. Caso utilize o **IntelliJ IDEA Community**, você pode instalar o plugin gratuito **Database Navigator** pelo Marketplace ou utilizar um cliente externo como o [DBeaver](https://dbeaver.io/) ou [pgAdmin](https://www.pgadmin.org/) usando exatamente as mesmas configurações descritas abaixo.

### 3. Criar uma nova conexão (Data Source)
1. Na janela **Database**, clique no botão **`+`** (New) no topo.
2. Navegue até **Data Source** ➔ selecione **PostgreSQL**.

### 4. Preencher as configurações de conexão
Na janela de propriedades da fonte de dados (**Data Sources and Drivers**), preencha os campos com os valores a seguir:

| Campo | Valor a Preencher | Observações |
| :--- | :--- | :--- |
| **Name** | `devops_postgres` | Nome de identificação no IntelliJ |
| **Host** | `localhost` | Máquina local |
| **Port** | **`5433`** | ⚠️ **Importante**: Utilize `5433` (conforme mapeado no `docker-compose.yaml`) |
| **Authentication** | `User & Password` | |
| **User** | `postgres` | Usuário configurado |
| **Password** | `admin` | Senha configurada |
| **Database** | `devops` | Nome da base de dados |
| **URL (gerada)** | `jdbc:postgresql://localhost:5433/devops` | Confirme se a URL coincide |

### 5. Download dos drivers (caso necessário)
Se for a primeira vez configurando o PostgreSQL no IntelliJ, haverá uma mensagem de aviso no rodapé da janela: *"Driver files are missing"*. Basta clicar no link azul **Download** para que a IDE baixe o driver JDBC oficial automaticamente.

### 6. Testar a conexão
1. Clique no botão **Test Connection** (localizado no canto inferior esquerdo da janela).
2. Se os dados estiverem corretos e o container ativo, uma mensagem com ícone verde aparecerá:
   ```text
   Succeeded: PostgreSQL 16.x ...
   ```
3. Clique em **Apply** e em seguida em **OK**.

### 7. Explorando as tabelas geradas pelo Hibernate/JPA
1. Na aba **Database**, expanda a árvore:
   ```text
   devops_postgres
   └── @localhost
       └── devops (database)
           └── schemas
               └── public
                   └── tables
   ```
2. Você verá as entidades mapeadas:
   - `table_aluno`: Tabela contendo os registros de alunos (`aluno_id` [UUID], `name` [VARCHAR]).
   - `curso_table`: Tabela contendo os cursos vinculados (`id` [UUID], `status` [VARCHAR], `aluno_id` [UUID]).
3. **Para ver os dados**: Dê um duplo clique sobre o nome da tabela.
4. **Para executar consultas SQL**: Clique com o botão direito sobre a fonte de dados e escolha **New** ➔ **Query Console**.

---

## 📡 Endpoints da API e Exemplos de Uso

A API REST roda na porta **`8080`**. Abaixo estão os endpoints disponíveis e como testá-los:

### 1. Health Check
Verifica se a aplicação está online.
- **Método**: `GET`
- **URL**: `http://localhost:8080/health`
- **Exemplo via cURL**:
  ```bash
  curl -X GET http://localhost:8080/health
  ```
- **Resposta esperada**:
  ```json
  {
    "message": "OK"
  }
  ```

---

### 2. Cadastro de Aluno
Registra um novo aluno no banco de dados.
- **Método**: `POST`
- **URL**: `http://localhost:8080/aluno`
- **Header**: `Content-Type: application/json`
- **Body**:
  ```json
  {
    "name": "João da Silva"
  }
  ```
- **Exemplo via cURL**:
  ```bash
  curl -X POST http://localhost:8080/aluno \
    -H "Content-Type: application/json" \
    -d "{\"name\": \"João da Silva\"}"
  ```
- **Exemplo via PowerShell**:
  ```powershell
  Invoke-RestMethod -Uri "http://localhost:8080/aluno" -Method POST -ContentType "application/json" -Body '{"name": "João da Silva"}'
  ```
- **Resposta esperada**:
  ```json
  {
    "id": "e4f8a32b-9e0f-48d6-b0ad-5b43a9f5d1e2",
    "name": "João da Silva"
  }
  ```

---

## 📂 Estrutura de Pastas do Repositório

```text
Pratica5-DevOps/
├── .env.api.example             # Template de variáveis de ambiente da API
├── .env.client.example          # Template de variáveis de ambiente do Frontend
├── docker-compose.yaml          # Orquestração dos serviços (api, client, postgres)
├── README.md                    # Documentação principal do projeto
│
├── api/                         # Backend em Spring Boot
│   ├── dockerfile               # Multi-stage Dockerfile (dev, builder, prod)
│   ├── pom.xml                  # Gerenciador de dependências Maven
│   ├── mvnw / mvnw.cmd          # Maven Wrapper (Linux / Windows)
│   └── src/
│       ├── main/
│       │   ├── java/com/example/gamificacao/grupo_7/
│       │   │   ├── Application.java
│       │   │   ├── controller/   # Controllers REST (AlunoController, HealthController)
│       │   │   ├── enums/        # Enums de domínio (CursoStatus)
│       │   │   ├── model/        # Entidades JPA (Aluno, Curso)
│       │   │   ├── repository/   # Interfaces de repositório Spring Data
│       │   │   └── service/      # Regras de negócio (AlunoService)
│       │   └── resources/
│       │       └── application.properties # Configurações da aplicação e datasource
│       └── test/                # Testes de unidade e integração
│
└── client/                      # Frontend em React + Vite + TypeScript
    ├── dockerfile               # Multi-stage Dockerfile (deps, dev, build, production)
    ├── package.json             # Dependências e scripts npm
    ├── tsconfig.json            # Configuração TypeScript
    ├── vite.config.ts           # Configuração do Vite
    └── src/                     # Código-fonte React (App.tsx, main.tsx, assets)
```

---

## 🔧 Resolução de Problemas (Troubleshooting)

| Problema | Causa Provável | Solução |
| :--- | :--- | :--- |
| **Erro ao conectar no Postgres pelo IntelliJ na porta 5432** | O container expõe a porta `5433` no host, e não `5432`. | Mude a porta na tela de conexão do IntelliJ para **`5433`**. |
| **A API local não conecta ao banco de dados** | A variável `DATABASE_URL` não está configurada e tenta conectar em `postgres:5432`. | Configure a variável de ambiente: `DATABASE_URL=jdbc:postgresql://localhost:5433/devops`. |
| **Docker Compose falha com aviso sobre `.env.api` ou `.env.client`** | Os arquivos `.env.api` e `.env.client` ainda não foram criados. | Execute `cp .env.api.example .env.api` e `cp .env.client.example .env.client`. |
| **Porta 8080 ou 5173 já em uso** | Outro serviço ou aplicação local está rodando nestas portas. | Finalize o processo conflitante ou altere a porta mapeada no `docker-compose.yaml`. |
| **IntelliSense/imports em vermelho no IntelliJ** | O arquivo `api/pom.xml` não foi importado como projeto Maven. | Clique com o botão direito em `api/pom.xml` e selecione **Add as Maven Project** (ou **Maven ➔ Reload Project**). |
| **Resetar completamente o banco de dados** | Deseja apagar todas as tabelas e dados armazenados no volume Docker. | Execute `docker compose down -v` e em seguida `docker compose up -d`. |
