.API Documentation
We use the OpenAPI 3.1 specification to document our API. The main specification file is located at openapi.yml.

.Local View
open aop-docs.zip to check static index.html page 

.Getting Started
 How to lint and mock:
   npx @stoplight/spectral-cli lint openapi.yml
   npx @stoplight/prism-cli mock -p 8080 openapi.yml：


 How to curl to test:
   GET /api/issue: Get a list of all issues.
   curl -X 'GET' \
   'http://127.0.0.1:8080/api/issues?page=1&status=open&per_page=10' \
   -H 'accept: application/json'

  Post /api/webhooks/github 
  curl -i -X POST 'http://127.0.0.1:8080/api/webhooks/github' \                                                  
  -H 'Content-Type: application/json' \
  -H 'X-GitHub-Event: issue_comment' \
  -H 'X-GitHub-Delivery: 8e9a1d3a-4b8a-4a8e-9f52-0b1d1f2f3c4d' \
  -H 'X-Hub-Signature-256: sha256=aee73876dc0cbb71cf5122dd391733e28fabc009eba54be1a2066cb1e92c81d1' \
  -d '{
        "action": "created",
        "issue": {
          "id": 123, "number": 1, "title": "hello", "state": "open",
          "user": { "id": 456, "login": "alice" }
        },
        "comment": {
          "id": 9001, "body": "LGTM",
          "user": { "id": 456, "login": "alice" },
          "created_at": "2024-01-01T00:00:00Z",
          "updated_at": "2024-01-01T00:00:00Z",
          "html_url": "https://github.com/you/your-repo/issues/1#issuecomment-9001"
        },
        "repository": {
          "id": 789,
          "full_name": "you/your-repo",
          "html_url": "https://github.com/you/your-repo"
        },
        "sender": { "id": 456, "login": "alice" }
      }'
 

