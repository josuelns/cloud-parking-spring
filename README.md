# cloud-parking-spring

API REST para **controle de estacionamento**: grid de vagas (A1, B2…), check-in por placa + vaga, check-out com tarifa automática e liberação da vaga.

Parte da minha transição **Node.js → Java/Spring Boot**. Front-end companion: [parking-web-client](https://github.com/josuelns/parking-web-client)

## Stack

| Camada | Tecnologias |
|---|---|
| Runtime | Java 21, Spring Boot 4, Virtual Threads |
| API | REST, Bean Validation, MapStruct |
| Dados | Spring Data JPA, MySQL 8, Redis |
| Segurança | Spring Security + Keycloak (perfil `prod`) |
| Testes | JUnit 5, Mockito, H2 (testes) |
| Infra | Docker Compose |

## Destaques

- **Domínio rico**: sessões de estacionamento, grid configurável, tarifas no check-out
- **Clean Architecture**: `domain` → `application` → `infrastructure`
- **Consulta de veículos**: adapter mock ou API Brasil (configurável)
- **Testes automatizados**: use cases, domínio, utilitários e integração
- **Idempotência**: Redis para evitar operações duplicadas

## Arquitetura

```
domain/          → ParkingSpot, ParkingSession, regras de tarifa
application/     → ParkingOperationsUseCase, VehicleLookupUseCase
infrastructure/  → controllers REST, JPA, Redis, adapters externos
```

## Pré-requisitos

- Java 21+
- Docker e Docker Compose

## Como rodar

```bash
# 1. MySQL (porta 3307) + Redis (porta 6380)
docker compose up -d

# 2. API — perfil dev (sem auth)
./gradlew bootRun
```

| Serviço | URL / Porta |
|---|---|
| API | http://localhost:8080 |
| MySQL | `localhost:3307` — db `cloud_parking_db` |
| Redis | `localhost:6380` |

## Endpoints REST

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/spots` | Mapa do grid com status das vagas |
| `GET` | `/spots/available` | Vagas disponíveis |
| `GET` | `/vehicles/{plate}` | Dados do veículo por placa |
| `GET` | `/parking` | Sessões abertas |
| `GET` | `/parking/{id}` | Sessão por ID |
| `POST` | `/parking` | Check-in (placa + código da vaga) |
| `POST` | `/parking/{id}/exit` | Check-out com cálculo de tarifa |

### Exemplo — check-in

```json
POST /parking
{
  "plate": "ABC1D23",
  "spotCode": "A1"
}
```

## Testes

```bash
./gradlew test
```

Cobertura em use cases, domínio (`ParkingSession`, `ParkingSpot`), normalização de placas e grid.

## Projetos relacionados

- [parking-web-client](https://github.com/josuelns/parking-web-client) — interface React para operar o estacionamento
- [tasks-api-spring](https://github.com/josuelns/tasks-api-spring) — API Spring Boot com JPA + Flyway
- [auth-api-prisma](https://github.com/josuelns/auth-api-prisma) — API documentada com Swagger/OpenAPI (Node.js)

---

[Portfólio](https://josuelns.github.io/) · [GitHub](https://github.com/josuelns) · [LinkedIn](https://www.linkedin.com/in/josue-leandro-navarro)
