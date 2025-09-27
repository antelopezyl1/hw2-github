.PHONY: mock logs down lint local-mock

mock:        ## Start Prism mock in Docker
	docker compose up -d mock

logs:        ## Follow Prism logs
	docker compose logs -f mock

down:        ## Stop containers
	docker compose down

lint:        ## Lint OpenAPI
	npx @stoplight/spectral-cli lint openapi.yml

local-mock:  ## Run Prism via Node (no Docker)
	npx @stoplight/prism-cli mock -p 8080 openapi.yml
