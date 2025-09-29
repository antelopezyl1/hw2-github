# GitHub Issues API Service

A Spring Boot service that wraps the GitHub REST API for managing issues in a single repository. This assignment provides a clean interface for issue management operations including listing, retrieving, updating issues, and handling webhooks.

## Features

- **Issue Management**: List, retrieve, and update GitHub issues
- **Webhook Support**: Handle GitHub webhook events for real-time updates
- **OpenAPI Documentation**: Complete API specification using OpenAPI 3.1
- **Mock Server**: Prism mock server for development and testing
- **Health Checks**: Built-in health monitoring endpoints
- **Docker Support**: Containerized deployment with Docker Compose

## API Documentation

We use the OpenAPI 3.1 specification to document our API. The main specification file is located at `openapi.yml`.

### Linting and Mocking

```bash
# Lint the OpenAPI specification
npx @stoplight/spectral-cli lint openapi.yml

# Start mock server on port 8080
npx @stoplight/prism-cli mock -p 8080 openapi.yml
```

## Quickstart

### Using Docker (Recommended)

```bash
# Start the mock server
docker compose up -d mock

# Health check
curl -i -H 'Authorization: Bearer demo-token' http://127.0.0.1:8080/healthz

# List issues
curl -i -H 'Authorization: Bearer demo-token' \
  'http://127.0.0.1:8080/issues?page=1&state=open&per_page=10'

# Get a specific issue
curl -i -H 'Authorization: Bearer demo-token' \
  'http://127.0.0.1:8080/issues/1'

# Update an issue
curl -i -X PATCH -H 'Authorization: Bearer demo-token' -H 'Content-Type: application/json' \
  'http://127.0.0.1:8080/issues/1' -d '{"state":"closed"}'

# Test webhook endpoint
curl -i -X POST 'http://127.0.0.1:8080/webhook' \
  -H 'Authorization: Bearer demo-token' -H 'Content-Type: application/json' \
  -H 'X-GitHub-Event: issues' \
  -H 'X-GitHub-Delivery: 00000000-0000-0000-0000-000000000000' \
  -H 'X-Hub-Signature-256: sha256=aee73876dc0cbb71cf5122dd391733e28fabc009eba54be1a2066cb1e92c81d1' \
  -d '{"action":"opened","issue":{"id":1,"number":1,"title":"hello","state":"open","user":{"id":1,"login":"alice"}},"repository":{"id":1,"full_name":"<owner>/<repo>"},"sender":{"id":1,"login":"alice"}}'
```

> **Note**: See `UI_Screenshots.docx` for screenshots showing container running, health 200, issues 200, patch 200, and webhook 204 responses.

## Development

### Makefile Commands

```bash
make run         # Start mock server with docker compose
make test        # Run smoke tests (/healthz → 200 + /webhook → 204)
make coverage    # Run tests with coverage (uses lint as gate)
make logs        # Follow container logs (Ctrl-C to exit)
make down        # Stop all containers
make lint        # Lint OpenAPI specification with Spectral
```

### Non-Docker Development

#### Mock Server Only
```bash
# Lint OpenAPI specification
npx -y @stoplight/spectral-cli lint openapi.yml

# Start mock server
npx -y @stoplight/prism-cli mock -p 8080 openapi.yml
```

#### Spring Boot Application
```bash
# Run with Maven
mvn -q spring-boot:run

# Or build and run JAR
mvn -q -DskipTests package && java -jar target/*.jar

# Test health endpoint
curl -i http://127.0.0.1:8080/healthz
```

## Environment Variables

The following environment variables are used by the real backend:

| Variable | Description | Example |
|----------|-------------|---------|
| `GITHUB_TOKEN` | GitHub personal access token | `ghp_xxxxxxxxxxxx` |
| `GITHUB_OWNER` | Repository owner | `your-username` |
| `GITHUB_REPO` | Repository name | `CMPE272-GitHub-TestRepository-CodeCoven` |
| `WEBHOOK_SECRET` | Webhook secret for signature verification | `your-secret-key` |
| `PORT` | Server port (default: 8080) | `8080` |

> **Note**: Environment parameters are defined in `/src/main/resources/static`. Use Run Configurations → Environment to input correct values.

## Webhook Setup

### 1. Expose the Webhook Endpoint

Choose one of the following methods to expose your local webhook endpoint:

#### Using Smee
```bash
npx -y smee-client --url https://smee.io/<id> --target http://127.0.0.1:8080/webhook
```

#### Using ngrok
```bash
ngrok http 8080
```

### 2. Configure GitHub Webhook

1. Go to your GitHub repository
2. Navigate to **Settings** → **Webhooks** → **Add webhook**
3. Configure the webhook:
   - **Payload URL**: `<tunnel-url>/webhook`
   - **Content type**: `application/json`
   - **Secret**: Use your `WEBHOOK_SECRET` value
   - **Events**: Select "Issues" and "Issue comments"
4. Click **Add webhook**

### 3. Test Webhook Delivery

- Go to **Webhooks** → **Recent deliveries**
- Select a delivery to view details
- Use **Redeliver** to retry failed deliveries

## Testing

### Unit and Integration Tests

Tests are located under `/test` directory and can be run with:

```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
```

### API Testing

#### Using HTTPie
```bash
# Health check
http GET :8080/healthz

# List issues
http GET ':8080/issues?page=1&state=open&per_page=10'
```

#### Using Postman
Import the OpenAPI specification (`openapi.yml`) into Postman for interactive API testing.

## Troubleshooting

### Common Issues

| Issue             | Solution |
|-------            |----------|
| Port 8080 is busy | Use `PORT=8081 docker compose up -d mock` and update URLs to use `:8081` |
| 404 on `/api/...` | The API has no `/api` base path; use `/issues`, `/webhook` directly |
| 401 Unauthorized on mock | Include `-H 'Authorization: Bearer demo-token'` in your requests |

## References

Some code templates were assisted by ChatGPT:
- `webClientBuilder` in `GitHubConfig`
- Method-chain template for `gitHubWebClient` HTTP CRUD in `GitHubService`
- `mockMvc` template for `healthzTest` in `HealthControllerTest`
