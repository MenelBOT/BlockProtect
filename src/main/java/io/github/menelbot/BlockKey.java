package io.github.menelbot;

import org.bukkit.block.Block;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record BlockKey(UUID world, int x, int y, int z) {
    @Contract("_ -> new")
    static @NotNull BlockKey of(@NotNull Block b) {
        return new BlockKey(b.getWorld().getUID(), b.getX(), b.getY(), b.getZ());
    }
}
