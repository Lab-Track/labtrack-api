# LabTrack API

Backend do LabTrack — sistema de controle de emprestimo de equipamentos de laboratorio. API REST em Spring Boot, com schema versionado via Flyway em PostgreSQL.

## Stack

- Java 17
- Spring Boot 4.1.0 (Web, Data JPA, Security, Validation)
- Gradle (Gradle Wrapper incluido, nao precisa instalar Gradle)
- PostgreSQL 16 (via Docker Compose)
- Flyway (versionamento de schema)
- springdoc-openapi (Swagger UI)
- Lombok

## Estrutura de pacotes

```
src/main/java/com/labtrack/labtrack/
├── LabtrackApplication.java
├── config/          # configuracoes gerais do Spring
├── controller/      # endpoints REST (ainda nao implementados)
├── service/         # regras de negocio (ainda nao implementado)
├── repository/      # acesso a dados (ainda nao implementado)
├── model/           # entidades JPA (10 tabelas do dominio)
├── dto/             # objetos de transferencia de dados
├── exception/       # tratamento de erros
└── security/        # configuracao de seguranca (modo dev por enquanto)
```

O schema do banco fica em `src/main/resources/db/migration/` (Flyway) e o diagrama ER de referencia em `docs/LabTrack_DB.drawio`.

## Como rodar (do zero, sem passos manuais alem destes)

Pre-requisitos: JDK 17+ e Docker Desktop rodando.

```bash
docker compose up -d
./gradlew bootRun
```

A API sobe em `http://localhost:8080`. O Flyway aplica o schema automaticamente no start.

Swagger UI: http://localhost:8080/swagger-ui.html

## Rodar os testes

Com o Postgres do `docker compose up -d` no ar (o teste de integracao conecta nele):

```bash
./gradlew test
```

Isso inclui um teste de integracao que confirma que as 10 tabelas do dominio (professor, student, technician, project, equipment, status_history, loan, loan_item, loan_return, notification) existem apos a migration.

## Parar o banco de dados

```bash
docker compose down
```

Para remover tambem os dados persistidos:

```bash
docker compose down -v
```

## Seguranca

O `SecurityConfig` atual libera todas as requisicoes no profile `dev` (ativo por padrao) para nao travar o desenvolvimento dos endpoints. A logica de autenticacao/JWT ainda nao foi implementada — e escopo de uma task futura.
