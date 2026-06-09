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

## Endpoints da API

### 1. Inicializar votação
> Como operador do TSE, inicio a eleição com senha para permitir a votação.

**POST** `/api/v1/votacao/inicializar`

**Request body**
```json
{
  "senha": "TSE2024"
}
```

**Resposta de sucesso (200)**
```json
{
  "mensagem": "Votação iniciada com sucesso",
  "estado": "EM_VOTACAO",
  "inicioVotacao": "2026-06-09T20:00:00"
}
```

**Possíveis erros**
| Status | Descrição |
|--------|-----------|
| `400`  | Payload inválido (senha vazia) |
| `403`  | Senha inválida (`SenhaInvalidaException`) |
| `404`  | Urna não encontrada (`RecursoNaoEncontradoException`) |
| `409`  | Votação já iniciada ou finalizada (`EstadoUrnaInvalidoException`) |

---

### 2. Encerrar votação
> Como operador do TSE, encerro a eleição com senha.

**POST** `/api/v1/votacao/encerrar`

**Request body**
```json
{
  "senha": "TSE2024"
}
```

**Resposta de sucesso (200)**
```json
{
  "mensagem": "Votação finalizada com sucesso.",
  "estado": "FINALIZADA",
  "fimVotacao": "2026-06-09T20:00:00"
}
```

**Possíveis erros**
| Status | Descrição |
|--------|-----------|
| `400`  | Payload inválido (senha vazia) |
| `403`  | Senha inválida (`SenhaInvalidaException`) |
| `404`  | Urna não encontrada (`RecursoNaoEncontradoException`) |
| `409`  | Votação não iniciada ou já finalizada (`EstadoUrnaInvalidoException`) |

---

### 3. Identificar eleitor
> Como mesário, identifico o eleitor pelo título para liberar o voto.

**POST** `/api/v1/eleitor/identificar`

**Request body**
```json
{
  "tituloEleitor": "10003"
}
```

**Resposta de sucesso (200)**
```json
{
  "id": 1,
  "tituloEleitor": "10003",
  "nome": "João da Silva",
  "cpf": "12345678901",
  "jaVotou": false
}
```

**Possíveis erros**
| Status | Descrição |
|--------|-----------|
| `400`  | Payload inválido (título vazio) |
| `404`  | Eleitor não encontrado (`RecursoNaoEncontradoException`) |

---

### 4. Votar
> Como eleitor, registro meus votos para os cargos em disputa.

**POST** `/api/v1/voto/votar`

**Campos do `VotoRequest` (dentro de `votosEleitor`)**
| Campo | Tipo | Obrigatório | Descrição |
|-------|------|:---:|---|
| `cargoID` | Long | ✅ | ID do cargo que está sendo votado |
| `numeroCandidato` | Long | ❌ | Número do candidato (ignorado para BRANCO/NULO) |
| `tipoVoto` | Enum | ✅ | `NORMAL`, `BRANCO` ou `NULO` (default: `NORMAL`) |

**Exemplo 1 — Voto normal em candidato**

Request:
```json
{
  "tituloEleitor": "10003",
  "votosEleitor": [
    {
      "cargoID": 1,
      "numeroCandidato": 10,
      "tipoVoto": "NORMAL"
    }
  ]
}
```

Response (200):
```json
{
  "mensagem": "Votação computada com sucesso!",
  "nomeEleitor": "João da Silva",
  "dataVotacao": "2026-06-09T20:00:00"
}
```

**Exemplo 2 — Voto em branco e nulo**

Request:
```json
{
  "tituloEleitor": "10002",
  "votosEleitor": [
    {
      "cargoID": 1,
      "tipoVoto": "BRANCO"
    },
    {
      "cargoID": 3,
      "tipoVoto": "NULO"
    }
  ]
}
```

Response (200):
```json
{
  "mensagem": "Votação computada com sucesso!",
  "nomeEleitor": "Maria Souza",
  "dataVotacao": "2026-06-09T20:00:00"
}
```

**Exemplo 3 — Voto duplicado no mesmo cargo (ERRO)**

Request:
```json
{
  "tituloEleitor": "10001",
  "votosEleitor": [
    {
      "cargoID": 3,
      "numeroCandidato": 80,
      "tipoVoto": "NORMAL"
    },
    {
      "cargoID": 3,
      "numeroCandidato": 80,
      "tipoVoto": "NORMAL"
    }
  ]
}
```

Response (409):
```json
{
  "timestamp": "2026-06-09T20:00:00-03:00",
  "status": 409,
  "error": "Conflict",
  "message": "Voto duplicado detectado: O candidato ao cargo 3 de número 80 recebeu mais de um voto para o mesmo cargo.",
  "path": "/api/v1/voto/votar",
  "details": []
}
```

**Possíveis erros**
| Status | Descrição |
|--------|-----------|
| `400`  | Payload inválido (título vazio, cargoID ausente) |
| `404`  | Eleitor não encontrado (`RecursoNaoEncontradoException`) |
| `409`  | Eleitor já votou (`JaVotouException`) |
| `409`  | Fraude na votação: voto duplicado, candidato não concorre ao cargo, limite de votos por cargo excedido (`FraudeNaVotacaoException`) |

## Próximas sprints (visão)
- Cadastro de candidatos e cargos (1 presidente, 2 deputados)
- Registro de eleitor por título (voto único)
- Recebimento de voto válido/branco/nulo
- Finalização da votação com senha
- Apuração e estatísticas finais
