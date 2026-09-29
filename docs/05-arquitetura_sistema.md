# Arquitetura do Sistema

> Documento de arquitetura **planejada** do Sistema de Gestão de Imóveis. Todos os requisitos do levantamento ainda estão como "NÃO IMPLEMENTADO", então os nomes de pacotes, rotas e configurações abaixo são a proposta a ser seguida na implementação.

## 1. Estilo Arquitetural

API REST em camadas, com a seguinte estrutura de pacotes:

```
br.com.sistemaimoveis.api
├── config             → configurações (SecurityConfig, OpenApiConfig)
├── security           → JWT: geração/validação do token e filtro de autenticação
├── controller         → recebe requisições HTTP, valida entrada (DTO + Bean Validation), delega ao service
├── database
│   ├── entity         → entidades JPA (@Entity): Imovel, Usuario, Contato (+ Endereco @Embeddable)
│   └── repository     → interfaces Spring Data JPA (I-prefixadas: IImovelRepository, IUsuarioRepository, IContatoRepository)
├── dto                → objetos de entrada/saída da API (Request/Response)
├── enums              → TipoImovel, SituacaoImovel, Perfil
├── exception          → hierarquia de exceções de negócio + GlobalExceptionHandler
└── service            → regras de negócio e orquestração de transações
```

Fluxo de uma requisição:
```
Cliente HTTP → Filtro JWT → Controller → Service → Repository → Banco de Dados
                              ↑ DTO         ↑ Entity/Domain
```

- **Filtro JWT**: nas rotas `/admin/**`, valida o token antes de chegar ao controller. Rotas públicas passam direto.
- **Controller**: apenas orquestra. Recebe o DTO, chama o service, converte a resposta em DTO e define o status HTTP.
- **Service**: contém as regras de negócio (RN02, RN03, RN05, RN07 etc.) e orquestra as operações de CRUD.
- **Repository**: interfaces `JpaRepository<Entity, Long>`, com queries derivadas por nome de método e consultas com filtros (contatos por imóvel e por data).
- **DTO**: desacopla o contrato da API do modelo de persistência. A entidade nunca é devolvida direto no controller, e o `Usuario` nunca expõe a senha.

## 2. Rotas da API

| Método | Rota | Acesso | Caso de uso |
|--------|------|--------|-------------|
| GET | `/api/v1/imoveis` | Público | UC01 (paginação e ordenação) |
| GET | `/api/v1/imoveis/{id}` | Público | UC02 |
| GET | `/api/v1/imoveis?titulo=` | Público | UC02 (busca por título) |
| POST | `/api/v1/contatos` | Público | UC03 |
| POST | `/api/v1/auth/login` | Público | UC04 |
| POST | `/api/v1/admin/imoveis` | Admin | UC05 |
| PUT | `/api/v1/admin/imoveis/{id}` | Admin | UC06 |
| DELETE | `/api/v1/admin/imoveis/{id}` | Admin | UC07 |
| GET | `/api/v1/admin/contatos` | Admin | UC08 (filtro por imóvel ou data) |
| GET | `/api/v1/admin/contatos/{id}` | Admin | UC09 |
| POST | `/api/v1/admin/usuarios` | Admin | UC10 |

Não existe rota pública de cadastro de usuários (RF11). O primeiro admin nasce da migration de seed (RF10).

## 3. Segurança (JWT)

- Autenticação **stateless** com JWT e tempo de expiração definido (RNF09). O servidor não guarda sessão.
- No Spring Boot 2.7 (Spring Security 5.7), a configuração é feita com um bean `SecurityFilterChain`, e não com `WebSecurityConfigurerAdapter` (obsoleto).
- Regras de acesso:
  - Públicas: `GET /imoveis/**`, `POST /contatos`, `POST /auth/login`, Swagger (`/swagger-ui/**`, `/v3/api-docs/**`) e `/actuator/health`.
  - Protegidas: `/admin/**` exige token válido e perfil `ADMIN`.
- Senhas guardadas com hash **BCrypt** (`BCryptPasswordEncoder`), com mínimo de 8 caracteres (RN06).
- **401 x 403 (RF06):** o Spring Security, por padrão, pode devolver `403` para quem não enviou token. É preciso configurar um `AuthenticationEntryPoint` que devolva `401` (sem token ou token inválido) e um `AccessDeniedHandler` que devolva `403` (autenticado, mas sem permissão).
- **Segredo e expiração do JWT** vêm de variáveis de ambiente (`JWT_SECRET`, `JWT_EXPIRATION`), nunca do código (RNF10).
- **Login (RN09):** a mensagem de erro é sempre "credenciais inválidas", seja e-mail inexistente, senha errada ou admin desativado.

## 4. Tratamento de Erros

`GlobalExceptionHandler` (`@RestControllerAdvice`) centraliza o mapeamento de exceções de negócio para respostas HTTP padronizadas. Todas as exceções de negócio estendem `NegocioException` (abstrata), que carrega o `HttpStatus` e o rótulo de `error`. Isso permite que um único `@ExceptionHandler` trate todas elas de forma genérica.

| Exceção | Status HTTP | Usada em |
|---------|-------------|----------|
| `RecursoNaoEncontradoException` | 404 | Imóvel ou contato inexistente (UC02, UC06, UC07, UC09) |
| `RegistroDuplicadoException` | 409 | E-mail de usuário já cadastrado (RN05, UC10) |
| `ValidacaoNegocioException` | 400 | Terreno com quartos, banheiros ou vagas (RN03) |
| `OperacaoNaoPermitidaException` | 409 | Admin tentando excluir/desativar a si mesmo ou o último admin ativo (RN07) |
| `CredenciaisInvalidasException` | 401 | Login com credenciais inválidas (RN09) |

Erros que não passam pelas exceções acima:

| Situação | Status HTTP | Tratado por |
|----------|-------------|-------------|
| Campos inválidos no DTO (RN01, RN02, RN06) | 400 | `MethodArgumentNotValidException`, com a lista em `fieldErrors` |
| Sem token ou token inválido | 401 | `AuthenticationEntryPoint` |
| Autenticado sem permissão | 403 | `AccessDeniedHandler` |

Resposta de erro padrão (formato `ErrorResponseDTO`):
```json
{
  "timestamp": "2026-09-29T14:32:00.123-03:00",
  "status": 409,
  "error": "Registro duplicado",
  "message": "Já existe um usuário cadastrado com o e-mail informado",
  "path": "/api/v1/admin/usuarios",
  "fieldErrors": []
}
```

Exemplo com erro de validação (RN02):
```json
{
  "timestamp": "2026-09-29T14:35:10.456-03:00",
  "status": 400,
  "error": "Requisição inválida",
  "message": "Erro de validação nos campos informados",
  "path": "/api/v1/admin/imoveis",
  "fieldErrors": [
    { "field": "preco", "message": "deve ser maior que zero" }
  ]
}
```

## 5. Persistência e Migrations

- Spring Data JPA/Hibernate sobre MySQL (perfil `dev`) ou H2 (perfil `test`).
- Flyway gerencia o versionamento do schema em `resources/db/migration`:
  - `V1__create_schema.sql`: tabelas `imovel`, `usuario` e `contato`.
  - `V2__seed_admin_inicial.sql`: primeiro administrador, com dados vindos de variáveis de ambiente (RF10, RNF10).
- `spring.jpa.hibernate.ddl-auto=validate` em ambos os perfis. O schema é fonte de verdade via Flyway, não o Hibernate auto-DDL.
- `spring.jpa.open-in-view=false`: evita lazy loading fora da transação. Como `Contato.imovel` é `LAZY`, a conversão para DTO deve acontecer dentro do service.
- Listagens usam `Pageable` do Spring Data (`Page<Imovel>`), o que entrega paginação e ordenação (RF02).

## 6. Containerização (Docker)

Configuração planejada (`docker-compose.yml` e `Dockerfile` na raiz):

```yaml
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: ${MYSQL_DATABASE:-sistema_imoveis}
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-root}
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD-SHELL", "mysqladmin ping -h 127.0.0.1 -P 3306 -uroot -p$${MYSQL_ROOT_PASSWORD} --silent"]
      interval: 5s
      timeout: 5s
      retries: 20
      start_period: 10s

  app:
    build: .
    depends_on:
      mysql:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_DATASOURCE_URL: ${DATABASE_URL:-jdbc:mysql://mysql:3306/sistema_imoveis}
      SPRING_DATASOURCE_USERNAME: ${DATABASE_USERNAME:-root}
      SPRING_DATASOURCE_PASSWORD: ${DATABASE_PASSWORD:-root}
      JWT_SECRET: ${JWT_SECRET}
      JWT_EXPIRATION: ${JWT_EXPIRATION:-3600000}
      ADMIN_NOME: ${ADMIN_NOME}
      ADMIN_EMAIL: ${ADMIN_EMAIL}
      ADMIN_SENHA_HASH: ${ADMIN_SENHA_HASH}
    ports:
      - "8080:8080"

volumes:
  mysql_data:
```

```dockerfile
FROM maven:3.9-eclipse-temurin-8 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:8-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

As variáveis de ambiente têm um exemplo em `.env.example` na raiz do projeto. Copie para `.env` antes de rodar `docker compose up`:

```properties
MYSQL_DATABASE=sistema_imoveis
MYSQL_ROOT_PASSWORD=troque-esta-senha
DATABASE_URL=jdbc:mysql://mysql:3306/sistema_imoveis
DATABASE_USERNAME=root
DATABASE_PASSWORD=troque-esta-senha
JWT_SECRET=troque-por-um-segredo-longo-e-aleatorio
JWT_EXPIRATION=3600000
ADMIN_NOME=Administrador
ADMIN_EMAIL=admin@exemplo.com
ADMIN_SENHA_HASH='$2a$10$troque-pelo-hash-bcrypt-gerado'
```

> **Atenção com o hash BCrypt:** ele contém o caractere `$`, que o Docker Compose tenta interpretar como variável. Por isso o valor de `ADMIN_SENHA_HASH` fica entre **aspas simples** no `.env`. O arquivo `.env` real não deve ir para o Git (adicione ao `.gitignore`); só o `.env.example`, com valores fictícios, é versionado.

## 7. Documentação da API

- Dependência `org.springdoc:springdoc-openapi-ui:1.7.0` no `pom.xml` (compatível com Spring Boot 2.7 / Java 8; a versão 2.x do springdoc exige Spring Boot 3).
- Expõe `/swagger-ui.html` e `/v3/api-docs` automaticamente a partir dos `@RestController` e DTOs.
- Para testar as rotas `/admin/**` pelo Swagger, declarar o esquema de segurança **Bearer JWT** (`@SecurityScheme`), que habilita o botão "Authorize".
- **Pendência (RF12):** o Swagger gerado é o padrão, a partir da introspecção dos controllers e DTOs. Falta enriquecer com `@Operation`, `@ApiResponse` e exemplos, para documentar as regras de negócio e os códigos de erro.

## 8. Testes (RNF11)

Testes automatizados nos principais fluxos:
- **Login:** sucesso, senha incorreta, e-mail inexistente (mesma mensagem), admin desativado.
- **Acesso negado:** rota `/admin/**` sem token (`401`) e com perfil sem permissão (`403`).
- **CRUD de imóveis:** criação, atualização, exclusão, listagem paginada, RN02 e RN03.
- **Envio de contato:** dados válidos, e-mail ou telefone inválido, imóvel inexistente.

Ferramentas: `spring-boot-starter-test` e `spring-security-test`, com o perfil `test` usando H2 e o mesmo script Flyway.

## 9. Resumo das Dependências Principais (`pom.xml`)

- `spring-boot-starter-parent` — `2.7.18`
- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- `spring-boot-starter-security`
- `spring-boot-starter-actuator` — expõe `/actuator/health`
- `spring-boot-starter-test` e `spring-security-test` (escopo `test`)
- `io.jsonwebtoken:jjwt-api`, `jjwt-impl` e `jjwt-jackson` — `0.11.5` (geração e validação do JWT)
- `mysql-connector-j` (escopo `runtime`)
- `com.h2database:h2` (escopo `runtime`, usado no perfil `test`)
- `org.flywaydb:flyway-core` + `org.flywaydb:flyway-mysql`
- `org.springdoc:springdoc-openapi-ui:1.7.0`
- `org.projectlombok:lombok`