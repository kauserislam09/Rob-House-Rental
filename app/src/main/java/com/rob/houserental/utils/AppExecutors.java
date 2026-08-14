package com.rob.houserental.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Shared thread pool executors for the entire application.
 * Replaces per-class ExecutorService instances to reduce thread proliferation
 * and ensure coordinated lifecycle management.
 */
public class AppExecutors {

    private static final AppExecutors INSTANCE = new AppExecutors();

    private final ExecutorService databaseExecutor;
    private final ExecutorService networkExecutor;

    private AppExecutors() {
        // Single thread for all sequential database operations
        databaseExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "db-executor");
            t.setDaemon(true);
            return t;
        });

        // Fixed pool for network I/O (up to 3 concurrent requests)
        networkExecutor = Executors.newFixedThreadPool(3, r -> {
            Thread t = new Thread(r, "net-executor");
            t.setDaemon(true);
            return t;
        });
    }

    public static AppExecutors getInstance() {
        return INSTANCE;
    }

    /**
     * Executor for all Room database operations.
     * Single-threaded to avoid SQLite write contention.
     */
    public ExecutorService getDatabaseExecutor() {
        return databaseExecutor;
    }

    /**
     * Executor for network/HTTP operations.
     * Fixed thread pool allowing limited concurrency.
     */
    public ExecutorService getNetworkExecutor() {
        return networkExecutor;
    }

    /** Convenience shortcut: execute on the database thread. */
    public static void runOnDatabase(Runnable task) {
        INSTANCE.databaseExecutor.execute(task);
    }

    /** Convenience shortcut: execute on the network thread pool. */
    public static void runOnNetwork(Runnable task) {
        INSTANCE.networkExecutor.execute(task);
    }
}
