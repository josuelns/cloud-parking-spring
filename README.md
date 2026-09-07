# CloudParking

Sistema de gerenciamento de estacionamento desenvolvido em **Java 21** com **Spring Boot 4**. Permite controlar vagas em um grid, registrar entradas e saídas de veículos, calcular tarifas e consultar dados de placas via API externa ou mock.

---

## Sumário

- [Visão geral](#visão-geral)
- [Funcionalidades](#funcionalidades)
- [Stack tecnológica](#stack-tecnológica)
- [Arquitetura](#arquitetura)
- [Estrutura do projeto](#estrutura-do projeto)
- [Pré-requisitos](#pré-requisitos)
- [Como executar](#como-executar)
- [Configuração](#configuração)
- [API REST](#api-rest)
- [Regras de negócio](#regras-de-negócio)
- [Concorrência e idempotência](#concorrência-e-idempotência)
- [Testes](#testes)
- [Perfis de execução](#perfis-de-execução)

---

## Visão geral

O **CloudParking** simula a operação de um estacionamento com vagas numeradas em formato de grid (`A1`, `A2`, `B1`…). O operador registra a **entrada** (check-in) informando placa e vaga, e a **saída** (check-out) quando o veículo deixa o estacionamento — momento em que o sistema calcula a tarifa e libera a vaga.

O projeto foi estruturado em camadas inspiradas em **Clean Architecture / Hexagonal Architecture**, separando regras de negócio, casos de uso e detalhes de infraestrutura (banco, Redis, APIs externas).

---

## Funcionalidades

| Funcionalidade | Descrição |
|----------------|-----------|
| **Check-in** | Registra entrada de veículo em uma vaga livre |
| **Check-out** | Encerra sessão, calcula tarifa e libera vaga |
| **Mapa de vagas** | Retorna grid completo com status e placas ocupadas |
| **Vagas disponíveis** | Lista apenas vagas livres |
| **Consulta de veículo** | Busca marca, modelo e cor por placa (mock ou API Brasil) |
| **Idempotência** | Evita processamento duplicado via Redis |
| **Lock pessimista** | Protege vagas contra race condition no MySQL |

---

## Stack tecnológica

| Tecnologia | Uso |
|------------|-----|
| Java 21 | Linguagem (records, virtual threads) |
| Spring Boot 4.1.1 | Framework principal |
| Spring Data JPA | Persistência com Hibernate |
| MySQL 8 | Banco de dados relacional |
| Redis 7 | Idempotência de operações |
| Spring Security | Autenticação (JWT em prod, liberado em dev) |
| MapStruct | Mapeamento entidade → DTO |
| Lombok | Redução de boilerplate |
| Docker Compose | MySQL e Redis locais |
| JUnit 5 + Mockito | Testes unitários |

---

## Arquitetura

```
┌─────────────────────────────────────────────────────────┐
│  infrastructure/http          Controllers REST          │
├─────────────────────────────────────────────────────────┤
│  application                  Casos de uso                │
├─────────────────────────────────────────────────────────┤
│  domain                       Entidades + regras puras    │
├─────────────────────────────────────────────────────────┤
│  infrastructure               DB, Redis, adapters, config │
└─────────────────────────────────────────────────────────┘
```

### Camadas

| Camada | Pacote | Responsabilidade |
|--------|--------|------------------|
| **Domain** | `domain/` | Entidades (`ParkingSpot`, `ParkingSession`), enums, regras puras (`ParkingCheckOut`), portas (`VehicleLookupPort`) |
| **Application** | `application/` | Orquestração: `ParkingOperationsUseCase`, `VehicleLookupUseCase` |
| **Infrastructure** | `infrastructure/` | Controllers, repositórios JPA, Redis, adapters, segurança, mappers |

### Fluxo de uma requisição (check-in)

```
Cliente HTTP
    │
    ▼  POST /parking  { "plate": "ABC1D23", "spotCode": "A1" }
ParkingController
    │
    ▼
ParkingOperationsUseCase.checkIn()
    ├── IdempotencyService (Redis SETNX)
    ├── ParkingSessionRepository (verifica sessão aberta)
    ├── ParkingSpotRepository (SELECT FOR UPDATE)
    ├── VehicleLookupUseCase → MockVehicleLookupAdapter
    └── Persiste sessão + ocupa vaga
    │
    ▼  HTTP 201  ParkingSessionResponse
```

---

## Estrutura do projeto

```
src/main/java/josue/CloudParking/
├── CloudParkingApplication.java          # Ponto de entrada
├── domain/
│   ├── ParkingSpot.java                  # Entidade vaga
│   ├── ParkingSession.java               # Entidade sessão
│   ├── VehicleInfo.java                  # Record dados do veículo
│   ├── SpotStatus.java                   # Enum FREE / OCCUPIED
│   ├── service/ParkingCheckOut.java      # Cálculo de tarifa
│   └── port/VehicleLookupPort.java       # Interface consulta placa
├── application/
│   ├── ParkingOperationsUseCase.java     # Check-in, check-out, consultas
│   └── VehicleLookupUseCase.java         # Consulta veículo
└── infrastructure/
    ├── http/                             # Controllers REST
    ├── http/request/                     # DTOs de entrada
    ├── http/response/                    # DTOs de saída
    ├── mapper/                           # MapStruct mappers
    ├── persistence/                      # Repositórios JPA
    ├── adapter/                          # Mock e API Brasil
    ├── config/                           # Segurança, grid, inicialização
    ├── service/IdempotencyService.java  # Lock Redis
    ├── exception/                        # Exceções + handler global
    └── util/PlateNormalizer.java         # Normalização de placas

src/test/java/                            # Testes unitários (37 testes)
src/main/resources/
├── application.yml                       # Configuração principal
└── application.properties                # Tokens API Brasil
compose.yml                               # Docker: MySQL + Redis
```

---

## Pré-requisitos

- **Java 21** (JDK)
- **Docker** e **Docker Compose** (para MySQL e Redis locais)
- Ou MySQL na porta `3307` e Redis na porta `6380` configurados manualmente

---

## Como executar

### 1. Subir dependências (MySQL + Redis)

```bash
docker compose up -d
```

### 2. Executar a aplicação

```bash
# Windows
.\gradlew.bat bootRun

# Linux/macOS
./gradlew bootRun
```

A aplicação sobe em **http://localhost:8080** (porta padrão Spring Boot).

> No perfil `dev`, o Spring Boot inicia automaticamente o Docker Compose (`lifecycle-management: start-only`).

### 3. Build e testes

```bash
.\gradlew.bat build      # Compila + roda testes
.\gradlew.bat test       # Apenas testes
```

Relatório de testes: `build/reports/tests/test/index.html`

---

## Configuração

Arquivo principal: `src/main/resources/application.yml`

```yaml
cloud-parking:
  grid:
    total-spots: 20          # Número total de vagas (grid 4×5)
  vehicle-lookup:
    provider: mock           # mock | apibrasil
    apibrasil:
      base-url: https://gateway.apibrasil.io
      device-token: ""
      bearer-token: ""
```

### Variáveis de ambiente (API Brasil)

Definidas em `application.properties`:

| Variável | Descrição |
|----------|-----------|
| `APIBRASIL_DEVICE_TOKEN` | Token do dispositivo |
| `APIBRASIL_BEARER_TOKEN` | Bearer token da API |

Para usar a API real, altere `provider` para `apibrasil` e configure os tokens.

### Grid de vagas

- Configurável via `total-spots` ou `rows` + `columns`
- Na subida, `ParkingSpotInitializer` cria vagas `A1`, `A2`… se não existirem
- Exemplo: 20 vagas → grid **4 linhas × 5 colunas**

---

## API REST

Base URL: `http://localhost:8080`

### Estacionamento — `/parking`

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/parking` | Lista sessões abertas |
| `GET` | `/parking/{id}` | Busca sessão por ID |
| `POST` | `/parking` | Check-in (entrada) |
| `POST` | `/parking/{id}/exit` | Check-out (saída) |

**Check-in — request:**

```json
POST /parking
{
  "plate": "ABC1D23",
  "spotCode": "A1"
}
```

**Check-in — response (201 Created):**

```json
{
  "id": "a1b2c3d4e5f6...",
  "spotCode": "A1",
  "license": "ABC1D23",
  "state": "SP",
  "model": "Uno",
  "color": "Branco",
  "entryDate": "07/09/2026 15:30"
}
```

**Check-out — response:**

```json
{
  "id": "a1b2c3d4e5f6...",
  "spotCode": "A1",
  "license": "ABC1D23",
  "entryDate": "07/09/2026 15:30",
  "exitDate": "07/09/2026 18:00",
  "bill": 9.0
}
```

### Vagas — `/spots`

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/spots` | Mapa completo (rows, columns, vagas + placas) |
| `GET` | `/spots/available` | Apenas vagas livres |

**Mapa — response:**

```json
{
  "rows": 4,
  "columns": 5,
  "spots": [
    { "code": "A1", "status": "OCCUPIED", "license": "ABC1D23" },
    { "code": "A2", "status": "FREE", "license": null }
  ]
}
```

### Veículos — `/vehicles`

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/vehicles/{plate}` | Consulta dados do veículo por placa |

**Placas mock disponíveis (provider `mock`):**

| Placa | Marca | Modelo |
|-------|-------|--------|
| ABC1D23 | Fiat | Uno |
| XYZ9K88 | Volkswagen | Gol |
| DEF4G56 | Chevrolet | Onix |

### Códigos HTTP de erro

| HTTP | Exceção | Quando |
|------|---------|--------|
| 400 | `IllegalArgumentException` | Placa/vaga inválida, parâmetros ausentes |
| 404 | `ParkingNotFoundException` | Sessão ou veículo não encontrado |
| 409 | `DuplicateOperationException` | Operação duplicada ou placa já estacionada |
| 409 | `SpotNotAvailableException` | Vaga ocupada |
| 409 | `SessionAlreadyClosedException` | Checkout em sessão já encerrada |

---

## Regras de negócio

### Check-in

1. Placa é normalizada (`abc-1d23` → `ABC1D23`)
2. Uma placa **não pode** ter mais de uma sessão aberta
3. Vaga deve existir e estar **FREE**
4. Dados do veículo são consultados (mock ou API); se não encontrado, usa valores padrão
5. Vaga passa para **OCCUPIED** e sessão é criada

### Check-out

1. Sessão deve existir e estar aberta (`exitDate == null`)
2. Tarifa é calculada com base no tempo de permanência
3. Vaga é liberada (**FREE**) e sessão é encerrada

### Cálculo de tarifa (`ParkingCheckOut`)

| Tempo de permanência | Regra | Exemplo |
|---------------------|-------|---------|
| Até 1 hora | Tarifa fixa **R$ 5,00** | 30 min → R$ 5,00 |
| 1h a 24h | R$ 5,00 + **R$ 2,00/hora** extra | 2h30 → R$ 9,00 |
| Acima de 24h | **R$ 20,00/dia** | 25h → R$ 20,00 |

---

## Concorrência e idempotência

O sistema usa **duas camadas** de proteção:

### 1. Redis — IdempotencyService

- Operação `SETNX` com TTL de **15 segundos**
- Evita que a mesma requisição seja processada duas vezes (retry de rede, duplo clique)
- Em caso de **falha**, o lock é liberado para permitir nova tentativa
- Em caso de **sucesso**, o TTL expira naturalmente

```
Chave: cloudparking:idempotency:{userId}:{operacao}:{params}
Exemplo: cloudparking:idempotency:anonymous:checkin:ABC1D23:A1
```

### 2. MySQL — Lock pessimista

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<ParkingSpot> findByCodeForUpdate(String code);
```

Traduz para `SELECT ... FOR UPDATE`, garantindo que duas threads não ocupem a mesma vaga simultaneamente.

---

## Testes

O projeto possui **37 testes unitários** organizados por camada:

| Classe de teste | Cobertura |
|-----------------|-----------|
| `ParkingOperationsUseCaseTest` | Check-in, check-out, erros, idempotência |
| `VehicleLookupUseCaseTest` | Normalização e delegação ao port |
| `PlateNormalizerTest` | Validação e normalização de placas |
| `IdempotencyServiceTest` | Acquire/release lock Redis |
| `ParkingCheckOutTest` | Cálculo de tarifas |
| `ParkingGridCalculatorTest` | Dimensões do grid |
| `ParkingSpotTest` / `ParkingSessionTest` | Comportamento das entidades |
| `CloudParkingApplicationTests` | Contexto Spring sobe corretamente |

```bash
.\gradlew.bat test
```

---

## Perfis de execução

| Perfil | Segurança | Banco | Uso |
|--------|-----------|-------|-----|
| **dev** (padrão) | Tudo liberado (`permitAll`) | MySQL via Docker (3307) | Desenvolvimento local |
| **prod** | JWT OAuth2 obrigatório | MySQL | Produção com Keycloak |
| **test** | — | H2 em memória | Testes automatizados |

Para ativar outro perfil:

```bash
.\gradlew.bat bootRun --args='--spring.profiles.active=prod'
```

---

## Exemplo de fluxo completo

```bash
# 1. Consultar mapa de vagas
curl http://localhost:8080/spots

# 2. Check-in
curl -X POST http://localhost:8080/parking \
  -H "Content-Type: application/json" \
  -d '{"plate":"ABC1D23","spotCode":"A1"}'

# 3. Listar sessões abertas
curl http://localhost:8080/parking

# 4. Check-out (substitua {id} pelo ID retornado no check-in)
curl -X POST http://localhost:8080/parking/{id}/exit

# 5. Consultar veículo
curl http://localhost:8080/vehicles/ABC1D23
```

---

## Licença

Projeto educacional — uso livre para estudo e referência.
