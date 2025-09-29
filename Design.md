Design Note

1) Error Mapping

Webhook (POST /webhook)

- 204 No Content: valid signature + supported event; we persist asynchronously/quickly and ACK.
- 401 Unauthorized: signature verification fails.
- 400 Bad Request: missing required headers (X-GitHub-Event, X-GitHub-Delivery, X-Hub-Signature-256), invalid signature header format, unsupported event, missing/invalid JSON (action missing for non-ping).
- 500 Internal Server Error: server misconfiguration (e.g., no WEBHOOK_SECRET) or unexpected failure.

Issues/Comments proxy endpoints 
- 400: invalid client params to our API (e.g., bad per_page, state).
- 401: missing/invalid our bearer token (per OpenAPI bearerAuth), not GitHub’s token.
- 403: client authenticated but not allowed by our service policy (optional).
- 404: requested issue/comment not found (propagate from GitHub when applicable).
- 409: GitHub rejects update due to version/state conflicts.
- 503 Service Unavailable: upstream GitHub outage or rate-limit we can’t immediately honor.

2) Pagination Strategy

Client-facing query params
- Support page, per_page, and (for issues) state, labels exactly as in OpenAPI.
- Validate per_page bounds (1–100).

Forwarding to GitHub
- Translate our page/per_page to GitHub’s same-named params.
- Always send Accept: application/vnd.github+json.

Headers to propagate back
- Link: copy GitHub’s pagination Link header verbatim (provides next, prev, first, last).
- X-RateLimit-Remaining and X-RateLimit-Reset: forward from GitHub so clients can self-throttle.

3) Webhook Dedupe & Persistence

Idempotency key
- Key = delivery_id + ":" + action.
	Rationale: GitHub’s X-GitHub-Delivery is unique per delivery; pairing with action is
	defensive if GitHub ever reuses a UUID across different actions (rare, but safe).

Store
- File-based SQLite (webhooks.db) with table:

Flow
1. Verify headers + signature.
2. Parse action (or ping), extract issue.number (optional for logs).
3. Persist with idempotency key; log stored=true|false.
4. Respond 204 immediately; never block on long work.

Observability
- Structured logs: request_id, gh_delivery_id, gh_event, stored, issue_number.
- Never log payload content or secrets; only sizes/IDs.


4) Security Trade-offs

HMAC verification
- HMAC SHA-256 over raw request bytes with WEBHOOK_SECRET.
- Use constant-time comparison (MessageDigest.isEqual).
- Validate header format sha256=<64-hex> before computing.
- Reject if secret is missing (server misconfig → 500).

Open endpoint vs. signature
- /webhook has no bearer auth (per OpenAPI security: []), so signature validation is mandatory.
- Trade-off: attackers can hit the endpoint, but without the secret their requests are rejected.

Secret management
- Never log the secret or raw signature.
- Load from env (WEBHOOK_SECRET) or property (webhook.secret).
- Consider rotation: allow multiple secrets (CSV), accept if any verifies; deprecate old ones.
- Store secrets in a secure vault for production.