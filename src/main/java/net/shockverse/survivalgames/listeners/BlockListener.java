package net.shockverse.survivalgames.listeners;

import net.shockverse.survivalgames.ArenaManager;
import net.shockverse.survivalgames.GameManager;
import net.shockverse.survivalgames.GameManager.SGGameState;
import net.shockverse.survivalgames.SurvivalGames;
import net.shockverse.survivalgames.data.ArenaData;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * @decsription Handles all block related events
 *
 * @author Duker02, LegitModern, Tagette
 */
public class BlockListener implements Listener {

    private final SurvivalGames plugin;

    public BlockListener(final SurvivalGames plugin) {
        this.plugin = plugin;
    }

    public void disable() {
        BlockBurnEvent.getHandlerList().unregister(plugin);
        WeatherChangeEvent.getHandlerList().unregister(plugin);
        BlockBreakEvent.getHandlerList().unregister(plugin);
        BlockPlaceEvent.getHandlerList().unregister(plugin);
    }

//    @EventHandler(priority = EventPriority.NORMAL)
//    public void onBlockBurn(BlockBurnEvent ev) {
//        Location location = ev.getBlock().getLocation();
//        BlockData block = plugin.getArenaManager().getBlock(location);
//
//        if ((!ev.isCancelled()) && (block == null)) {
//            block = new BlockData();
//            block.location = location;
//            block.type = Material.AIR;
//            block.data = 0;
//
//            plugin.getDebug().normal("OnBlockBurn");
//            plugin.getArenaManager().setBlock(null, location.getWorld(), block);
//        }
//    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onWeatherChange(WeatherChangeEvent ev) {
        boolean changeToStorm = ev.toWeatherState();
        if(changeToStorm && !plugin.getArenaManager().getCurrentArena().stormy
                || !changeToStorm && plugin.getArenaManager().getCurrentArena().stormy)
            ev.setCancelled(true);
        ev.getWorld().setThundering(false);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent ev) {
        Player p = ev.getPlayer();
        Location location = ev.getBlock().getLocation();
        Material type = location.getBlock().getType();
//        BlockData block = plugin.getArenaManager().getBlock(location);
        GameManager gameMan = plugin.getGameManager();
        ArenaManager arenaMan = plugin.getArenaManager();
        ArenaData aData = arenaMan.getCurrentArena();
        
        if(!arenaMan.isEditing(p.getName(), p.getWorld().getName())) {
            if(gameMan.isSpectator(p)) {
                ev.setCancelled(true);
            } else if(gameMan.getState() != SGGameState.GAME
                    && gameMan.getState() != SGGameState.DEATHMATCH) {
                ev.setCancelled(true);
            } else if(aData != null && !aData.breakWhitelist.contains(type)) {
                ev.setCancelled(true);
            }
        }

//        if (!ev.isCancelled() && block == null 
//                && !arenaMan.isEditing(p.getName(), p.getWorld().getName())) {
//            block = new BlockData();
//            block.location = location;
//            block.type = location.getBlock().getType();
//            block.data = location.getBlock().getData();
//
//            plugin.getDebug().normal("OnBlockBreak");
//            plugin.getArenaManager().setBlock(p.getName(), p.getWorld(), block);
//        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent ev) {
        Player p = ev.getPlayer();
//        BlockData block = plugin.getArenaManager().getBlock(location);
        Material type = ev.getBlock().getType();
        GameManager gameMan = plugin.getGameManager();
        ArenaManager arenaMan = plugin.getArenaManager();
        ArenaData aData = arenaMan.getCurrentArena();
        
        if(!arenaMan.isEditing(p.getName(), p.getWorld().getName())) {
            if(gameMan.isSpectator(p)) {
                ev.setCancelled(true);
            } else if(gameMan.getState() != SGGameState.GAME
                    && gameMan.getState() != SGGameState.DEATHMATCH) {
                ev.setCancelled(true);
            } else if(aData != null && !aData.placeWhitelist.contains(type)) {
                ev.setCancelled(true);
            }
        }
        if(!ev.isCancelled()) {
            // Allowed
            if(ev.getBlock().getType() == Material.TNT) {
                ev.getBlock().setType(Material.AIR);
                TNTPrimed tnt = (TNTPrimed) p.getWorld().spawnEntity(ev.getBlock().getLocation(), EntityType.PRIMED_TNT);
                tnt.setFuseTicks(plugin.getSettings().tntTicks);
                tnt.setMetadata("explode", new FixedMetadataValue(plugin, false));
            } 
//            else if (block == null 
//                    && !arenaMan.isEditing(p.getName(), p.getWorld().getName())) {
//                block = new BlockData();
//                block.location = location;
//                block.type = Material.AIR;
//                block.data = 0;
//                plugin.getDebug().normal("OnBlockPlace");
//                plugin.getArenaManager().setBlock(p.getName(), p.getWorld(), block);
//            }
        }
    }

    @EventHandler
    public void fragBallExplode(EntityExplodeEvent e) {
        e.setCancelled(true);

        for(Block block : e.blockList()) {
            if(block.getRelative(BlockFace.UP).getType() == Material.AIR && block.getType().isSolid()) {
                FallingBlock fallingBlock = block.getWorld().spawnFallingBlock(block.getLocation().add(0, 1, 0), block.getType(), block.getData());
                double x = (block.getLocation().getX() - e.getLocation().getX()) / 3,
                        y = 1,
                        z = (block.getLocation().getZ() - e.getLocation().getZ()) / 3;
                fallingBlock.setVelocity(new Vector(x, y, z).normalize());
                fallingBlock.setMetadata("explode", new FixedMetadataValue(plugin, false));
                fallingBlock.setDropItem(false);
                e.setYield(0F);
            }
        }
    }

    @EventHandler
    public void fragBallFallingBlock(final EntityChangeBlockEvent event) {
        if ((event.getEntityType() == EntityType.FALLING_BLOCK)) {
            if(event.getEntity().hasMetadata("explode")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        event.getBlock().getWorld().playEffect(event.getBlock().getLocation(), Effect.STEP_SOUND, event.getBlock().getType().getId());
                        event.getBlock().setType(Material.AIR);
                    }
                }.runTaskLater(plugin, 1);
            }
        }
    }
    
    /*
     * == Block ==
     *
     * BlockBreakEvent BlockBurnEvent BlockCanBuildEvent BlockDamageEvent
     * BlockDispenseEvent BlockEvent BlockExpEvent BlockFadeEvent BlockFormEvent
     * BlockFromToEvent BlockGrowEvent BlockIgniteEvent BlockPhysicsEvent
     * BlockPistonEvent BlockPistonExtendEvent BlockPistonRetractEvent
     * BlockPlaceEvent BlockRedstoneEvent BlockSpreadEvent EntityBlockFormEvent
     * LeavesDecayEvent NotePlayEvent SignChangeEvent
     *
     * == Painting ==
     *
     * PaintingBreakByEntityEvent PaintingBreakEvent PaintingEvent
     * PaintingPlaceEvent
     */
}
