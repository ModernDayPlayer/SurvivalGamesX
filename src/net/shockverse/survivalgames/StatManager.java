package net.shockverse.survivalgames;

import java.util.HashMap;

/**
 * @author Tagette, LegitModern
 */
public class StatManager {
    
    private SurvivalGames plugin;
    
    private HashMap<String, PlayerStats> playerStats;
    private HashMap<String, Integer> playerBounties;
    
    public StatManager(SurvivalGames plugin) {
        this.plugin = plugin;
        playerStats = new HashMap<String, PlayerStats>();
        playerBounties = new HashMap<String, Integer>();
    }
    
    public void disable() {
        // Save stuff.
        playerStats.clear();
        playerBounties.clear();
    }
    
    public void setBounty(String pName, int bounty) {
        playerBounties.put(pName.toLowerCase(), bounty);
    }
    
    public int getBounty(String pName) {
        int bounty = 0;
        if(playerBounties.containsKey(pName.toLowerCase()))
            bounty = playerBounties.get(pName.toLowerCase());
        return bounty;
    }
    
    public PlayerStats addPlayer(String pName) {
        if(!playerStats.containsKey(pName.toLowerCase())) {
            PlayerStats stats = PlayerStats.LoadFromDB(pName.toLowerCase());
            if(stats == null) {
                stats = new PlayerStats(pName.toLowerCase());
                stats.save();
            }
            playerStats.put(pName.toLowerCase(), stats);
        }
        return playerStats.get(pName.toLowerCase());
    }
    
    public void removePlayer(String pName) {
        playerStats.remove(pName.toLowerCase());
    }
    
    public PlayerStats getPlayer(String pName) {
        if(!playerStats.containsKey(pName.toLowerCase())) {
            PlayerStats stats = PlayerStats.LoadFromDB(pName.toLowerCase());
            if(stats != null)
                playerStats.put(pName.toLowerCase(), stats);
        }
        return playerStats.get(pName.toLowerCase());
    }
    
    public void savePlayer(String pName) {
        PlayerStats pStats = getPlayer(pName.toLowerCase());
        pStats.save();
    }
    
}
