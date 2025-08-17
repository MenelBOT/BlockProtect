package io.menelbot.github;

import org.bukkit.block.Block;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.Set;
import java.util.Collection;
import java.util.stream.Collectors;

public class BlockGuard {
    private static final Map<BlockKey, Integer> counts = new HashMap<>();
    private static final Map<BlockKey, Map<UUID ,Long>> expiries = new HashMap<>();

    public static final class Lease implements AutoCloseable {
        private final UUID id = UUID.randomUUID();
        private final Set<BlockKey> keys;
        private volatile boolean closed;
        private long until;

        private Lease(Set<BlockKey> keys, long ttl) {
            this.keys = keys;
            until = System.currentTimeMillis() + ttl;
            synchronized (counts) {
                for (BlockKey k : keys) {
                    counts.merge(k, 1, Integer::sum);
                    expiries.computeIfAbsent(k, __ -> new HashMap<>()).put(id, until);
                }
            }
        }

        /**
         * Add a single block to this lease without modifying the ttl.
         * @return true if newly added to the lease, false if it was already present.
         * @throws IllegalStateException if the lease is closed.
         */
        @ApiStatus.AvailableSince("1.0.0")
        @SuppressWarnings("unused")
        public boolean add(BlockKey k) {
            return add(k, 0);
        }

        /**
         * Add a single block to this lease. If already present, only its expiry is updated.
         * @return true if newly added to the lease, false if it was already present.
         * @throws IllegalStateException if the lease is closed.
         */
        @ApiStatus.AvailableSince("1.0.0")
        @SuppressWarnings("unused")
        public boolean add(BlockKey k, long ttl ) {
            if (closed) throw new IllegalStateException("lease is closed");
            long until = ttl == 0 ? this.until : System.currentTimeMillis() + ttl;
            this.until = until;
            boolean added;
            synchronized (counts) {
                added = keys.add(k);
                if (added) {
                    counts.merge(k, 1, Integer::sum);
                }
                expiries
                        .computeIfAbsent(k, __ -> new HashMap<>())
                        .put(id, until); // update or set this lease's expiry for k
            }
            return added;
        }

        /**
         * Remove a single block from this lease.
         * @return true if it was present and removed, false if it was not in this lease.
         * @throws IllegalStateException if the lease is closed.
         */
        @ApiStatus.AvailableSince("1.0.0")
        @SuppressWarnings("unused")
        public boolean remove(BlockKey k) {
            if (closed) throw new IllegalStateException("lease is closed");
            synchronized (counts) {
                if (!keys.remove(k)) return false;

                Map<UUID, Long> m = expiries.get(k);
                if (m != null) {
                    m.remove(id);
                    if (m.isEmpty()) expiries.remove(k);
                }
                counts.computeIfPresent(k, (__, n) -> n <= 1 ? null : n - 1);
            }
            return true;
        }

        /**
         * Add more time until the lease closes automatically.
         * @param extraMillis The amount of milliseconds to extend the lease by.
         * @throws IllegalStateException If the lease has already closed. Please acquire a new lease instead if this happens.
         */
        @ApiStatus.AvailableSince("1.0.0")
        @SuppressWarnings("unused")
        public void renew(long extraMillis) {
            if (closed) throw new IllegalStateException("lease is closed");
            long newUntil = System.currentTimeMillis() + extraMillis;
            synchronized (counts) {
                for (BlockKey k : keys) {
                    Map<UUID, Long> m = expiries.get(k);
                    if (m != null && m.containsKey(id)) m.put(id, newUntil);
                }
            }
        }

        @ApiStatus.AvailableSince("1.0.0")
        @Override public void close() {
            if (closed) return;
            closed = true;
            synchronized (counts) {
                for (BlockKey k : keys) {
                    Map<UUID, Long> m = expiries.get(k);
                    if (m != null) {
                        m.remove(id);
                        if (m.isEmpty()) expiries.remove(k);
                    }
                    counts.computeIfPresent(k, (__, n) -> n <= 1 ? null : n - 1);
                }
            }
        }
    }
    /**
     * @param blocks The blocks to enable protection on
     * @param ttl A number of milliseconds until the protection expires
     */
    @ApiStatus.AvailableSince("1.0.0")
    @SuppressWarnings("unused")
    public static Lease acquire(Collection<Block> blocks, long ttl) {
        Set<BlockKey> ks = blocks.stream().map(BlockKey::of).collect(Collectors.toUnmodifiableSet());
        return new Lease(ks, ttl);
    }

    /**
     * @param b The block to check.
     * @return <code>true</code> if the block is under any lease, <code>false</code> otherwise.
     */
    @ApiStatus.AvailableSince("1.0.0")
    @SuppressWarnings("unused")
    public static boolean isProtected(Block b) {
        BlockKey k = BlockKey.of(b);
        long now = System.currentTimeMillis();
        synchronized (counts) {
            Map<UUID, Long> m = expiries.get(k);
            if (m == null) return false;
            // prune
            m.values().removeIf(exp -> exp < now);
            if (m.isEmpty()) { expiries.remove(k); counts.remove(k); return false; }
            return true;
        }
    }
}
