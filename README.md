Building a service wrapping the GitHub REST API for single repository issues

.API Documentation
We use the OpenAPI 3.1 specification to document our API. The main specification file is located at openapi.yml.

.Getting Started
 How to lint and mock:
   npx @stoplight/spectral-cli lint openapi.yml
   npx @stoplight/prism-cli mock -p 8080 openapi.yml：

.env:
All env parameters are defined in /src/main/resources/static, please use Run-configurations-env to input correct values.

# GitHub Issues API — Mock (DevOps & Docs)

This repo ships an OpenAPI 3.1 contract (openapi.yml) and a Prism mock for development and testing without the real backend.

## References
- Some code templates were assisted by ChatGPT:
  - `webClientBuilder` in `GitHubConfig`
  - Method-chain template for `gitHubWebClient` HTTP CRUD in `GitHubService`
  - `mockMvc` template for `healthzTest` in `HealthControllerTest`

Building a service wrapping the GitHub REST API for single repository issues

.API Documentation  
We use the OpenAPI 3.1 specification to document our API. The main specification file is located at `openapi.yml`.

## Quickstart
```bash
# start mock on 8080
docker compose up -d mock

# health check 
curl -i -H 'Authorization: Bearer demo-token' http://127.0.0.1:8080/healthz
# list issues
curl -i -H 'Authorization: Bearer demo-token' \
  'http://127.0.0.1:8080/issues?page=1&state=open&per_page=10'

# get one issue
curl -i -H 'Authorization: Bearer demo-token' \
  'http://127.0.0.1:8080/issues/1'

# update issue 
curl -i -X PATCH -H 'Authorization: Bearer demo-token' -H 'Content-Type: application/json' \
  'http://127.0.0.1:8080/issues/1' -d '{"state":"closed"}'

# webhook 
curl -i -X POST 'http://127.0.0.1:8080/webhook' \
  -H 'Authorization: Bearer demo-token' -H 'Content-Type: application/json' \
  -H 'X-GitHub-Event: issues' \
  -H 'X-GitHub-Delivery: 00000000-0000-0000-0000-000000000000' \
  -H 'X-Hub-Signature-256: sha256=aee73876dc0cbb71cf5122dd391733e28fabc009eba54be1a2066cb1e92c81d1' \
  -d '{"action":"opened","issue":{"id":1,"number":1,"title":"hello","state":"open","user":{"id":1,"login":"alice"}},"repository":{"id":1,"full_name":"<owner>/<repo>"},"sender":{"id":1,"login":"alice"}}'

## UI_Screenshots.docx for: container running, health 200, issues 200, patch 200, webhook 204.

## Makefile 
make run         # docker compose up -d mock
make test        # /healthz → 200 + /webhook → 204 (smoke)
make coverage    # uses lint as the gate
make logs        # follow logs (Ctrl-C to exit)
make down        # stop containers
make lint        # spectral lint

##Non-Docker (Mock)
npx -y @stoplight/spectral-cli lint openapi.yml
npx -y @stoplight/prism-cli mock -p 8080 openapi.yml

##Non-Docker (example: Spring)
mvn -q spring-boot:run
# or:
mvn -q -DskipTests package && java -jar target/*.jar
curl -i http://127.0.0.1:8080/healthz

##Env vars (used by real backend)

GITHUB_TOKEN   
GITHUB_OWNER   
GITHUB_REPO    # e.g., CMPE272-GitHub-TestRepository-CodeCoven
WEBHOOK_SECRET 
PORT # default 8080

##Webhook Setup 
1) Expose /webhook:
   - smee:  npx -y smee-client --url https://smee.io/<id> --target http://127.0.0.1:8080/webhook
   - ngrok: ngrok http 8080
2) GitHub → Repo → Settings → Webhooks → Add:
   Payload URL = <tunnel>/webhook
   Content type = application/json
   Secret = WEBHOOK_SECRET
   Events = Issues (+ Issue comments)
3) Redelivery: Webhooks → Recent deliveries → select → Redeliver

##Tests

Unit + integration tests live under /tests (run by Person C).

Example (Java): mvn test

CI (optional): lint/test/build workflow.

Postman / HTTPie
# HTTPie examples
http GET :8080/healthz
http GET ':8080/issues?page=1&state=open&per_page=10'

##Troubleshooting

Port 8080 busy → PORT=8081 docker compose up -d mock and use :8081 in curls.

404 on /api/... → spec has no /api base path; use /issues, /webhook.

401 on mock → include -H 'Authorization: Bearer demo-token'.
