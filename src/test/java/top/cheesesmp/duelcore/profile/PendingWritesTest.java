package top.cheesesmp.duelcore.profile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;

/** A login waits for every pending write of the player, not only the last one tracked. */
class PendingWritesTest {

    @Test
    void waitsForEarlierWrites() {
        Map<UUID, CompletableFuture<?>> pending = new ConcurrentHashMap<>();
        UUID uuid = UUID.randomUUID();
        CompletableFuture<Integer> rating = new CompletableFuture<>();
        CompletableFuture<Void> touch = new CompletableFuture<>();
        ProfileService.track(pending, uuid, rating);
        ProfileService.track(pending, uuid, touch);

        touch.complete(null);
        CompletableFuture<?> waiting = pending.get(uuid);
        assertNotNull(waiting);
        assertFalse(waiting.isDone(), "the rating write is still running");

        rating.completeExceptionally(new RuntimeException("db down"));
        assertTrue(waiting.isDone());
        assertFalse(waiting.isCompletedExceptionally(), "failed writes do not fail the login wait");
        assertNull(pending.get(uuid), "removed once everything finished");
    }

    @Test
    void laterWriteKeepsEntry() {
        Map<UUID, CompletableFuture<?>> pending = new ConcurrentHashMap<>();
        UUID uuid = UUID.randomUUID();
        CompletableFuture<Void> first = new CompletableFuture<>();
        ProfileService.track(pending, uuid, first);
        CompletableFuture<Void> second = new CompletableFuture<>();
        ProfileService.track(pending, uuid, second);
        first.complete(null);
        assertNotNull(pending.get(uuid));
        second.complete(null);
        assertNull(pending.get(uuid));
    }

    @Test
    void completedWriteIsRemoved() {
        Map<UUID, CompletableFuture<?>> pending = new ConcurrentHashMap<>();
        UUID uuid = UUID.randomUUID();
        ProfileService.track(pending, uuid, CompletableFuture.completedFuture(1));
        assertNull(pending.get(uuid));
    }
}
