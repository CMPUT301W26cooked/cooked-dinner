package com.eventwise.database;

/**
 * Callback for delete-entrant operation.
 * Implement this interface to receive success/failure signals.
 */
public interface DeletionCallback {
    /** Called when deletion succeeds (idempotent success is also treated as success). */
    void onSuccess();

    /** Called when the operation fails due to authentication/authorization (optional). */
    void onUnauthorized();

    /** Called when deletion fails for other reasons. */
    void onFailure(String message);
}
