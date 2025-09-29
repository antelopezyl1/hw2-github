package org.example.service;

public interface WebhookService {
	// Header names 
    String HDR_SIG   = "X-Hub-Signature-256";
    String HDR_EVENT = "X-GitHub-Event";
    String HDR_DELIV = "X-GitHub-Delivery";

    enum Result { ACK, UNAUTHORIZED, BAD_REQUEST, SERVER_ERROR }

    final class Outcome {
        public final Result result;
        public final String message; // populated for errors
        public Outcome(Result result, String message) {
            this.result = result; this.message = message;
        }
    }

    Outcome process(String signatureHeader,
                    String eventName,
                    String deliveryId,
                    String requestId,
                    byte[] rawBody);
}
