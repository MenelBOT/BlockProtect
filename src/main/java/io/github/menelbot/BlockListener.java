package io.github.menelbot;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.FluidLevelChangeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class BlockListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent e) {
        if (BlockGuard.isProtected(e.getBlock())) e.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlace(BlockPlaceEvent e) {
        if (BlockGuard.isProtected(e.getBlock())) e.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onExplode(EntityExplodeEvent e) {
        e.blockList().removeIf(BlockGuard::isProtected);
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockExplode(BlockExplodeEvent e) {
        e.blockList().removeIf(BlockGuard::isProtected);
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPhysics(BlockPhysicsEvent e) {
        if (BlockGuard.isProtected(e.getBlock())) e.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFluid(FluidLevelChangeEvent e) {
        if (BlockGuard.isProtected(e.getBlock())) e.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        if (e.getBlocks().stream().anyMatch(BlockGuard::isProtected)) e.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        if (e.getBlocks().stream().anyMatch(BlockGuard::isProtected)) e.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSpread(BlockSpreadEvent e) {
        if (BlockGuard.isProtected(e.getBlock())) e.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onIgnite(BlockIgniteEvent e) {
        if (BlockGuard.isProtected(e.getBlock())) e.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBurn(BlockBurnEvent e) {
        if (BlockGuard.isProtected(e.getBlock())) e.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGrow(BlockGrowEvent e) {
        if (BlockGuard.isProtected(e.getBlock())) e.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFade(BlockFadeEvent e) {
        if (BlockGuard.isProtected(e.getBlock())) e.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityChange(EntityChangeBlockEvent e) {
        if (BlockGuard.isProtected(e.getBlock())) e.setCancelled(true);
    }
}
