package com.rob.houserental.repository;

/**
 * Shared callback interface for all asynchronous database operations.
 * Replaces the identical per-repository inner interface definitions.
 *
 * @param <T> The type of the successful result.
 */
public interface DatabaseCallback<T> {

    /** Called when the operation completes successfully. */
    void onSuccess(T result);

    /** Called when the operation fails with an exception. */
    void onError(Exception exception);
}
