# -------- configurable vars --------
PORT ?= 8080
BASE ?= http://127.0.0.1:$(PORT)

# -------- phony targets --------
.PHONY: help run down logs lint lint_strict test coverage local-mock rerun

help:          ## Show targets
	@echo "make run [PORT=8081]     - start Prism mock via Docker Compose"
	@echo "make test [PORT=8081]    - smoke test health(200) + webhook(204)"
	@echo "make logs                - follow Prism logs"
	@echo "make down                - stop containers"
	@echo "make lint                - Spectral lint (non-blocking)"
	@echo "make lint_strict         - Spectral lint (fails on issues)"
	@echo "make coverage            - use lint as quality report"
	@echo "make local-mock          - run Prism via Node on PORT"
	@echo "make rerun [PORT=...]    - compose down + up"

run:           ## Start Prism mock via Docker Compose
	@echo "Starting mock on port $(PORT)"
	PORT=$(PORT) docker compose up -d mock

down:          ## Stop containers
	docker compose down

logs:          ## Follow Prism logs
	docker compose logs -f mock

lint:          ## Lint OpenAPI (prints findings; won't fail build)
	- npx -y @stoplight/spectral-cli lint --ruleset spectral:oas -f stylish openapi.yml

lint_strict:   ## Lint OpenAPI (fails on violations)
	npx -y @stoplight/spectral-cli lint --ruleset spectral:oas -f stylish openapi.yml

# Minimal smoke tests: assert health 200 and webhook 204 on $(BASE)
test: run
	@echo "Checking /healthz on $(BASE)..."
	@test $$(curl -s -o /dev/null -w '%{http_code}' -H 'Authorization: Bearer demo-token' $(BASE)/healthz) -eq 200
	@echo "Posting webhook to $(BASE)..."
	@test $$(curl -s -o /dev/null -w '%{http_code}' -i -X POST "$(BASE)/webhook" \
	  -H 'Authorization: Bearer demo-token' \
	  -H 'Content-Type: application/json' \
	  -H 'X-GitHub-Event: issues' \
	  -H 'X-GitHub-Delivery: 00000000-0000-0000-0000-000000000000' \
	  -H 'X-Hub-Signature-256: sha256=aee73876dc0cbb71cf5122dd391733e28fabc009eba54be1a2066cb1e92c81d1' \
	  -d '{"action":"opened","issue":{"id":1,"number":1,"title":"hello","state":"open","user":{"id":1,"login":"alice"}},"repository":{"id":1,"full_name":"you/your-repo"},"sender":{"id":1,"login":"alice"}}') -eq 204
	@echo "✅ Smoke tests passed"

coverage:      ## No runtime coverage for mock; use lint as the gate
	$(MAKE) lint
	@echo "No code coverage for the mock; Spectral lint is the quality report."

local-mock:    ## Run Prism locally without Docker on $(PORT)
	npx -y @stoplight/prism-cli mock -p $(PORT) openapi.yml

rerun:         ## Restart mock (down + up)
	$(MAKE) down
	$(MAKE) run
