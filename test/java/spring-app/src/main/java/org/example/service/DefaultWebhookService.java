package org.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.store.WebhookStore;
import org.example.util.HmacVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class DefaultWebhookService implements WebhookService {
	private static final Logger log = LoggerFactory.getLogger(DefaultWebhookService.class);

    private static final Pattern SIG_PATTERN = Pattern.compile("^sha256=[a-f0-9]{64}$");
    
    //Only these GitHub events are supported by this endpoint 
    private static final Set<String> SUPPORTED_EVENTS = Set.of("issues", "issue_comment", "ping");

    private final ObjectMapper mapper;
    private final HmacVerifier verifier;
    private final WebhookStore store;
    private final String webhookSecret;

    public DefaultWebhookService(ObjectMapper mapper,
                                 HmacVerifier verifier,
                                 WebhookStore store,
                                 @Value("${webhook.secret:${WEBHOOK_SECRET:}}") String webhookSecret) {
        this.mapper = mapper;
        this.verifier = verifier;
        this.store = store;
        this.webhookSecret = webhookSecret;
    }

    /**
     * Core webhook processing. Keeps work minimal and returns quickly (no long blocking).
     *
     * @param signatureHeader header {@code X-Hub-Signature-256}
     * @param eventName       header {@code X-GitHub-Event} 
     * @param deliveryId      header {@code X-GitHub-Delivery} 
     * @param requestId       request id for logging correlation 
     * @param rawBody         raw JSON request body
     * @return Outcome indicating ACK or specific error 
     */
    @Override
    public Outcome process(String signatureHeader,
                           String eventName,
                           String deliveryId,
                           String requestId,
                           byte[] rawBody) {

        try (MDC.MDCCloseable c1 = MDC.putCloseable("request_id", requestId != null ? requestId : UUID.randomUUID().toString());
             MDC.MDCCloseable c2 = MDC.putCloseable("gh_delivery_id", deliveryId);
             MDC.MDCCloseable c3 = MDC.putCloseable("gh_event", eventName)) {

            // headers
            if (deliveryId == null || deliveryId.isBlank())  return new Outcome(Result.BAD_REQUEST, "Missing X-GitHub-Delivery");
            if (eventName == null || !SUPPORTED_EVENTS.contains(eventName))
                return new Outcome(Result.BAD_REQUEST, "Unsupported or missing X-GitHub-Event");
            if (signatureHeader == null || signatureHeader.isBlank())
                return new Outcome(Result.BAD_REQUEST, "Missing X-Hub-Signature-256");
            if (!SIG_PATTERN.matcher(signatureHeader.trim()).matches())
                return new Outcome(Result.BAD_REQUEST, "Invalid signature header format");
            if (webhookSecret == null || webhookSecret.isBlank()) {
                log.error("WEBHOOK_SECRET not configured");
                return new Outcome(Result.SERVER_ERROR, "Server misconfiguration");
            }

            // HMAC verify (constant-time)
            if (!verifier.verifySha256(webhookSecret, rawBody, signatureHeader)) {
                log.warn("Webhook unauthorized: signature mismatch");
                return new Outcome(Result.UNAUTHORIZED, "Invalid signature");
            }

            // Extract action (required for non-ping) + optional issue number
            String action = "ping";
            String issueNumber = null;
            if (!"ping".equals(eventName)) {
                try {
                    JsonNode root = mapper.readTree(rawBody);
                    JsonNode actionNode = root.get("action");
                    if (actionNode == null || actionNode.asText().isBlank())
                        return new Outcome(Result.BAD_REQUEST, "Missing 'action' in payload");
                    action = actionNode.asText();

                    JsonNode issueNode = root.get("issue");
                    if (issueNode != null && issueNode.hasNonNull("number")) {
                        issueNumber = String.valueOf(issueNode.get("number").asInt());
                    }
                } catch (IOException e) {
                    return new Outcome(Result.BAD_REQUEST, "Invalid JSON payload");
                }
            }

            // Idempotent persistence
            boolean stored = store.saveIfAbsent(deliveryId, eventName, action, issueNumber, rawBody);

            // Structured summary
            log.info("Webhook ack: event='{}' action='{}' delivery='{}' issue='{}' stored={} at={}",
                    eventName, action, deliveryId, issueNumber, stored, Instant.now());

            return new Outcome(Result.ACK, "accepted");

        } catch (Exception ex) {
            log.error("Webhook processing failed unexpectedly", ex);
            return new Outcome(Result.SERVER_ERROR, "Internal server error");
        }
    }
}
