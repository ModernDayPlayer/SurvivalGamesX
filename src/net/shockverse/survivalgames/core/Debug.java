package net.shockverse.survivalgames.core;


import java.util.HashMap;
import java.util.Map;
import net.shockverse.survivalgames.SurvivalGames;
import net.shockverse.survivalgames.extras.DebugDetailLevel;
import org.bukkit.entity.Player;

/**
 * @description Handles debugging for the plugin.
 * @author Duker02, LegitModern, Tagette
 */
public class Debug {

    private Map<Player, DebugDetailLevel> debugees;
    private boolean debugging;
    private DebugDetailLevel detailLevel;
    private SurvivalGames plugin;
    private Settings settings;

    public Debug(SurvivalGames instance) {
        plugin = instance;
        this.settings = plugin.getSettings();
        debugging = settings.initialDebug;
        debugees = new HashMap<Player, DebugDetailLevel>();
        detailLevel = settings.debugLevel;
    }
    
    public void disable(){
        debugees.clear();
        debugees = null;
        debugging = false;
    }

    /*
     * Checks is the plugin is in debug mode.
     */
    public boolean inDebugMode() {
        return !debugees.isEmpty() || debugging;
    }

    /*
     * Checks if a player is in debug mode.
     * 
     * @param player    The player to check.
     */
    public boolean isDebugging(Player player) {
        return debugees.get(player) != null;
    }

    public void startDebugging() {
        if (!inDebugMode())
            debugging = Constants.debugAllowed;
    }

    /*
     * Sets a players debug mode.
     * 
     * @param player    The player to set the debug mode of.
     */
    public void startDebugging(Player player) {
        if (Constants.debugAllowed && !isDebugging(player)) {
            debugees.put(player, settings.debugLevel);
        }
    }

    public void stopDebugging(Player player) {
        if (inDebugMode()) {
            debugees.remove(player);
            if(debugees.isEmpty())
                debugging = false;
        }
    }

    public void stopDebugging(String displayMessage) {
        if (inDebugMode()) {
            for (Player player : debugees.keySet()) {
                player.sendMessage(displayMessage);
            }
            debugees.clear();
            debugging = false;
        }
    }

    public void everything(String message) {
        if (inDebugMode()) {
            if (DebugDetailLevel.EVERYTHING.compareTo(detailLevel) >= 0) {
                Logger.info(message);
            }
            for (Player player : debugees.keySet()) {
                if (DebugDetailLevel.EVERYTHING.compareTo(debugees.get(player)) >= 0) {
                    player.sendMessage(message);
                }
            }
        }
    }

    public void normal(String message) {
        if (inDebugMode()) {
            if (DebugDetailLevel.NORMAL.compareTo(detailLevel) >= 0) {
                Logger.info(message);
            }
            for (Player player : debugees.keySet()) {
                if (DebugDetailLevel.NORMAL.compareTo(debugees.get(player)) >= 0) {
                    player.sendMessage(message);
                }
            }
        }
    }

    public void important(String message) {
        if (inDebugMode()) {
            if (DebugDetailLevel.IMPORTANT.compareTo(detailLevel) >= 0) {
                Logger.info(message);
            }
            for (Player player : debugees.keySet()) {
                if (DebugDetailLevel.IMPORTANT.compareTo(debugees.get(player)) >= 0) {
                    player.sendMessage(message);
                }
            }
        }
    }
    
    public void setDetailLevel(DebugDetailLevel level){
        detailLevel = level;
    }
    
    public void setDetailLevel(Player player, DebugDetailLevel detail){
        if(isDebugging(player)){
            debugees.remove(player);
            debugees.put(player, detail);
        }
    }
}
