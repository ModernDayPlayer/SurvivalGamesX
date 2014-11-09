package net.shockverse.survivalgames.data;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.util.Vector;

public class WorldData {
    public boolean enabled = true;
    public String name = "Survival Games";
    public String worldFolder;
    public List<RewardData> rewards = new ArrayList<RewardData>();
    public List<Vector> spawns = new ArrayList<Vector>();
    public List<BlockData> blockData = new ArrayList<BlockData>();
    public List<Material> breakWhitelist = new ArrayList<Material>();
    public List<Material> placeWhitelist = new ArrayList<Material>();
    public int graceTime = 30;
    public int gameCountdown = 60;
    public int gameTime = 20;
    public int deathMatchCountdown = 60;
    public int deathMatchTime = 300;
    public int stateMessageTime = 40;
    public int minStartTributes = 6;
    public int minTributes = 3;
    public int refillWorldTime = 17000;
    public int maxChestRewards = 15;
    public String gameMakerChat = "&RED[GM] %display%: %message%";
    public String tributeChat = "&DARK_GREEN%display%: %message%";
    public String specChat = "&DARK_GRAY[SPECTATOR] %user%: %message%";
}