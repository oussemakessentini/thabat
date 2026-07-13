# ADR-001: Use a Microservice Architecture

## Status

Accepted

## Context

Thabat could initially be implemented as a modular monolith because its first
version has a limited number of business domains.

However, the project also has an educational and portfolio objective. It should
provide practical experience with:

- Distributed systems
- Spring Cloud
- Service discovery
- API gateways
- Kafka
- Independent databases
- Resilience patterns
- Centralized logging
- Metrics
- Distributed tracing
- Containerization
- CI/CD

## Decision

Thabat will use a small microservice architecture.

The initial business services are:

- Identity Service
- Prayer Service
- Quran Service

The initial platform services are:

- Discovery Service
- Config Service
- Gateway Service

Notification and analytics services will be introduced after the initial
business flow works.

## Consequences

### Benefits

- Independent service ownership
- Clear business boundaries
- Real microservice experience
- Independent database migrations
- Event-driven communication practice
- Better interview and portfolio value

### Costs

- More configuration
- More local resource usage
- Distributed debugging complexity
- Eventual consistency
- Additional testing requirements
- More deployment infrastructure

## Guardrails

- Do not create unnecessary services.
- Do not share databases.
- Do not introduce Kafka until the first synchronous flow works.
- Do not introduce Kubernetes before Docker Compose is stable.
- Keep the first release achievable.