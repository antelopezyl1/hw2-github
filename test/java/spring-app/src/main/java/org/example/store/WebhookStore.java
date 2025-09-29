package org.example.store;

public interface WebhookStore {
    /**
     * Persist the webhook event if not already present.
     * @return true if newly stored, false if it was a duplicate.
     */
    boolean saveIfAbsent(String deliveryId, String event, String action, String issueNumber, byte[] payload);
}
