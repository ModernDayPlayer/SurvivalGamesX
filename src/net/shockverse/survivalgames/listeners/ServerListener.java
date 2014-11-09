package net.shockverse.survivalgames.listeners;

import net.shockverse.survivalgames.ArenaManager;
import net.shockverse.survivalgames.GameManager;
import net.shockverse.survivalgames.SurvivalGames;
import net.shockverse.survivalgames.core.Language;
import net.shockverse.survivalgames.core.Perms;
import net.shockverse.survivalgames.core.Settings;
import net.shockverse.survivalgames.data.ArenaData;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListPingEvent;

/**
 * @description Handles enabling plugins
 * @author Duker02, LegitModern, Tagette
 */
public class ServerListener implements Listener {

    private final SurvivalGames plugin;

    public ServerListener(SurvivalGames instance) {
        plugin = instance;
    }
    
    public void disable() {
    	PluginEnableEvent.getHandlerList().unregister(plugin);
    	PluginDisableEvent.getHandlerList().unregister(plugin);
        ServerListPingEvent.getHandlerList().unregister(plugin);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin() != plugin) {
            // Try to load again!
            Perms.onOtherPluginEnable();
            plugin.getTreasury().onOtherPluginEnable();
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPluginDisable(PluginDisableEvent event) {

    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onServerPing(ServerListPingEvent event) {
        GameManager gameMan = plugin.getGameManager();
        ArenaManager arenaMan = plugin.getArenaManager();
        Language.setVar("arenaname", arenaMan.getCurrentArena().name);
        Language.setVar("worldname", arenaMan.getCurrentArena().worldName);
        int worldPlayers = 0;
        ArenaData cData = arenaMan.getCurrentArena();
        if(cData != null) {
            World currentWorld = Bukkit.getWorld(cData.worldName);
            worldPlayers = currentWorld.getPlayers().size();
        }
        Language.setVar("players", worldPlayers + "");
        Language.setVar("tributes", gameMan.getTributeNames().size() + "");
        Language.setVar("spectators", gameMan.getSpectatorNames().size() + "");
        Language.setVar("maxplayers", arenaMan.getCurrentArena().spawns.size() + "");
        Language.setVar("nextarena", arenaMan.getNextArena() != null ? arenaMan.getNextArena().name : "Unknown");
        
        Settings settings = plugin.getSettings();
        String motd = "";
        switch(gameMan.getState()) {
            case LOBBY:
                motd = Language.getCustomLanguage(settings.motdLobby, true);
                break;
            case STARTING:
                motd = Language.getCustomLanguage(settings.motdStarting, true);
                break;
            case GAME:
                motd = Language.getCustomLanguage(settings.motdInGame, true);
                break;
            case PRE_DEATHMATCH:
                motd = Language.getCustomLanguage(settings.motdPreDM, true);
                break;
            case DEATHMATCH:
                motd = Language.getCustomLanguage(settings.motdDeathMatch, true);
                break;
            case GAME_OVER:
            case RESETTING:
            case RESTARTING:
                motd = Language.getCustomLanguage(settings.motdRestarting, true);
                break;
        }
        event.setMotd(motd);
    }
    
    /*
    == Server ==

    MapInitializeEvent 
    PluginDisableEvent 
    PluginEnableEvent 
    PluginEvent 
    RemoteServerCommandEvent 
    ServerCommandEvent 
    ServerEvent 
    ServerListPingEvent 
    ServiceEvent 
    ServiceRegisterEvent 
    ServiceUnregisterEvent
    */
}
