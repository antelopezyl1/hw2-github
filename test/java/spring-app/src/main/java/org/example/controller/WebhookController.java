package org.example.controller;

import org.example.service.WebhookService;
import org.example.service.WebhookService.Outcome;
import org.example.service.WebhookService.Result;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.example.model.ErrorResponse;

import java.util.UUID;

/**
 * POST /webhook (security: [] per OpenAPI)
 * - Required headers: X-GitHub-Event, X-GitHub-Delivery, X-Hub-Signature-256
 * - Body: GitHubWebhookPayload (oneOf)
 * - Responses: 204 | 400 {error} | 401 {error}
 */
@RestController
public class WebhookController {

	private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping(path = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> webhook(@RequestHeader(value = WebhookService.HDR_SIG, required = false) String sig,
                                     @RequestHeader(value = WebhookService.HDR_EVENT, required = false) String event,
                                     @RequestHeader(value = WebhookService.HDR_DELIV, required = false) String deliveryId,
                                     @RequestHeader HttpHeaders headers,
                                     @RequestBody byte[] rawBody) {

        String requestId = headers.getFirst("X-Request-Id");
        if (requestId == null || requestId.isBlank()) requestId = UUID.randomUUID().toString();

        try (MDC.MDCCloseable ignored = MDC.putCloseable("request_id", requestId)) {
            Outcome outcome = webhookService.process(sig, event, deliveryId, requestId, rawBody);

            if (outcome.result == Result.ACK) {
                return ResponseEntity.noContent().build(); // 204
            }

            // Build error body
            ErrorResponse err = new ErrorResponse();
            int code = switch (outcome.result) {
                case UNAUTHORIZED -> 401;
                case BAD_REQUEST  -> 400;
                case SERVER_ERROR -> 500;
                default -> 500;
            };
            err.setCode(code);
            err.setMessage(outcome.message);

            return switch (outcome.result) {
                case UNAUTHORIZED -> ResponseEntity.status(401).contentType(MediaType.APPLICATION_JSON).body(err);
                case BAD_REQUEST  -> ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(err);
                case SERVER_ERROR -> ResponseEntity.internalServerError().contentType(MediaType.APPLICATION_JSON).body(err);
                default           -> ResponseEntity.internalServerError().contentType(MediaType.APPLICATION_JSON).body(err);
            };
        }
    }
}

