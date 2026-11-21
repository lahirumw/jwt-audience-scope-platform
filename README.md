# Centralized JWT Authentication: Audience and Scope Security (multi-module)

**Java 21 · Spring Boot 4 · Spring Security 7 · RS256 · JWKS · Maven multi-module**

One central **auth-service** issues short-lived, audience- and scope-limited JWTs. Independent resource APIs
(**orders-api**, **customers-api**) validate those tokens locally using the issuer's public keys (JWKS).
Shared validation rules live in **security-common**.

## Modules

| Module | Port | Responsibility |
|---|---|---|
| `security-common` | – | Library: issuer + audience validation, scope/role mapping, reusable resource filter chain |
| `auth-service` | 8080 | Login, registration, scope policy, RS256 signing, `/.well-known/jwks.json` |
| `orders-api` | 8081 | Protected Orders API (`aud=orders-api`, `orders:read`, `orders:write`) |
| `customers-api` | 8082 | Protected Customers API (`aud=customers-api`, `customers:read`, `customers:write`) |

Resource APIs never call the issuer per request. They fetch the public key set once from the JWKS endpoint,
cache it, and verify every token locally.

## Run

Requirements: Java 21, Maven 3.9+. Start the issuer first, then the APIs, each in its own terminal:

```bash
mvn -pl auth-service spring-boot:run
mvn -pl orders-api spring-boot:run
mvn -pl customers-api spring-boot:run
```

(If Maven cannot resolve sibling modules, run `mvn install -DskipTests` once from the root.)

Demo accounts (local learning only): `demo / DemoPass123!` (USER), `admin / AdminPass123!` (ADMIN).

## Try it

```bash
# 1. Orders read token as a normal user
curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"DemoPass123!","audience":"orders-api","scopes":["orders:read"]}'

# 2. Call the Orders API (note port 8081) -> 200
curl http://localhost:8081/api/orders -H "Authorization: Bearer <TOKEN>"

# 3. Same token, write operation -> 403
curl -X POST http://localhost:8081/api/orders -H "Authorization: Bearer <TOKEN>"

# 4. Same token against Customers API (port 8082) -> 401, wrong audience
curl http://localhost:8082/api/customers -H "Authorization: Bearer <TOKEN>"

# 5. Normal user asks for a write scope -> 403 from the issuer
curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"DemoPass123!","audience":"orders-api","scopes":["orders:write"]}'
```

## Expected outcomes

| Scenario | Expected |
|---|---:|
| Orders token + `orders:read` → GET orders | 200 |
| Orders token with only read → POST orders | 403 |
| Orders token → Customers API | 401 |
| Customers token → Orders API | 401 |
| No token → protected API | 401 |
| User requests ungranted scope | 403 (issuer) |

## Production notes

This is an educational custom issuer, not a full OAuth 2.0 / OIDC authorization server. For production use
Spring Authorization Server or a managed identity provider, HTTPS, persistent managed signing keys with rotation,
persistent user storage, short token lifetimes, auditing and a revocation strategy. The demo generates an ephemeral
RSA key at startup and uses in-memory H2.
