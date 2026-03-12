# CanBankX — Plateforme bancaire commerciale canadienne

## Vue d'ensemble

CanBankX est une plateforme bancaire numérique conçue pour les clients particuliers et PME au Canada. Le système implémente une architecture microservices avec API RESTful sécurisée, observabilité complète et conformité réglementaire (FINTRAC, OSFI, LPRPDE).

## Architecture

```
┌─────────────┐     ┌──────────────┐     ┌──────────────┐
│   Client     │────▶│  API Gateway │────▶│  Customer    │
│  (Web/Mobile)│     │  (Nginx)     │     │  Service     │
└─────────────┘     │  :8080       │     │  :8081       │
                    └──────────────┘     └──────┬───────┘
                                                │
                    ┌──────────────┐     ┌──────▼───────┐
                    │  Grafana     │     │  PostgreSQL  │
                    │  :3000       │     │  :5432       │
                    └──────┬───────┘     └──────────────┘
                           │
                    ┌──────▼───────┐
                    │  Prometheus  │
                    │  :9090       │
                    └──────────────┘
```

### Stack technique
- **Langage** : Java 21
- **Framework** : Spring Boot 4.0.3
- **Base de données** : PostgreSQL 16
- **Sécurité** : JWT + BCrypt
- **API Gateway** : Nginx
- **Observabilité** : Prometheus + Grafana (4 Golden Signals)
- **Documentation API** : OpenAPI 3 / Swagger UI
- **Build** : Gradle
- **Conteneurs** : Docker + Docker Compose

## Cas d'utilisation implémentés

| UC | Description | Endpoint |
|----|------------|----------|
| UC-01 | Inscription & Vérification KYC | `POST /api/v1/auth/register`, `PUT /api/v1/customers/{id}/kyc/verify` |
| UC-02 | Authentification JWT | `POST /api/v1/auth/login` |
| UC-03 | Ouverture compte (chèque/épargne) | `POST /api/v1/accounts` |
| UC-04 | Consultation soldes & historiques | `GET /api/v1/accounts/{customerId}`, `GET /api/v1/transactions/{accountId}` |
| UC-05 | Virement bancaire | `POST /api/v1/transactions/transfer` |
| UC-06 | Paiement de factures | `POST /api/v1/bills/pay` |
| UC-07 | Détection AML (activités suspectes) | Automatique sur chaque transaction |
| UC-08 | Audit trail réglementaire | `GET /api/v1/audit/{entityType}` |

## Démarrage rapide

### Prérequis
- Docker & Docker Compose
- Java 21 (pour le développement local)

### Lancer avec Docker Compose (recommandé)

```bash
# Cloner le projet
git clone https://github.com/kasra122/projet_LOG430.git
cd projet_LOG430

# Démarrer tous les services
docker-compose up --build -d

# Vérifier la santé
curl http://localhost:8080/actuator/health
```

### Services disponibles

| Service | URL | Description |
|---------|-----|-------------|
| API Gateway | http://localhost:8080 | Point d'entrée principal |
| Customer Service (direct) | http://localhost:8081 | Accès direct au service |
| Swagger UI | http://localhost:8081/swagger-ui.html | Documentation API interactive |
| Prometheus | http://localhost:9090 | Métriques et monitoring |
| Grafana | http://localhost:3000 | Dashboards (admin/admin) |

### Développement local

```bash
cd services/customer-service

# Build
./gradlew build

# Tests
./gradlew test

# Lancer (nécessite PostgreSQL local)
./gradlew bootRun
```

## API — Guide d'utilisation

### 1. Inscription (UC-01)

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jean",
    "lastName": "Tremblay",
    "email": "jean@example.com",
    "password": "MonMotDePasse123",
    "address": "123 Rue Sainte-Catherine, Montréal",
    "phone": "514-555-0100"
  }'
```

Réponse : `{ "token": "eyJ...", "email": "jean@example.com" }`

### 2. Authentification (UC-02)

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{ "email": "jean@example.com", "password": "MonMotDePasse123" }'
```

### 3. Ouvrir un compte (UC-03)

```bash
curl -X POST "http://localhost:8080/api/v1/accounts?customerId={UUID}&accountType=CHECKING&currency=CAD" \
  -H "Authorization: Bearer {TOKEN}"
```

### 4. Dépôt

```bash
curl -X POST "http://localhost:8080/api/v1/transactions/deposit?accountId={UUID}&amount=1000" \
  -H "Authorization: Bearer {TOKEN}" \
  -H "Idempotency-Key: dep-001"
```

### 5. Virement (UC-05)

```bash
curl -X POST "http://localhost:8080/api/v1/transactions/transfer?sourceAccountId={UUID}&targetAccountId={UUID}&amount=250" \
  -H "Authorization: Bearer {TOKEN}" \
  -H "Idempotency-Key: xfer-001"
```

### 6. Paiement de factures (UC-06)

```bash
curl -X POST "http://localhost:8080/api/v1/bills/pay?accountId={UUID}&payee=Hydro-Quebec&amount=150" \
  -H "Authorization: Bearer {TOKEN}" \
  -H "Idempotency-Key: bill-001"
```

### 7. Vérification KYC (UC-01)

```bash
curl -X PUT "http://localhost:8080/api/v1/customers/{customerId}/kyc/verify" \
  -H "Authorization: Bearer {TOKEN}"
```

## Exigences non fonctionnelles

### Sécurité
- **Authentification** : JWT avec tokens signés HS256, expiration configurable
- **Mots de passe** : Hachés avec BCrypt (10 rounds)
- **CORS** : Configuré pour contrôler les origines autorisées
- **CSRF** : Désactivé (API stateless)
- **Credentials** : Externalisés via variables d'environnement

### Observabilité (4 Golden Signals)
- **Latence** : Histogram P95/P99 des requêtes HTTP
- **Trafic** : Requests per second par endpoint
- **Erreurs** : Taux d'erreurs 4xx/5xx
- **Saturation** : CPU, mémoire JVM (heap), threads, pool de connexions DB

### Idempotence
Toutes les opérations financières supportent un header `Idempotency-Key` pour garantir l'exactly-once delivery.

### Audit & Conformité
Journal d'audit immuable (append-only) pour toutes les opérations :
- Création de clients
- Opérations KYC
- Transactions financières
- Paiements de factures
- Alertes AML

### Détection AML (Anti Money Laundering)
- **Seuil montant** : Transactions > $10,000 CAD signalées automatiquement
- **Fréquence** : > 10 transactions en 24h sur un même compte

## Structure du projet

```
projet_LOG430/
├── docker-compose.yml              # Orchestration des services
├── .github/workflows/ci.yml        # Pipeline CI/CD
├── infra/
│   ├── prometheus/prometheus.yml    # Config de scraping Prometheus
│   ├── grafana/
│   │   ├── provisioning/           # Auto-config Grafana
│   │   └── dashboards/             # Dashboard 4 Golden Signals
│   └── nginx/nginx.conf            # API Gateway config
├── services/
│   └── customer-service/
│       ├── Dockerfile              # Multi-stage build
│       ├── build.gradle            # Dépendances & plugins
│       └── src/
│           ├── main/java/com/canbankx/customer/
│           │   ├── config/         # SecurityConfig, OpenApiConfig
│           │   ├── controller/     # REST endpoints
│           │   ├── domain/         # Entités JPA
│           │   ├── dto/            # DTOs (Auth, Register)
│           │   ├── exception/      # Exceptions + GlobalHandler
│           │   ├── repository/     # Spring Data repositories
│           │   ├── security/       # JWT provider + filter
│           │   └── service/        # Business logic
│           └── test/               # Unit + integration tests
```

## Tests

```bash
cd services/customer-service
./gradlew test
```

**28 tests** couvrant :
- `CustomerServiceTest` — CRUD + KYC workflow
- `AccountServiceTest` — Création compte chèques/épargne + validations
- `TransactionServiceTest` — Dépôt/retrait/virement + idempotence + validation montants
- `BillPaymentServiceTest` — Paiement factures + idempotence
- `CustomerServiceApplicationTests` — Context load Spring Boot

## Variables d'environnement

| Variable | Défaut | Description |
|----------|--------|-------------|
| `POSTGRES_DB` | `canbankx` | Nom de la base de données |
| `POSTGRES_USER` | `canbankx` | Utilisateur PostgreSQL |
| `POSTGRES_PASSWORD` | `canbankx` | Mot de passe PostgreSQL |
| `JWT_SECRET` | (voir .properties) | Secret pour signer les JWT |
| `JWT_EXPIRATION_MS` | `3600000` | Durée de validité des tokens (1h) |

## Décisions architecturales (ADR)

### ADR-001 : Architecture en couches MVC
**Contexte** : Besoin d'une séparation claire des responsabilités.
**Décision** : Controller → Service → Repository avec entités JPA.
**Conséquence** : Code testable, maintenable, et extensible.

### ADR-002 : JWT Stateless Authentication
**Contexte** : API REST nécessitant une authentification sans état.
**Décision** : JWT signé HS256 avec BCrypt pour les mots de passe.
**Conséquence** : Scalabilité horizontale, pas de session serveur.

### ADR-003 : Idempotency Keys pour transactions
**Contexte** : Garantir l'exactly-once pour les opérations financières.
**Décision** : Header `Idempotency-Key` avec stockage en DB (unique constraint).
**Conséquence** : Prévention des doubles traitements, conformité bancaire.
