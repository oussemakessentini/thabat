# Thabat System Architecture

## 1. Overview

Thabat is a global mobile platform designed to help Muslims:

- Track missed prayers and recovery progress
- Track Quran memorization and revision
- Build consistent worship habits
- Participate in optional challenges and leaderboards
- Receive reminders and motivational notifications

The backend uses a microservice architecture to provide independent deployment,
clear domain boundaries, scalability, fault isolation, and practical experience
with distributed systems.

---

## 2. High-Level Architecture

```text
Mobile Application                 Admin Dashboard
React Native + Expo                React + TypeScript
         |                                  |
         +---------------+------------------+
                         |
                    API Gateway
                         |
          +--------------+----------------+
          |              |                |
    Identity Service  Prayer Service   Quran Service
          |              |                |
     Identity DB      Prayer DB        Quran DB
          |
          +---------------- Kafka ----------------+
                              |
                 +------------+------------+
                 |                         |
        Notification Service       Analytics Service
                 |                         |
         Notification DB             Analytics DB
```

Supporting infrastructure:

```text
Spring Cloud Config
Eureka Service Discovery
Apache Kafka
Redis
PostgreSQL
Elasticsearch
Logstash
Kibana
Prometheus
Grafana
OpenTelemetry
Docker
Docker Compose
GitHub Actions
```

---

## 3. Core Services

### 3.1 Discovery Service

Responsibilities:

- Run the Eureka Server
- Register backend services
- Allow services to discover one another
- Provide service-instance health visibility

Port:

```text
8761
```

No database is required.

---

### 3.2 Config Service

Responsibilities:

- Provide centralized configuration
- Store shared and service-specific settings
- Separate configuration by environment
- Support local, development, staging, and production profiles

Port:

```text
8888
```

Configuration examples:

```text
application.yml
identity-service.yml
gateway-service.yml
prayer-service.yml
quran-service.yml
```

No application database is required.

---

### 3.3 Gateway Service

Responsibilities:

- Provide the single public backend entry point
- Route requests to internal services
- Validate access tokens
- Apply CORS configuration
- Apply rate limiting
- Add correlation IDs
- Handle common request logging
- Provide resilience and fallback behavior

Port:

```text
8080
```

Example routes:

```text
/api/auth/**       -> identity-service
/api/users/**      -> identity-service
/api/prayers/**    -> prayer-service
/api/quran/**      -> quran-service
/api/challenges/** -> community service later
```

The gateway does not own business data.

---

### 3.4 Identity Service

Responsibilities:

- User registration
- User login
- JWT access tokens
- Refresh tokens
- Password hashing
- Email verification
- Password reset
- User roles and permissions
- Account locking and suspension
- Basic user profile data
- Country, language, and timezone preferences

Port:

```text
8081
```

Database:

```text
thabat_identity_db
```

Owned tables may include:

```text
users
roles
user_roles
refresh_tokens
email_verification_tokens
password_reset_tokens
```

Published events:

```text
UserRegistered
UserProfileUpdated
UserAccountSuspended
UserAccountDeleted
```

---

### 3.5 Prayer Service

Responsibilities:

- Estimate missed prayers
- Store prayer calculation settings
- Create recovery plans
- Record completed makeup prayers
- Track daily and long-term progress
- Calculate remaining prayers
- Generate prayer statistics

Port:

```text
8082
```

Database:

```text
thabat_prayer_db
```

Owned tables may include:

```text
prayer_assessments
prayer_recovery_plans
prayer_progress_entries
prayer_daily_summaries
```

The service stores the user UUID but does not access the identity database.

Published events:

```text
PrayerAssessmentCreated
PrayerRecoveryPlanCreated
MakeupPrayerCompleted
PrayerGoalCompleted
```

---

### 3.6 Quran Service

Responsibilities:

- Store Quran structural reference data
- Track memorization by page
- Track memorization by surah and ayah
- Track juz progress
- Track Quran revision
- Manage memorization goals
- Calculate memorization statistics
- Schedule future reviews

Port:

```text
8083
```

Database:

```text
thabat_quran_db
```

Owned tables may include:

```text
surahs
ayahs
quran_pages
memorization_entries
revision_entries
memorization_goals
review_schedules
```

Published events:

```text
QuranPageMemorized
QuranRevisionCompleted
SurahMemorized
MemorizationGoalCompleted
```

---

### 3.7 Notification Service

Responsibilities:

- Push notifications
- Email notifications
- Reminder scheduling
- Notification templates
- User notification preferences
- Retry failed notifications

Port:

```text
8084
```

Database:

```text
thabat_notification_db
```

Consumes events such as:

```text
UserRegistered
PrayerGoalCompleted
QuranPageMemorized
ReviewReminderDue
```

Future integrations:

```text
Firebase Cloud Messaging
Email provider
Apple Push Notification Service
```

---

### 3.8 Analytics Service

Responsibilities:

- Store aggregated product analytics
- Calculate active users
- Track registrations by country
- Track prayer activity totals
- Track Quran activity totals
- Provide data to the admin dashboard
- Avoid exposing private worship details unnecessarily

Port:

```text
8085
```

Database:

```text
thabat_analytics_db
```

Consumes events from all business services.

Examples:

```text
UserRegistered
MakeupPrayerCompleted
QuranPageMemorized
QuranRevisionCompleted
```

---

## 4. Future Services

The following services should not be created during the initial foundation phase.

### Community Service

Future responsibilities:

- Friendships
- Study groups
- Challenges
- Achievements
- Leaderboards
- Country rankings
- Global rankings
- Privacy-based leaderboard participation

### Admin Service

The first admin APIs can be owned by the relevant services.

A separate admin service may later handle:

- Aggregated admin use cases
- Moderation workflows
- Reports
- Announcements
- Support cases

### Payment or Support Service

This should be added only after legal, immigration, tax, and app-store payment
requirements are reviewed.

Possible future responsibilities:

- One-time voluntary support
- App-store purchase verification
- Payment provider webhooks
- Financial reporting

---

## 5. Service Communication

### Synchronous communication

Use HTTP REST when the caller needs an immediate response.

Examples:

```text
Gateway -> Identity Service
Gateway -> Prayer Service
Gateway -> Quran Service
```

Internal REST communication should remain limited.

### Asynchronous communication

Use Kafka for events and workflows that do not require an immediate response.

Examples:

```text
Identity Service publishes UserRegistered
Notification Service sends a welcome notification
Analytics Service updates registration statistics
```

---

## 6. Kafka Topics

Initial topic plan:

```text
thabat.identity.events
thabat.prayer.events
thabat.quran.events
thabat.notification.events
```

Dead-letter topics:

```text
thabat.identity.events.dlt
thabat.prayer.events.dlt
thabat.quran.events.dlt
```

Events should contain:

```text
eventId
eventType
eventVersion
occurredAt
correlationId
producer
payload
```

Example:

```json
{
  "eventId": "43c7aa75-d67c-4dd2-b3c1-c286d5c3db93",
  "eventType": "UserRegistered",
  "eventVersion": 1,
  "occurredAt": "2026-07-12T18:00:00Z",
  "correlationId": "3aa9eb2b-a95d-4e2c-9908-bfe997194810",
  "producer": "identity-service",
  "payload": {
    "userId": "fe99c65b-6f54-49dd-b29c-688955b06889",
    "countryCode": "US",
    "preferredLanguage": "en"
  }
}
```

Sensitive information must not be included unless necessary.

Do not publish:

```text
Passwords
JWT tokens
Refresh tokens
Private prayer details
Private personal notes
```

---

## 7. Database Principles

Each service owns its own database.

```text
identity-service -> thabat_identity_db
prayer-service   -> thabat_prayer_db
quran-service    -> thabat_quran_db
notification     -> thabat_notification_db
analytics        -> thabat_analytics_db
```

Rules:

- A service must not directly query another service's database.
- Foreign keys must not cross service databases.
- User references are stored as UUID values.
- Flyway manages every service database independently.
- Cross-service state is synchronized through APIs or Kafka events.

---

## 8. Authentication and Authorization

The identity service issues access and refresh tokens.

Recommended approach:

```text
Identity Service holds the private signing key.
Gateway and resource services use the public verification key.
```

Access token claims may include:

```text
sub
roles
email
iat
exp
jti
```

Downstream services should validate the JWT themselves.

The gateway may forward trusted identity context internally, but services must not
trust arbitrary client-provided identity headers.

---

## 9. Reliability Patterns

The system will progressively implement:

- Timeouts
- Retries with backoff
- Circuit breakers
- Bulkheads
- Idempotent consumers
- Dead-letter topics
- Transactional outbox pattern
- Health checks
- Graceful degradation

Kafka consumers must expect duplicate delivery.

Every consumer should use an event ID to prevent duplicate processing.

---

## 10. Observability

### Logs

Use structured JSON logs containing:

```text
timestamp
service
environment
level
correlationId
traceId
spanId
message
```

Log aggregation:

```text
Application -> Logstash -> Elasticsearch -> Kibana
```

### Metrics

```text
Spring Boot Actuator
Micrometer
Prometheus
Grafana
```

Important metrics:

```text
HTTP request duration
HTTP error rate
Database connection usage
Kafka consumer lag
JVM memory
JVM CPU
Active users
Registration count
```

### Distributed tracing

Use OpenTelemetry with a tracing backend such as Tempo or Jaeger.

A request should be traceable across:

```text
Gateway -> Identity Service -> Kafka -> Notification Service
```

---

## 11. Local Development

Docker Compose will eventually run:

```text
PostgreSQL databases
Kafka
Kafka UI
Redis
Elasticsearch
Logstash
Kibana
Prometheus
Grafana
Tracing backend
```

Spring services may initially run from IntelliJ.

Later, all services will support Docker-based startup.

---

## 12. Deployment Strategy

Initial deployment:

```text
Docker containers
AWS
Managed PostgreSQL
Managed secrets
Central logs and metrics
```

Possible future orchestration:

```text
Amazon ECS
Amazon EKS
Kubernetes
```

Kubernetes should be introduced only after the Docker Compose environment is stable.

---

## 13. Initial Development Order

```text
1. Parent Maven project
2. Discovery Service
3. Config Service
4. Gateway Service
5. Identity Service
6. PostgreSQL infrastructure
7. Registration and login
8. JWT validation
9. Kafka infrastructure
10. Prayer Service
11. Quran Service
12. Notification Service
13. Analytics Service
14. Redis
15. Centralized logging
16. Metrics and dashboards
17. Distributed tracing
18. CI/CD
19. Mobile application
20. Admin dashboard
```

---

## 14. Architecture Principles

- Keep business capabilities clearly separated.
- Avoid shared databases.
- Avoid excessive synchronous service-to-service calls.
- Keep events versioned.
- Protect sensitive worship and identity data.
- Prefer privacy-preserving aggregate analytics.
- Keep services independently deployable.
- Add infrastructure only when there is a real use case.
- Maintain automated tests and documentation.