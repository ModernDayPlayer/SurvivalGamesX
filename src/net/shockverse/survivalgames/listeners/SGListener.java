package net.shockverse.survivalgames.listeners;

import net.shockverse.survivalgames.SurvivalGames;
import net.shockverse.survivalgames.events.SGShutdownEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * @decsription Handles all block related events
 * @author Duker02, LegitModern,  Tagette
 */
public class SGListener implements Listener {

    private final SurvivalGames plugin;

    public SGListener(final SurvivalGames plugin) {
        this.plugin = plugin;
    }
    
    public void disable() {
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSGShutdown(SGShutdownEvent event) {
        if(!event.isCancelled()) {
            //plugin.restartServer();
        }
    }
    
    /*
    == Survival Games ==
     
    SGShutdownEvent
    
    
    */
}
