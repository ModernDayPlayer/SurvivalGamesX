package net.shockverse.survivalgames.extras;

import net.shockverse.survivalgames.interfaces.CancellableTask;
import net.shockverse.survivalgames.interfaces.DelayedTask;
import net.shockverse.survivalgames.interfaces.StubbornTask;
import net.shockverse.survivalgames.interfaces.Task;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author LegitModern, Tagette
 */
public abstract class GameTask implements Task, DelayedTask, CancellableTask, StubbornTask {

    private JavaPlugin plugin;
    private long delay;
    private long repeat;
    private boolean cancelled;
    private boolean hasFinished;
    private int timesRun;
    
    public GameTask(JavaPlugin plugin) {
        this(plugin, 0, 0);
    }
    
    public GameTask(JavaPlugin plugin, long delay) {
        this(plugin, delay, 0);
    }
    
    public GameTask(JavaPlugin plugin, long delay, long repeat) {
        this.plugin = plugin;
        this.delay = delay;
        this.repeat = repeat;
        schedule(delay);
    }
    
    @Override
    public abstract void run();
    @Override
    public void runAnyways() { } // Here to override optionally
    @Override
    public void onCancel() { } // Here to override optionally
    @Override
    public void onResume() { } // Here to override optionally
    @Override
    public void onFinish() { } // Here to override optionally
    
    private void schedule(long delay) {
        if((delay > 0 || timesRun == 0) && !isCancelled()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){

                @Override
                public void run() {
                    if(!isCancelled()) GameTask.this.run();
                    runAnyways();
                    
                    timesRun++;
                    schedule(repeat);
                }

            }, delay);
        } else {
            hasFinished = true;
        }
    }
    
    @Override
    public boolean hasFinished() {
        return hasFinished;
    }
    
    @Override
    public boolean hasRun() {
        return timesRun > 0;
    }
    
    @Override
    public int getTimesRun() {
        return timesRun;
    }
    
    @Override
    public long getDelay() {
        return delay;
    }
    
    @Override
    public long getRepeat() {
        return repeat;
    }
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
        if(cancelled)
            onCancel();
        else
            onResume();
    }

}
