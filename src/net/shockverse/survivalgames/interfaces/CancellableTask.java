package net.shockverse.survivalgames.interfaces;

import org.bukkit.event.Cancellable;

public interface CancellableTask extends Cancellable {
    
    public void onCancel();
    public void onResume();
    
}
