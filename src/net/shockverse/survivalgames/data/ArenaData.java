package net.shockverse.survivalgames.data;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;

public class ArenaData {
    public boolean enabled = true;
    public boolean resetting = false;
    public String name = "Survival Games";
    public String worldName;
    public int lighterUses = 3;
    public int graceTime = 30;
    public int gameCountdown = 60;
    public int gameTime = 20;
    public int deathMatchCountdown = 60;
    public int deathMatchTime = 300;
    public int stateMessageTime = 40;
    public int minStartTributes = 6;
    public int minTributes = 3;
    public int worldStartTime = 1000;
    public boolean stormy = false;
    public int refillWorldTime = 17000;
    public int refillCount = 1;
    public int winPoints = 100;
    public int killPoints = 10;
    public int killPercent = 0;
    public double moneyMultiplier = 1.0;
    public boolean loseMoneyOnDeath = false;
    public boolean killDMRun = false;
    public double dmRange = 35;
    public String adminChat = "&RED[ADMIN] %tdisplay%: %message%";
    public String tributeChat = "&DARK_GREEN[%points%] %tdisplay%: %message%";
    public String specChat = "&GRAY[SPECTATOR] %tdisplay%: %message%";
    public String arenaDetails = "You are playing %arena%.\nMay the odds be ever in your favor.";
    
    public EnumMap<Material, ContainerData> containers = new EnumMap<Material, ContainerData>(Material.class);
    public Location spectatorSpawn;
    public Location dmCenter;
    public HashMap<Integer, Location> spawns = new HashMap<Integer, Location>();
    public Location dmSpectatorSpawn;
    public HashMap<Integer, Location> dmSpawns = new HashMap<Integer, Location>();
    public HashMap<String, List<BlockData>> blockData = new HashMap<String, List<BlockData>>();
    public List<Material> breakWhitelist = new ArrayList<Material>();
    public List<Material> placeWhitelist = new ArrayList<Material>();
    
    public List<String> editors = new ArrayList<String>();
}