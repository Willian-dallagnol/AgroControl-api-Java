# AgroControl API

API REST de gestão agrícola construída com **Java 21 + Spring Boot 3**. Organiza **Fazendas → Talhões → Culturas** por usuário, com autenticação JWT, controle de acesso por papel e **isolamento total de dados entre usuários**. É a reimplementação em Java/Spring da AgroControl originalmente escrita em Go.

---

## Visão Geral

O AgroControl resolve um problema real do agronegócio: organizar a hierarquia produtiva de uma propriedade rural. Cada usuário gerencia apenas os próprios dados — um usuário nunca enxerga ou acessa recursos de outro.

Funcionalidades principais:

- Registro e autenticação de usuários (JWT)
- CRUD de Fazendas, Talhões e Culturas
- Isolamento multiusuário em todas as consultas
- Regra de negócio: a soma das áreas dos talhões não pode exceder a área total da fazenda
- Paginação, filtros e ordenação nas listagens
- Documentação interativa via Swagger UI

---

## Arquitetura

Arquitetura em camadas, com responsabilidades bem separadas:

```
Controller   →  recebe HTTP, valida entrada (DTO), delega ao Service
    ↓
Service      →  regras de negócio, transações, orquestração
    ↓
Repository   →  acesso a dados (Spring Data JPA), consultas escopadas pelo dono
    ↓
PostgreSQL
```

Camadas de apoio: `domain` (entidades JPA), `dto` (records de entrada/saída), `mapper` (MapStruct), `security` (JWT + Spring Security), `exception` (handler global), `validation` (constraints customizadas), `config` (OpenAPI, propriedades).

**Por que camadas e não Hexagonal/Clean completa?** Para o escopo deste projeto (CRUD com regras de negócio pontuais), a arquitetura em camadas entrega a separação de responsabilidades necessária sem o custo de indireção de portas/adaptadores e casos de uso. Seguindo KISS e YAGNI, a complexidade só seria justificada por requisitos que o projeto não tem.

---

## Tecnologias

| Camada         | Tecnologia                                  |
|----------------|---------------------------------------------|
| Linguagem      | Java 21                                     |
| Framework      | Spring Boot 3.3                             |
| Persistência   | Spring Data JPA / Hibernate                 |
| Banco          | PostgreSQL 16                               |
| Migrations     | Flyway                                      |
| Segurança      | Spring Security + JWT (jjwt)                |
| Validação      | Bean Validation (Hibernate Validator)       |
| Mapeamento     | MapStruct                                   |
| Boilerplate    | Lombok                                      |
| Documentação   | springdoc-openapi (Swagger UI)             |
| Observabilidade| Actuator + Micrometer/Prometheus            |
| Testes         | JUnit 5, Mockito, Testcontainers, MockMvc   |
| Cobertura      | JaCoCo                                       |
| Build          | Maven                                       |
| CI             | GitHub Actions                              |

---

## Segurança

- **Isolamento multiusuário:** toda consulta é filtrada pelo `user.id` do token. Acesso a recurso de outro usuário retorna **404** (não vaza existência).
- **Senhas:** armazenadas com **BCrypt**.
- **Controle de acesso por papel:** `@PreAuthorize` (ex.: remover fazenda exige `ADMIN` ou `MANAGER`).
- **Rate limiting:** endpoints de autenticação limitados por IP (mitiga brute force), com limpeza automática das entradas em memória.
- **JWT secret:** em produção (profile `prod`), a aplicação **não sobe** com segredo vazio, fraco ou default.
- **Sessão stateless:** sem estado de sessão no servidor; CSRF desabilitado por ser uma API stateless.
- **Logs de segurança:** falhas de autenticação registram IP e motivo (nunca senhas ou tokens).

---

## Autenticação JWT

Fluxo:

1. `POST /api/v1/auth/register` ou `POST /api/v1/auth/login` → retorna um **access token** (HS256).
2. O cliente envia `Authorization: Bearer <token>` nas requisições seguintes.
3. Um filtro (`JwtAuthenticationFilter`) valida o token, confere se a conta está ativa e popula o contexto de segurança.
4. `GET /api/v1/auth/me` retorna o usuário dono do token.

> Próximo passo planejado: **refresh token** com rotação e revogação (ver "Próximos Passos").

---

## Banco de Dados

- Schema versionado por **Flyway** (`src/main/resources/db/migration`). O Hibernate roda em `ddl-auto: validate` — nunca altera DDL em runtime.
- **Índices** (todos os campos de busca e relacionamento estão cobertos):
  - `users.email` → índice via constraint `UNIQUE` (usado no login).
  - `farms.user_id` e `(user_id, name)` → listagem e filtro por nome.
  - `fields.farm_id` → relacionamento e listagem.
  - `crops.field_id` → relacionamento e listagem.
- Constraints de integridade: `CHECK` de área positiva e de valores de enum (status/role).

---

## Observabilidade

- **Actuator** expõe apenas `health`, `info` e `prometheus` (exposição mínima por segurança — nunca `*`).
- **Métricas Prometheus** em `/actuator/prometheus`.
- **Logs estruturados (parametrizados):** criação, atualização e remoção de recursos registram `id` e `userId`; autenticação registra eventos de login.

---

## Como Executar

### Pré-requisitos
- Java 21 (JDK)
- Maven 3.9+
- Docker (para o PostgreSQL e para os testes de integração com Testcontainers)

### Subir tudo com Docker Compose
```bash
docker compose up --build
```
A API sobe em `http://localhost:8080`. Swagger UI: `http://localhost:8080/swagger-ui.html`.

### Rodar localmente (banco via Docker, app via Maven)
```bash
docker compose up -d db
./mvnw spring-boot:run
```

### Rodar os testes (com cobertura)
```bash
./mvnw verify
```
Relatório JaCoCo: `target/site/jacoco/index.html`.

### Variáveis de ambiente relevantes
| Variável               | Default (dev)              | Observação                                  |
|------------------------|----------------------------|---------------------------------------------|
| `JWT_SECRET`           | valor de dev no yml        | **Obrigatório e forte em produção**         |
| `JWT_EXPIRATION_MS`    | 3600000 (1h)               |                                             |
| `CORS_ALLOWED_ORIGINS` | http://localhost:4200      | Origens do frontend (ex.: Angular)          |
| `DB_HOST/PORT/NAME/...`| localhost/5432/agrocontrol |                                             |

> Em produção, suba com `SPRING_PROFILES_ACTIVE=prod` para ativar a validação do JWT secret.

---

## Próximos Passos

- **Refresh token** com rotação e revogação.
- **Frontend Angular** mínimo consumindo a API (login → `/me` → listagem).
- Auditoria de entidades (`createdBy`/`updatedBy`) via `AuditorAware`, se houver requisito de compliance.

Itens conscientemente **fora de escopo** por ora (YAGNI): Redis/cache distribuído, mensageria, microsserviços e arquiteturas hexagonal/CQRS — não há requisito que os justifique no estágio atual, e adicioná-los aumentaria a complexidade sem ganho real.
