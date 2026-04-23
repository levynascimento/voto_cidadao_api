# Urna Eletrônica - Backend (Spring Boot)

Implementação da **US01 - Inicializar a votação** do sistema de urna eletrônica.

## Objetivo da US01
Como operador credenciado pelo TSE, quero iniciar a eleição com uma senha, para permitir o início da votação de forma segura.

### Critérios de aceitação atendidos
- Solicitar senha
- Validar senha
- Iniciar votação apenas com senha válida
- Alterar estado para `EM_VOTACAO`

## Stack e decisões técnicas
- Java 17
- Spring Boot 3.3.x
- Spring Web
- Spring Data JPA
- Bean Validation
- PostgreSQL
- Docker Compose (para subir banco rapidamente)
- Arquitetura em camadas: Controller, Service, Repository, Entity/Model, DTO

## Estados da urna
- `AGUARDANDO_INICIO`
- `EM_VOTACAO`
- `FINALIZADA`

Ao iniciar a aplicação pela primeira vez, uma urna é criada automaticamente com estado inicial `AGUARDANDO_INICIO`.

## Estrutura de pacotes
```text
src/main/java/br/com/urnaeletronica
├── config
├── controller
├── dto
├── entity
├── enums
├── exception
├── repository
└── service
```

## Como executar

### 1) Subir PostgreSQL com Docker
No diretório do projeto:

```bash
docker compose up -d
```

Banco criado com:
- Database: `urna_eletronica`
- User: `urna_user`
- Password: `urna_pass`
- Port: `5432`

### 2) Rodar a aplicação
```bash
./mvnw spring-boot:run
```

A API estará disponível em:
- `http://localhost:8080`

## Configurações importantes
No `application.properties`:
- `urna.tse.senha=${URNA_TSE_SENHA:TSE2024}`

Você pode sobrescrever com variável de ambiente:
```bash
export URNA_TSE_SENHA="NOVA_SENHA"
```

## Endpoint da US01

### Inicializar votação
**POST** `/api/v1/votacao/inicializar`

#### Request body
```json
{
  "senha": "TSE2024"
}
```

#### Resposta de sucesso (200)
```json
{
  "mensagem": "Votação iniciada com sucesso",
  "estado": "EM_VOTACAO",
  "inicioVotacao": "2026-04-22T20:00:00"
}
```

#### Possíveis erros
- `400 Bad Request` - payload inválido (ex.: senha vazia)
- `403 Forbidden` - senha inválida
- `404 Not Found` - urna não encontrada
- `409 Conflict` - votação já iniciada ou finalizada
- `500 Internal Server Error` - erro não tratado

## Exemplo com curl
```bash
curl -X POST "http://localhost:8080/api/v1/votacao/inicializar" \
  -H "Content-Type: application/json" \
  -d '{"senha":"TSE2024"}'
```

## Próximas sprints (visão)
- Cadastro de candidatos e cargos (1 presidente, 2 deputados)
- Registro de eleitor por título (voto único)
- Recebimento de voto válido/branco/nulo
- Finalização da votação com senha
- Apuração e estatísticas finais
