package net.shockverse.survivalgames.listeners;

import net.shockverse.survivalgames.SurvivalGames;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;

/**
 * @decsription Handles all block related events
 * @author Duker02, LegitModern, Tagette
 */
public class WorldListener implements Listener {

    private final SurvivalGames plugin;

    public WorldListener(SurvivalGames plugin) {
        this.plugin = plugin;
    }
    
    public void disable() {
        WorldInitEvent.getHandlerList().unregister(this);
        WorldUnloadEvent.getHandlerList().unregister(this);
        WorldLoadEvent.getHandlerList().unregister(this);
        WorldSaveEvent.getHandlerList().unregister(this);
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onWorldInit(WorldInitEvent ev) {
        plugin.getDebug().normal("#### " + ev.getWorld().getName() + " was saved.");
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onWorldUnload(WorldUnloadEvent ev) {
        plugin.getDebug().normal("#### " + ev.getWorld().getName() + " was unloaded.");
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onWorldLoad(WorldLoadEvent ev) {
        plugin.getDebug().normal("#### " + ev.getWorld().getName() + " was loaded.");
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onWorldSave(WorldSaveEvent ev) {
        plugin.getDebug().normal("#### " + ev.getWorld().getName() + " was saved.");
    }
    
    /*
    == World ==

    ChunkEvent 
    ChunkLoadEvent 
    ChunkPopulateEvent 
    ChunkUnloadEvent 
    PortalCreateEvent 
    SpawnChangeEvent 
    StructureGrowEvent 
    WorldEvent 
    WorldInitEvent 
    WorldLoadEvent 
    WorldSaveEvent 
    WorldUnloadEvent

    == Weather ==

    LightningStrikeEvent 
    ThunderChangeEvent 
    WeatherChangeEvent 
    WeatherEvent
    */
}
