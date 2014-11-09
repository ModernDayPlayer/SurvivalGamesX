package net.shockverse.survivalgames.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SGShutdownEvent extends Event implements Cancellable {

    // Declare private variables
    private boolean canceled;
    
    
    public SGShutdownEvent() {
        super(false);
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

    @Override
    public boolean isCancelled() {
        return canceled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        canceled = cancel;
    }
}
