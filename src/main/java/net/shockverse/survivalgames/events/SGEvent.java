package net.shockverse.survivalgames.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class SGEvent extends PlayerEvent {

    // Declare private variables

    public SGEvent(Player player /* Make arguments and set the variables. */) {
        super(player);
    }
    
    // Declare public getters methods for the variables.
    // The rest of this file is required for custom events.
    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
