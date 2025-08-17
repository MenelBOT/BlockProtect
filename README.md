# BlockGuard

Scoped, best‑effort protection for Bukkit/Paper blocks while your plugin operates.

`BlockGuard` lets you mark a set of block coordinates as *protected* for a limited time so that normal edits from players, physics, explosions, pistons, fluids, growth, or other plugins are prevented while you work. Protection is reference‑counted and lease‑based. You release it explicitly or let it expire.

> This library does **not** freeze the world or make blocks immutable. It intercepts Bukkit/Paper events and cancels or prunes changes that target protected blocks. It is reliable against standard API edits. It cannot prevent NMS‑level mutations that bypass events.

---

## Contents

- [Requirements](#requirements)
- [Installation](#installation)
- [Core Types](#core-types)
  - [BlockKey](#blockkey)
  - [BlockGuard](#blockguard)
  - [BlockListener](#blocklistener)
- [Quick Start](#quick-start)
- [Lifecycle](#lifecycle)
- [Event Coverage](#event-coverage)
- [Patterns and Tips](#patterns-and-tips)
- [Threading](#threading)
- [Limitations](#limitations)
- [FAQ](#faq)

---

## Requirements

- Java 21+
- Spigot/Paper API 1.21+ (tested on modern Paper builds)

---

## Installation

Add the library as a dependency.

```kotlin
dependencies {
    implementation("com.github.MenelBOT:BlockProtect:1.0.0")
}
```

```groovy
dependencies {
  implementation 'com.github.MenelBOT:BlockProtect:1.0.0'
}
```

```xml
<dependency>
  <groupId>com.github.MenelBOT</groupId>
  <artifactId>BlockProtect</artifactId>
  <version>1.0.0</version>
  <scope>shadow</scope>
</dependency>
```

Register the listener in your plugin’s `onEnable()`:

```java
getServer().getPluginManager().registerEvents(new BlockListener(), this);
```

---

## Core Types

### BlockKey

Value object that identifies a block by world UUID and integer coordinates.

```java
public record BlockKey(UUID world, int x, int y, int z) {
    public static BlockKey of(Block b) { /* world UID + x/y/z */ }
}
```

- Immutable and safe as a `HashMap` key.
- Use it when you need to store or compare positions without holding live `Block` references.

### BlockGuard

Central coordinator that tracks **leases** over protected blocks.

Key points:

- `acquire(Collection<Block> blocks, long ttlMillis)` returns a `Lease` bound to the given coordinates.
- Protection is **per‑key** and **reference‑counted** across multiple leases.
- `Lease` implements `AutoCloseable`: release early via `close()` or try‑with‑resources.
- `Lease.renew(extraMillis)` extends only that lease.
- `isProtected(Block)` answers whether a block is currently under any unexpired lease.

### BlockListener

Event listener that enforces protection. It cancels edits or prunes lists when a target is protected.

Handlers include (names may vary by API version):

- `BlockBreakEvent`, `BlockPlaceEvent`
- `BlockExplodeEvent`, `EntityExplodeEvent`
- `BlockPhysicsEvent`, `FluidLevelChangeEvent`
- `BlockPistonExtendEvent`, `BlockPistonRetractEvent`
- `BlockSpreadEvent`, `BlockIgniteEvent`, `BlockBurnEvent`
- `BlockGrowEvent`, `BlockFadeEvent`
- `EntityChangeBlockEvent`

---

## Quick Start

```java
// 1) Collect candidate blocks using your own scan (e.g., BFS over 6-neighbors)
List<Block> targets = /* your BlockScanner.find6Neighbor(...) */;

// 2) Acquire a lease for 10 seconds
try (var lease = BlockGuard.acquire(targets, 10_000)) {
    // 3) Do work that must not be interrupted by edits
    process(targets);

    // 4) Need more time? Extend the lease in place
    lease.renew(5_000);

    // 5) Done: exiting the try block calls close() automatically
}
```

You can release immediately by calling `lease.close()` without waiting for TTL.

---

## Lifecycle

1. **Acquire** a lease before you start work that must be protected.
2. **Operate** while the listener prevents edits to those coordinates.
3. **Extend** with `renew(...)` if needed, or **release** early with `close()`.
4. **Expire**: if you neither renew nor close, the lease auto‑expires after TTL.

Multiple subsystems may acquire leases on the same blocks. Protection remains until **all** unexpired leases are gone.

---


## Patterns and Tips

- **Generous TTL + early close**: pick a safe upper bound, then release early.
- **Renew on milestones**: extend after completing phases that took longer than expected.
- **Watchdog (optional)**: snapshot `BlockData` and periodically revert if an out‑of‑band change slipped through. Useful against NMS edits.
- **Chunk pinning (optional)**: keep chunks loaded while protected if your workflow is sensitive to unload/reload.
- **Value snapshots**: when you only need to *read*, use plain value objects instead of live `Block` references.

---

## Threading

- Call `acquire`, `renew`, and `close` on the **server thread**.
- Event handlers fire on the server thread; the guard’s internal maps are synchronized.
- If you compute target sets asynchronously, marshal back to the main thread before acquiring.

---

## FAQ

**Q: What if two systems protect the same area?**\
A: Leases are reference‑counted. Protection ends only when all leases expire or close.

**Q: Can I mutate protected blocks myself?**\
A: Yes. Protection is for external edits. Your code can still change them. If you want to block *all* edits, add guards in your own code paths or perform mutations before acquiring.

**Q: How big can the protected set be?**\
A: The data structures are O(n) in the number of protected coordinates. For very large regions consider chunk‑ or cuboid‑level protection alternatives.

---

## Example Custom Listener Snippet

```java
public final class BlockListener implements Listener {
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
  public void onBreak(BlockBreakEvent e) {
    if (BlockGuard.isProtected(e.getBlock())) e.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
  public void onPlace(BlockPlaceEvent e) {
    if (BlockGuard.isProtected(e.getBlockPlaced())) e.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
  public void onExplode(EntityExplodeEvent e) {
    e.blockList().removeIf(BlockGuard::isProtected);
  }

  // Add physics, fluids, pistons, spread/growth/fade, entity-change similarly
}
```

---

**Menel Permissive Non-Resale License (MPNRL-1.0)**
Copyright (c) 2025 Menel

You may use, modify, and distribute this library freely, **including in commercial plugins**,
**provided** that you do **not** sell or license this library itself for a fee, except as a
dependency of a larger work which substantially extends its functionality.

Plain English:

* ✅ Free plugins: fully allowed
* ✅ Paid plugins using this as a dependency: allowed
* ❌ Selling this library directly: not allowed
* ❌ Premium wrappers with minimal changes: not allowed

Full license text: [LICENSE](./LICENSE)

---
