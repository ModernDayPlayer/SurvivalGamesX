package net.shockverse.survivalgames;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.*;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.minecraft.util.org.apache.commons.io.FileUtils;
import net.minecraft.util.org.apache.commons.lang3.tuple.Pair;
import net.shockverse.survivalgames.core.*;
import net.shockverse.survivalgames.data.ArenaData;
import net.shockverse.survivalgames.data.ContainerData;
import net.shockverse.survivalgames.data.RewardData;
import net.shockverse.survivalgames.exceptions.PropertyException;
import net.shockverse.survivalgames.extras.GameTask;
import net.shockverse.survivalgames.extras.PropertyEntry;
import net.shockverse.survivalgames.extras.PropertyFile;
import net.shockverse.survivalgames.extras.PropertyList;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * @description Handles property files
 * @author LegitModern, Tagette
 */
public class ArenaManager extends HashMap<String, ArenaData> {

    private SurvivalGames plugin;
    private String arenaConfig = "Arenas";
    private String rewardsConfig = "Rewards";
    private String spawnsConfig = "Spawns";
    private String blockConfig = "Blocks";
    private Settings settings;
    
    private PropertyFile arenaFile;
    private HashMap<String, PropertyFile> spawnFiles;
    private HashMap<String, PropertyFile> rewardFiles;
    private HashMap<String, PropertyFile> blockFiles;
    
    private ArenaData lobby;
    private VoteManager voteMan;
    private String WORLD;
    private String LASTWORLD;
    private String NEXTWORLD;
    
    public List<String> arenaOrder;
    public HashMap<Block, Inventory> lootedContainers;
    private HashMap<String, String> editors;
    private HashMap<String, ProgressMonitor> progressMonitors;

    public ArenaManager(SurvivalGames instance, String arenaConfig, String rewardsConfig, String spawnsConfig) {
        plugin = instance;
        this.settings = plugin.getSettings();
        this.arenaConfig = arenaConfig;
        this.rewardsConfig = rewardsConfig;
        this.spawnsConfig = spawnsConfig;
        spawnFiles = new HashMap<String, PropertyFile>();
        rewardFiles = new HashMap<String, PropertyFile>();
        blockFiles = new HashMap<String, PropertyFile>();
        arenaOrder = new ArrayList<String>();
        lootedContainers = new HashMap<Block, Inventory>();
        editors = new HashMap<String, String>();
        progressMonitors = new HashMap<String, ProgressMonitor>();
        voteMan = new VoteManager();
    }
    
    public void disable(){
        this.clear();
        arenaOrder.clear();
        voteMan.clear();
        lootedContainers.clear();
        editors.clear();
    }

    public void load() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // Dont know why but needed to repeat..
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        try {
            File configFile = new File(plugin.getDataFolder(), arenaConfig + ".cfg");
            arenaFile = new PropertyFile(arenaConfig, configFile);
            arenaFile.loadProperties();
            setup(arenaFile.getProperties());
            arenaFile.saveProperties();
            for(String shortcut : arenaFile.getShortcuts().keySet())
                plugin.getDebug().everything(shortcut);
            if(size() <= 1)
                Logger.error("No arenas were loaded.");
            if (!settings.LowDetailMode) {
                Logger.info("Arena config loaded.");
            }
        } catch (PropertyException pe) {
            Logger.error(pe.getMessage());
        } catch(Exception ex) {
            Logger.error("Could not load " + arenaConfig + ".cfg file.");
            ex.printStackTrace();
        }
    }

    private void setup(PropertyList props) {
        // Declare settings here. Note that if config is not found these values should be placed into a new config file.

    	props.comment("-- Worlds --");
    	props.newLine();
        
        
        lobby = new ArenaData(); // Also gets default values.
                
        PropertyList lobbyList = props.getList("lobby");
        lobby.worldName = lobbyList.getString("worldFolder", "lobby");
        if(lobby.worldName.equals(""))
            lobby.worldName = "lobby";
                
        lobby.enabled = true;
        lobby.name = Tools.parseColors(lobbyList.getString("lobbyName", "Lobby"));
        lobby.gameTime = lobbyList.getInt("lobbyTime", 240, "Lobby seconds.");
        lobby.stateMessageTime = lobbyList.getInt("stateMessageTime", lobby.stateMessageTime, "How often the state message appears.");
        lobby.adminChat = lobbyList.getString("adminChat", lobby.adminChat, "The format of the chat for admins.");
        lobby.specChat = lobbyList.getString("spectatorChat", lobby.specChat, "The format of the chat for spectators.");
        lobby.tributeChat = lobbyList.getString("tributeChat", lobby.tributeChat, "The format of the chat for tributes.");
        World lobbyWorld = plugin.getServer().getWorld(lobby.worldName);
        lobby.spectatorSpawn = Tools.getLocation(lobbyWorld, 
                lobbyList.getVector("lobbySpawn", Tools.getVector(lobbyWorld.getSpawnLocation(), false), 
                "The spawn location for the lobby."));
        put(lobby.worldName, lobby);
        
        loadWorld(lobby.worldName);
        
        if(!settings.LowDetailMode)
            Logger.info("    '" + lobby.name + "' has been loaded.");
        
        PropertyList arenaList = props.getList("worlds");
        for(PropertyEntry arenaEntry : arenaList.getProperties()) {
            if(arenaEntry instanceof PropertyList) {
                // Build world.
                PropertyList arenaProps = arenaList.getList(arenaEntry.getKey(), "Your world folder name.");
                ArenaData aData = setupArena(arenaProps, arenaEntry.getKey());
                this.put(arenaEntry.getKey(), aData);
                if(aData.enabled && !settings.LowDetailMode)
                    Logger.info("    Arena '" + aData.name + "' has been loaded.");
            }
        }
        
    }
    
    private ArenaData setupArena(PropertyList arenaProps, String wName) {
        ArenaData aData = new ArenaData(); // Also gets default values.
        aData.name = Tools.parseColors(arenaProps.getString("arenaName", aData.name));
        aData.enabled = arenaProps.getBoolean("enabled", aData.enabled);
        aData.worldName = wName;

        if(aData.enabled) {
            if(!wName.equals("")) { 
                loadWorld(aData.worldName);
                // Make sure a zip exists.
                createWorldZips(wName, false);
            }
        } else {
            Logger.warning("Arena '" + wName + "' is disabled.");
        }

        aData.graceTime = arenaProps.getInt("graceTime", aData.graceTime, "Grace period seconds.");
        aData.gameCountdown = arenaProps.getInt("gameCountdown", aData.gameCountdown, "Countdown seconds before game begins.");
        aData.gameTime = arenaProps.getInt("gameTime", aData.gameTime, "Game minutes.") * 60;
        aData.deathMatchCountdown = arenaProps.getInt("deathMatchCountdown", aData.deathMatchCountdown, "Countdown before deathmatch.");
        aData.deathMatchTime = arenaProps.getInt("deathMatchTime", aData.deathMatchTime, "Death match seconds.");
        aData.minStartTributes = arenaProps.getInt("minStartTributes", aData.minStartTributes, "The minimum amount of tributes needed to start the game.");
        aData.minTributes = arenaProps.getInt("minDMTributes", aData.minTributes, "The amount of tributes that need to be left for the deathmatch to start.");
        aData.winPoints = arenaProps.getInt("winPoints", aData.winPoints);
        aData.killPoints = arenaProps.getInt("killPoints", aData.killPoints);
        aData.killPercent = arenaProps.getInt("killPercent", aData.killPercent, "This amount will be rewarded from a player you killed.");
        aData.moneyMultiplier = arenaProps.getDouble("moneyMultiplier", aData.moneyMultiplier, "When rewarding money, points will be multiplied by this number. 0.0 = disable");
        aData.loseMoneyOnDeath = arenaProps.getBoolean("loseMoneyOnDeath", aData.loseMoneyOnDeath, "If true than players will lose money when they die.");
        aData.worldStartTime = arenaProps.getInt("worldStartTime", aData.worldStartTime, "The world time the game will start at. (In ticks)");
        aData.stormy = arenaProps.getBoolean("stormy", aData.stormy, "If true then the world will be stormy.");
        aData.refillWorldTime = arenaProps.getInt("refillWorldTime", aData.refillWorldTime, "The chests will be refilled after this amount of time. (In ticks)");
        aData.refillCount = arenaProps.getInt("refillCount", aData.refillCount, "The number of times the chests will be refilled.");
        aData.killDMRun = arenaProps.getBoolean("killDMRun", aData.killDMRun, "Kills the tribute when they run away.");
        aData.dmRange = arenaProps.getDouble("dmRange", aData.dmRange, "The distance in blocks before the player is teleported or killed in deathmatch.");
        aData.adminChat = arenaProps.getString("adminChat", aData.adminChat, "The format of the chat for admins.");
        aData.specChat = arenaProps.getString("spectatorChat", aData.specChat, "The format of the chat for spectators.");
        aData.tributeChat = arenaProps.getString("tributeChat", aData.tributeChat, "The format of the chat for tributes.");
        aData.arenaDetails = arenaProps.getString("arenaDetails", aData.arenaDetails);

        aData = loadArenaSpawns(aData);
        aData = loadArenaRewards(aData);
        aData = loadArenaWhiteList(aData);
        
        return aData;
    }
    
    private ArenaData loadArenaRewards(ArenaData aData) {
        try {
            File worldFolder = new File(plugin.getDataFolder().getPath() + "/" + aData.worldName);
            if(!worldFolder.exists()) {
                worldFolder.mkdirs();
            }
            if(!worldFolder.exists()) {
                worldFolder.mkdirs();
            }
            File configFile = new File(plugin.getDataFolder().getPath() + "/" + aData.worldName, rewardsConfig + ".cfg");
            rewardFiles.put(aData.worldName, new PropertyFile(aData.worldName + "/" + rewardsConfig, configFile));
            rewardFiles.get(aData.worldName).loadProperties();
            PropertyList rewardProps = rewardFiles.get(aData.worldName).getProperties();
            if (aData.enabled && !settings.LowDetailMode) {
                Logger.info("    " + aData.worldName + " - loading container rewards...");
            }
            aData = setupArenaRewards(rewardProps, aData);
            rewardFiles.get(aData.worldName).saveProperties();
            for(String shortcut : rewardFiles.get(aData.worldName).getShortcuts().keySet())
                plugin.getDebug().everything(shortcut);
            if (aData.enabled && !settings.LowDetailMode) {
                Logger.info("        " + aData.containers.size() + " container(s) loaded. (" + rewardsConfig + ".cfg)");
            }
        } catch (PropertyException pe) {
            if(aData.enabled)
                Logger.error(pe.getMessage());
        } catch(Exception ex) {
            if(aData.enabled) {
                Logger.error("Could not load " + aData.worldName + "/" + rewardsConfig + ".cfg file.");
                ex.printStackTrace();
            }
        }
        return aData;
    }
    
    private ArenaData setupArenaRewards(PropertyList rewardProps, ArenaData aData) {
        rewardProps.comment("-- Rewards --");
        rewardProps.newLine();
        
        aData.lighterUses = rewardProps.getInt("lighterUses", aData.lighterUses, "The number of uses a lighter has left.");
        rewardProps.newLine();
        
        PropertyList containerList = rewardProps.getList("containers");
        int totalRewards = 0;
        for(PropertyEntry entry : containerList.getProperties()) {
            if(entry instanceof PropertyList) {
                Pair<Material, Byte> matByIdName = Tools.getMatByNameId(entry.getKey());
                if(matByIdName != null && matByIdName.getLeft() != null) {
                    ContainerData cData = new ContainerData();
                    cData.material = matByIdName.getLeft();
                    cData.data = matByIdName.getRight();
                    if(cData.material.isBlock()) {
                        PropertyList containerProps = containerList.getList(entry.getKey());
                        //String[] inherits = Tools.processArray(containerProps.getString("inherit", ""));
                        cData.enabled = containerProps.getBoolean("enabled", cData.enabled);
                        cData.title = containerProps.getString("name", cData.title, "The title that appears for the inventory.");
                        cData.minChestRewards = containerProps.getInt("minChestRewards", cData.minChestRewards, "The min amount of rewards that can be in a chest.");
                        cData.maxChestRewards = containerProps.getInt("maxChestRewards", cData.maxChestRewards, "The max amount of rewards that can be in a chest.");
                        
                        if(!cData.enabled)
                            Logger.warning("        " + cData.material + ":" + cData.data + " - Disabled. (" + rewardsConfig + ".cfg)");
                        
                        containerProps.comment("Item/Id = rarity x amount (or min, max)");
                        PropertyList rewardList = containerProps.getList("rewards");
                        for(PropertyEntry rewardEntry : rewardList.getProperties()) {
                            RewardData reward = new RewardData();
                            String itemNameId = rewardEntry.getKey();
                            reward.item = Tools.getMatByNameId(itemNameId).getLeft();
                            reward.data = Tools.getMatByNameId(itemNameId).getRight();
                            if(reward.item != null) {
                                String rewardStr = rewardList.getString(rewardEntry.getKey(), "0");
                                if(rewardStr.contains("x")) {
                                    String rarityStr = rewardStr.split("x")[0];
                                    String amountStr = rewardStr.split("x")[1];
                                    if(rarityStr != null && Tools.isInt(rarityStr.trim()))
                                        reward.rarity = Integer.parseInt(rarityStr.trim());
                                    if(amountStr != null) {
                                        if(amountStr.contains(",")) {
                                            String minStr = amountStr.split(",")[0];
                                            String maxStr = amountStr.split(",")[1];
                                            if(minStr != null && Tools.isInt(minStr.trim())) {
                                                reward.min = Integer.parseInt(minStr.trim());
                                            }
                                            if(maxStr != null && Tools.isInt(maxStr.trim())) {
                                                reward.max = Integer.parseInt(maxStr.trim());
                                            }
                                            if(minStr == null && maxStr != null) {
                                                reward.min = reward.max;
                                            }
                                            if(minStr != null && maxStr == null) {
                                                reward.max = reward.min;
                                            }
                                        } else if(Tools.isInt(amountStr.trim())) {
                                            reward.min = Integer.parseInt(amountStr.trim());
                                            reward.max = reward.min;
                                        }
                                    }
                                } else if(Tools.isInt(rewardStr)) {
                                    reward.rarity = Integer.parseInt(rewardStr.trim());
                                }
                                if(!cData.rewards.contains(reward)) {
                                    cData.rewards.add(reward);
                                    totalRewards++;
                                }
                            } else {
                                Logger.warning("Unknown  reward type " + rewardEntry.getKey() + " for " + cData.material + ". Ignored.");
                            }
                        }
                        if(!aData.containers.containsKey(cData.material)) {
                            aData.containers.put(cData.material, cData);
                            if (aData.enabled && cData.enabled && !settings.LowDetailMode) {
                                Logger.info("        " + cData.material + ":" + cData.data + " - " + cData.rewards.size() + " reward(s) loaded. (" + rewardsConfig + ".cfg)");
                            }
                        }
                    } else {
                        Logger.warning("Container type " + entry.getKey() + " is not a block. Ignored.");
                    }
                } else {
                    Logger.warning("Unkown container type " + entry.getKey() + ". Ignored.");
                }
            }
            if(aData.containers.isEmpty() || totalRewards == 0) {
                Logger.warning("No rewards have been added to '" + aData.name + "'. Arena disabled.");
                aData.enabled = false;
            }
        }
        

        return aData;
    }
    
    private ArenaData loadArenaSpawns(ArenaData aData) {
        try {
            File worldFolder = new File(plugin.getDataFolder().getPath() + "/" + aData.worldName);
            if(!worldFolder.exists()) {
                worldFolder.mkdirs();
            }
            if(!worldFolder.exists()) {
                worldFolder.mkdirs();
            }
            File configFile = new File(plugin.getDataFolder().getPath() + "/" + aData.worldName, spawnsConfig + ".cfg");
            spawnFiles.put(aData.worldName, new PropertyFile(aData.worldName + "/" + spawnsConfig, configFile));
            spawnFiles.get(aData.worldName).loadProperties();
            PropertyList spawnProps = spawnFiles.get(aData.worldName).getProperties();
            aData = setupArenaSpawns(spawnProps, aData);
            spawnFiles.get(aData.worldName).saveProperties();
            if (aData.enabled && !settings.LowDetailMode) {
                Logger.info("    " + aData.worldName + " - " + aData.spawns.size() + " spawn locations loaded. (" + spawnsConfig + ".cfg)");
            }
        } catch (PropertyException pe) {
            if(aData.enabled)
                Logger.error(pe.getMessage());
        } catch(Exception ex) {
            if(aData.enabled) {
                Logger.error("Could not load " + aData.worldName + "/" + spawnsConfig + ".cfg file.");
                ex.printStackTrace();
            }
        }
        return aData;
    }
    
    private ArenaData setupArenaSpawns(PropertyList spawnProps, ArenaData aData) {
        World world = plugin.getServer().getWorld(aData.worldName);
        if(world == null)
            Logger.info("World is null.");
        Vector worldSpawnPos = Tools.round(Tools.getVector(world.getSpawnLocation(), false), 2);
        spawnProps.comment("-- Arena Spawns --");
        spawnProps.newLine();
        aData.spectatorSpawn = Tools.getLocation(world, spawnProps.getVector("spectatorSpawn", worldSpawnPos));
        spawnProps.comment("Orders by the index.");
        spawnProps.comment("indexNumber = x, y, z");
        int index = 0;
        PropertyList spawnList = spawnProps.getList("spawns");
        HashMap<Integer, Location> worldSpawns = new HashMap<Integer, Location>();
        for(PropertyEntry spawnEntry : spawnList.getProperties()){
            if(Tools.isInt(spawnEntry.getKey())) {
                index = Integer.parseInt(spawnEntry.getKey());
                Location loc = Tools.getLocation(world, spawnList.getVector(spawnEntry.getKey(), new Vector()));
                worldSpawns.put(index, loc);
            }
        }
        index = 0;
        // Put the spawns in order.
        while(!worldSpawns.isEmpty()) {
            if(worldSpawns.containsKey(index)) {
                aData.spawns.put(index, worldSpawns.get(index));
                worldSpawns.remove(index);
            }
            index++;
        }
        spawnProps.newLine();
        spawnProps.comment("-- Deathmatch Spawns --");
        spawnProps.newLine();
        aData.dmCenter = Tools.getLocation(world, spawnProps.getVector("dmCenter", worldSpawnPos, "Used in deathmatch to keep players in."));
        aData.dmSpectatorSpawn = Tools.getLocation(world, spawnProps.getVector("dmSpectatorSpawn", worldSpawnPos));
        spawnProps.comment("Leave everything blank to use arena spawns for deathmatch.");
        PropertyList dmSpawnList = spawnProps.getList("deathmatch spawns");
        HashMap<Integer, Location> dmSpawns = new HashMap<Integer, Location>();
        for(PropertyEntry spawnEntry : dmSpawnList.getProperties()){
            if(Tools.isInt(spawnEntry.getKey())) {
                index = Integer.parseInt(spawnEntry.getKey());
                Location loc = Tools.getLocation(world, dmSpawnList.getVector(spawnEntry.getKey(), new Vector(0, 0, 0)));
                dmSpawns.put(index, loc);
            }
        }
        index = 0;
        // Put the spawns in order.
        while(!dmSpawns.isEmpty()) {
            if(dmSpawns.containsKey(index)) {
                aData.dmSpawns.put(index, dmSpawns.get(index));
                dmSpawns.remove(index);
            }
            index++;
        }
        if(aData.spawns.isEmpty()) {
            Logger.warning("No spawns have been set in '" + aData.name + "'. Arena disabled.");
            aData.enabled = false;
        }
        return aData;
    }
    
    private ArenaData loadArenaWhiteList(ArenaData aData) {
        try {
            File worldFolder = new File(plugin.getDataFolder().getPath() + "/" + aData.worldName);
            if(!worldFolder.exists()) {
                worldFolder.mkdirs();
            }
            if(!worldFolder.exists()) {
                worldFolder.mkdirs();
            }
            File configFile = new File(plugin.getDataFolder().getPath() + "/" + aData.worldName, blockConfig + ".cfg");
            blockFiles.put(aData.worldName, new PropertyFile(aData.worldName + "/" + blockConfig, configFile));
            blockFiles.get(aData.worldName).loadProperties();
            PropertyList spawnProps = blockFiles.get(aData.worldName).getProperties();
            aData = setupArenaWhiteList(spawnProps, aData);
            blockFiles.get(aData.worldName).saveProperties();
            if (aData.enabled && !settings.LowDetailMode) {
                Logger.info("    " + aData.worldName + " - block whitelist loaded. (" + blockConfig + ".cfg)");
            }
        } catch (PropertyException pe) {
            if(aData.enabled)
                Logger.error(pe.getMessage());
        } catch(Exception ex) {
            if(aData.enabled) {
                Logger.error("Could not load " + aData.worldName + "/" + blockConfig + ".cfg file.");
                ex.printStackTrace();
            }
        }
        return aData;
    }
    
    private ArenaData setupArenaWhiteList(PropertyList blockProps, ArenaData aData) {
        blockProps.comment("-- Blocks --");
        blockProps.newLine();
        PropertyList placeList = blockProps.getList("place");
        for(Material mat : Material.values()) {
            if(mat.isBlock()) {
                boolean allowed = placeList.getBoolean(mat.name(), blockPlaceDefault(mat));
                if(allowed)
                    aData.placeWhitelist.add(mat);
                else 
                    aData.placeWhitelist.remove(mat);
            }
        }
        PropertyList breakList = blockProps.getList("break");
        for(Material mat : Material.values()) {
            if(mat.isBlock()) {
                boolean allowed = breakList.getBoolean(mat.name(), blockBreakDefault(mat));
                if(allowed)
                    aData.breakWhitelist.add(mat);
                else 
                    aData.breakWhitelist.remove(mat);
            }
        }
        return aData;
    }
    
    private boolean blockPlaceDefault(Material mat) {
        return mat == Material.TNT
                || mat == Material.WEB
                || mat == Material.FIRE
                || mat == Material.CAKE_BLOCK;
    }
    
    private boolean blockBreakDefault(Material mat) {
        return mat == Material.LEAVES
                || mat == Material.VINE
                || mat == Material.RED_MUSHROOM
                || mat == Material.BROWN_MUSHROOM
                || mat == Material.WEB
                || mat == Material.CAKE_BLOCK;
    }
    
    public List<Player> getEditors() {
        List<Player> editorList = new ArrayList<Player>();
        for(String name : editors.keySet()) {
            editorList.add(Bukkit.getPlayer(name));
        }
        return editorList;
    }
    
    public List<Player> getEditors(String wName) {
        List<Player> editorList = new ArrayList<Player>();
        for(String pName : editors.keySet()) {
            if(editors.get(pName).equals(wName)) {
                if(Bukkit.getPlayer(pName) != null)
                    editorList.add(Bukkit.getPlayer(pName));
            }
        }
        return editorList;
    }
    
    public boolean isEditor(String pName) {
        return editors.containsKey(pName);
    }
    
    public boolean isEditing(String pName, String wName) {
        return editors.containsKey(pName) && editors.get(pName).equals(wName);
    }
    
    public void startEditting(String pName, String wName) {
        editors.put(pName, wName);
        if(get(wName).enabled) {
            // Disable
            disableArena(wName, true);
            // Remove from voting
            arenaOrder.remove(wName);
            // Make sure the world is loaded.
            loadWorld(wName);
        }
    }
    
    public void finishEditting(String pName) {
        final String wName = editors.get(pName);
        editors.remove(pName);
        if(getEditors(wName).isEmpty()) {
            World world = plugin.getServer().getWorld(wName);
            Player player = plugin.getServer().getPlayer(pName);
            ArenaData aData = get(wName);
            if(aData != getLobby()) {
                Logger.info("Saving '" + aData.worldName + "'...");
                world.save();
                new GameTask(plugin, 2L) {
                    @Override
                    public void run() {
                        createWorldZips(wName, true);
                    }
                };
            }
            enableArena(aData.worldName, true);
            if(player != null) {
                player.setGameMode(GameMode.SURVIVAL);
                player.setFlying(false);
                player.setAllowFlight(false);
                if(world.getName().equals(wName))
                    player.teleport(plugin.getArenaManager().getLobby().spectatorSpawn);
                Language.setVar("worldname", aData.worldName);
                Language.setVar("arenaname", aData.name);
                Language.sendLanguage(player, Language.LangKey.adminSave);
            }
        }
    }
    
    public void cancelEditting(String pName) {
        final String wName = editors.get(pName);
        editors.remove(pName);
        if(getEditors(wName).isEmpty()) {
            World world = plugin.getServer().getWorld(wName);
            Player player = plugin.getServer().getPlayer(pName);
            ArenaData aData = get(wName);
            if(aData != getLobby()) {
                Logger.info("Discarding '" + aData.worldName + "' changes...");
                resetArena(wName);
            }
            enableArena(aData.worldName, true);
            if(player != null) {
                player.setGameMode(GameMode.SURVIVAL);
                player.setFlying(false);
                player.setAllowFlight(false);
                if(world.getName().equals(wName))
                    player.teleport(plugin.getArenaManager().getLobby().spectatorSpawn);
                Language.setVar("worldname", aData.worldName);
                Language.setVar("arenaname", aData.name);
                Language.sendLanguage(player, Language.LangKey.adminSave);
            }
        }
    }
    
    public VoteManager getVoteManager() {
        return voteMan;
    }

    public ArenaData getLastArena() {
        return plugin.getArenaManager().get(LASTWORLD);
    }

    public void setLastArena(ArenaData aData) {
        LASTWORLD = aData.worldName;
    }

    public void setLastArena(String worldFolder) {
        LASTWORLD = worldFolder;
    }

    public ArenaData getCurrentArena() {
        return plugin.getArenaManager().get(WORLD);
    }

    public void setCurrentArena(ArenaData aData) {
        WORLD = aData.worldName;
    }

    public void setCurrentArena(String worldName) {
        WORLD = worldName;
    }

    public ArenaData getNextArena() {
        return plugin.getArenaManager().get(NEXTWORLD);
    }

    public void setNextArena(ArenaData aData) {
        NEXTWORLD = aData.worldName;
    }

    public void setNextArena(String worldFolder) {
        NEXTWORLD = worldFolder;
    }
    
    public ArenaData getLobby() {
        return lobby;
    }
    
    public void setLobbySpawn(Location loc) {
        lobby.spectatorSpawn = loc;
        PropertyEntry entry = arenaFile.getProperty("lobby.lobbySpawn");
        entry.setValue(Tools.toString(loc, false));
        arenaFile.saveProperties();
    }
    
    public void setSpawn(String wName, int spawnNumber, Location spawnLoc) {
        ArenaData aData = this.get(wName);
        aData.spawns.put(spawnNumber, spawnLoc);
        // Hard part...
        PropertyList list = (PropertyList) spawnFiles.get(wName).getProperty("spawns");
        if(list != null)
            list.setVector(spawnNumber + "", Tools.getVector(spawnLoc, false));
        spawnFiles.get(wName).saveProperties();
    }
    
    public void removeSpawn(String wName, int spawnNumber) {
        ArenaData aData = this.get(wName);
        if(aData.spawns.containsKey(spawnNumber)) {
            aData.spawns.remove(spawnNumber);
        }
        // Hard part...
        PropertyEntry entry = spawnFiles.get(wName).getProperty("spawns");
        if(entry != null && entry instanceof PropertyList) {
            ((PropertyList) entry).removeProperty(spawnNumber + "");
            spawnFiles.get(wName).saveProperties();
        }
    }
    
    public List<Location> getSpawnsInOrder(String arenaName) {
        HashMap<Integer, Location> spawns = get(arenaName).spawns;
        List<Location> spawnsInOrder = new ArrayList<Location>();
        int maxKey = 0;
        for(int key : spawns.keySet())
            if(key > maxKey)
                maxKey = key;
        for(int i = 0; i <= maxKey; i++) {
            if(spawns.containsKey(i))
                spawnsInOrder.add(spawns.get(i));
        }
        return spawnsInOrder;
    }
    
    public void setSpectatorSpawn(String wName, Location loc) {
        ArenaData aData = this.get(wName);
        aData.spectatorSpawn = loc;
        PropertyEntry entry = spawnFiles.get(wName).getProperty("spectatorSpawn");
        entry.setValue(Tools.toString(loc, false));
        spawnFiles.get(wName).saveProperties();
    }
    
    public void setDMCenter(String wName, Location loc) {
        ArenaData aData = this.get(wName);
        aData.dmCenter = loc;
        PropertyEntry entry = spawnFiles.get(wName).getProperty("dmCenter");
        entry.setValue(Tools.toString(loc, false));
        spawnFiles.get(wName).saveProperties();
    }
    
    public void setDMSpectatorSpawn(String wName, Location loc) {
        ArenaData aData = this.get(wName);
        aData.dmSpectatorSpawn = loc;
        PropertyEntry entry = spawnFiles.get(wName).getProperty("dmSpectatorSpawn");
        entry.setValue(Tools.toString(loc, false));
        spawnFiles.get(wName).saveProperties();
    }
    
    public void setDMSpawn(String wName, int spawnNumber, Location spawnLoc) {
        ArenaData aData = this.get(wName);
        aData.dmSpawns.put(spawnNumber, spawnLoc);
        // Hard part...
        PropertyList list = (PropertyList) spawnFiles.get(wName).getProperty("deathmatch spawns");
        if(list != null)
            list.setVector(spawnNumber + "", Tools.getVector(spawnLoc, false));
        spawnFiles.get(wName).saveProperties();
    }
    
    public void removeDMSpawn(String wName, int spawnNumber) {
        ArenaData aData = this.get(wName);
        if(aData.dmSpawns.containsKey(spawnNumber)) {
            aData.dmSpawns.remove(spawnNumber);
        }
        // Hard part...
        PropertyEntry entry = spawnFiles.get(wName).getProperty("deathmatch spawns");
        if(entry != null && entry instanceof PropertyList) {
            ((PropertyList) entry).removeProperty(spawnNumber + "");
            spawnFiles.get(wName).saveProperties();
        }
    }
    
    public List<Location> getDMSpawnsInOrder(String arenaName) {
        HashMap<Integer, Location> spawns = this.get(arenaName).dmSpawns;
        List<Location> spawnsInOrder = new ArrayList<Location>();
        int maxKey = 0;
        for(int key : spawns.keySet())
            if(key > maxKey)
                maxKey = key;
        for(int i = 0; i < maxKey; i++) {
            if(spawns.containsKey(i))
                spawnsInOrder.add(spawns.get(i));
        }
        return spawnsInOrder;
    }

    public void randomizeArenaOrder() {
        List<String> worlds = new ArrayList<String>();
        arenaOrder.clear();
        // Add enabled worlds.
        for (String key : this.keySet()) {
            if (get(key).enabled && !key.equals(lobby.worldName)) {
                worlds.add(key);
            }
        }
        // Add the worlds to arena order in random order.
        while (!worlds.isEmpty() && arenaOrder.size() < settings.maxVoteList) {
            Random rand = plugin.getRandom();
            int randIndex = rand.nextInt(worlds.size());
            if(!worlds.get(randIndex).equals(LASTWORLD)) {
                arenaOrder.add(worlds.get(randIndex));
                worlds.remove(randIndex);
            }
        }
        if(arenaOrder.size() < settings.maxVoteList && !worlds.isEmpty()) {
            arenaOrder.add(worlds.get(0)); // Add the last played world anyways.
        }
        if(!arenaOrder.isEmpty())
            setNextArena(arenaOrder.get(0));
    }

    public void refillContainers() {
        lootedContainers.clear();
    }

    public boolean canFillContainer(Block block) {
        return !lootedContainers.containsKey(block);
    }

    public void fillContainer(Block block, Inventory inv) {
        ArenaData aData = get(block.getWorld().getName());
        if(!aData.containers.isEmpty()) {
            ContainerData cData = aData.containers.get(block.getType());
            lootedContainers.put(block, inv);
            Random rand = plugin.getRandom();
            int min = cData.minChestRewards;
            int max = cData.maxChestRewards;
            if(max > inv.getSize())
                max = inv.getSize();
            int rewardsLeft = min + (int)(rand.nextDouble() * (max - min));
            while (rewardsLeft > 0) {
                RewardData reward = getRandomReward(cData);
                int slot = rand.nextInt(inv.getSize());
                if (inv.getContents()[slot] == null) {
                    int amount = reward.min + (int)(rand.nextDouble() * (reward.max - reward.min));
                    // FLint and steel should be damaged.
                    ItemStack item = new ItemStack(reward.item, amount);
                    if(reward.item == Material.FLINT_AND_STEEL)
                        item.setDurability((short) (Material.FLINT_AND_STEEL.getMaxDurability() - aData.lighterUses));
                    else
                        item.setDurability(reward.data);
                    inv.setItem(slot, item);
                    rewardsLeft--;
                }
            }
        }
    }

    public void clearContainer(Block block) {
        if(lootedContainers.containsKey(block)) {
            Inventory inv = lootedContainers.get(block);
            inv.setContents(new ItemStack[inv.getSize()]);
        }
    }

    public void clearContainers() {
        for (Block block : lootedContainers.keySet()) {
            clearContainer(block);
        }
    }

    private RewardData getRandomReward(ContainerData cData) {
        RewardData treasure = null;
        if(getCurrentArena() != null) {
            Random rand = plugin.getRandom();

            while (treasure == null && cData != null && !cData.rewards.isEmpty()) {
                RewardData reward = (RewardData) cData.rewards.get(rand.nextInt(cData.rewards.size()));
                if (rand.nextInt(101) <= reward.rarity) {
                    treasure = reward;
                }
            }
        }
        return treasure;
    }
    
    public boolean addArena(String wName) {
        File worldFolder = new File(wName);
        boolean added = false;
        if(worldFolder.isDirectory()) {
            // Save to config
            for(PropertyEntry entry : arenaFile.getProperties().getProperties()) {
                if(entry instanceof PropertyList && entry.getKey().equals("worlds")) {
                    PropertyList worldList = (PropertyList) entry;
                    if(!worldList.containsProperty(wName)){
                        PropertyList arenaProps = worldList.getList(wName, "Your world folder name.");
                        ArenaData aData = setupArena(arenaProps, wName);
                        this.put(wName, aData);
                        added = true;
                    }
                }
            }
            if(added)
                arenaFile.saveProperties();
        }
        return added;
    }
    
    public boolean removeArena(String wName) {
        File worldFolder = new File(wName);
        boolean removed = false;
        if(worldFolder.isDirectory()) {
            // Save to config
            for(PropertyEntry entry : arenaFile.getProperties().getProperties()) {
                if(entry instanceof PropertyList && entry.getKey().equals("worlds")) {
                    PropertyList worldList = (PropertyList) entry;
                    if(worldList.containsProperty(wName)) {
                        disableArena(wName, true);
                        unloadWorld(wName, true);
                        worldList.removeProperty(wName);
                        this.remove(wName);
                        removed = true;
                    }
                }
            }
            if(removed)
                arenaFile.saveProperties();
        }
        return removed;
    }

    public void enableArena(String wName, boolean show) {
        ArenaData aData = get(wName);
        if (aData != null) {
            if (!aData.enabled) {
                aData.enabled = true;
                // Save to config
                if(aData != lobby) {
                    PropertyEntry entry = arenaFile.getProperty("worlds." + aData.worldName + ".enabled");
                    entry.setValue("true");
                    arenaFile.saveProperties();
                    if(arenaOrder.size() < plugin.getSettings().maxVoteList)
                        arenaOrder.add(wName);
                }
                if(show) {
                    Language.setVar("arenaname", aData.name);
                    Language.broadcastLanguage(Language.LangKey.arenaLoaded);
                }
                plugin.getGameManager().resetScoreBoards();
            }
        }
    }

    public void disableArena(String wName, boolean show) {
        ArenaData aData = get(wName);
        if (aData != null) {
            if (aData.enabled) {
                aData.enabled = false;
                // Save to config
                if(aData != lobby) {
                    PropertyEntry entry = arenaFile.getProperty("worlds." + aData.worldName + ".enabled");
                    entry.setValue("false");
                    arenaFile.saveProperties();
                    arenaOrder.remove(wName);
                }
                if(show) {
                    Language.setVar("arenaname", aData.name);
                    Language.broadcastLanguage(Language.LangKey.arenaUnloaded);
                }
                plugin.getGameManager().resetScoreBoards();
            }
        }
    }
    
    public boolean isResetting() {
        for(ArenaData aData : this.values()) {
            if(aData.resetting)
                return true;
        }
        return false;
    }
    
    public boolean isResetting(String wName) {
        return containsKey(wName) && get(wName).resetting;
    }
    
    public void resetArena(String wName) {
        final String fwName = wName;
        if(containsKey(fwName)) {
            // Start resetting the arena.
            get(fwName).resetting = true;
            // Disable the arena.
            disableArena(fwName, false);
            // Register the world unload listener.
            plugin.getArenaManager().extractWorldZips(fwName);
                            
            // Make a task that waits until the task is done.
            new GameTask(plugin, 10L, 5L) { // Check every 1/4th of a second.
            	@Override
            	public void run() {
            		if((progressMonitors.containsKey(fwName)
            				&& progressMonitors.get(fwName).getResult() != ProgressMonitor.RESULT_WORKING)
            				/**&& (progressMonitors.containsKey(fwName + ".level")
                                                && progressMonitors.get(fwName + ".level").getResult() != ProgressMonitor.RESULT_WORKING)
                                            && (progressMonitors.containsKey(fwName + ".uid")
                                                && progressMonitors.get(fwName + ".uid").getResult() != ProgressMonitor.RESULT_WORKING)
                                            && (progressMonitors.containsKey(fwName + ".levelOld")
                                                && progressMonitors.get(fwName + ".levelOld").getResult() != ProgressMonitor.RESULT_WORKING)**/) {
                                        
                                        	if(progressMonitors.containsKey(fwName))
                                        		progressMonitors.remove(fwName);
                                        /**if(progressMonitors.containsKey(fwName + ".level"))
                                            progressMonitors.remove(fwName + ".level");
                                        if(progressMonitors.containsKey(fwName + ".levelOld"))
                                            progressMonitors.remove(fwName + ".levelOld");
                                        if(progressMonitors.containsKey(fwName + ".uid"))
                                            progressMonitors.remove(fwName + ".uid");**/
                                        
                                        ArenaManager arenaMan = plugin.getArenaManager();
                                        // Enable the world.
                                        arenaMan.enableArena(fwName, false);
                                        // Finish the resetting.
                                        arenaMan.get(fwName).resetting = false;
                                        setCancelled(true);
                                    }
                                }
                            };
        }
    }

    public void loadWorld(String wName) {
        Logger.info("Loading world '" + wName + "'...");
        WorldCreator creator = new WorldCreator(wName);
        creator.generateStructures(false);
        creator.type(WorldType.FLAT);
        creator.seed(0);
        plugin.getServer().createWorld(creator);
        if(!settings.LowDetailMode)
            Logger.info("    World '" + wName + "' loaded.");
    }

    public void unloadWorld(String wName, boolean save) {
        ArenaData aData = get(wName);
        World world = plugin.getServer().getWorld(aData.worldName);
        if (world != null) {
            Logger.info("Unloading world '" + aData.name + "'... (" + aData.worldName + ")");
            Logger.info("    Unloading chunks...");
            
            int chunkCount = 0;
            for(Chunk chunk : world.getLoadedChunks()) {
                world.unloadChunk(chunk);
                chunkCount++;
            }
            if(!settings.LowDetailMode)
                Logger.info("    " + chunkCount + " chunks unloaded.");
            plugin.getServer().unloadWorld(wName, save);
            if(!settings.LowDetailMode)
                Logger.info("World '" + aData.name + "' unloaded. (" + aData.worldName + ")");
        }
    }
    
    public void createWorldZips(String worldName, boolean overwrite) {
    	File regionFile = new File(worldName + ".zip");
        if(regionFile.exists() && !overwrite) {
            if(!settings.LowDetailMode)
                Logger.info("    Using existing world zip '" + worldName + "'.");
        } else {
            deleteFile(regionFile);
            if(!settings.LowDetailMode) {
                Logger.info("    Existing '" + worldName + "' world zip deleted.");
            }
            Logger.info("    Creating world zip '" + worldName + ".zip'...");
            try {
                long startTime = System.currentTimeMillis();
                ZipFile regionZip = new ZipFile(regionFile);
                regionZip.setRunInThread(false);
                regionZip.createZipFileFromFolder(new File(worldName), new ZipParameters(), false, 0);
                if(!settings.LowDetailMode)
                    Logger.info("        Created world zip '" + worldName + ".zip' in " + (System.currentTimeMillis() - startTime) + "ms.");
            } catch(Exception ex) {
                Logger.error("Unable to create zip for world '" + worldName + "'.");
                Logger.error(ex.getMessage());
            }
        }
        
        /**File regionFile = new File(worldName + "/region.zip");
        if(regionFile.exists() && !overwrite) {
            if(!settings.LowDetailMode)
                Logger.info("    Using existing world region zip '" + worldName + "'.");
        } else {
            deleteFile(regionFile);
            if(!settings.LowDetailMode) {
                Logger.info("    Existing '" + worldName + "' world region zip deleted.");
            }
            Logger.info("    Creating world region zip '" + worldName + "/region.zip'...");
            try {
                long startTime = System.currentTimeMillis();
                ZipFile regionZip = new ZipFile(regionFile);
                regionZip.setRunInThread(false);
                regionZip.createZipFileFromFolder(new File(worldName + "/region"), new ZipParameters(), false, 0);
                if(!settings.LowDetailMode)
                    Logger.info("        Created world region zip '" + worldName + "/region.zip' in " + (System.currentTimeMillis() - startTime) + "ms.");
            } catch(Exception ex) {
                Logger.error("Unable to create region zip for world '" + worldName + "'.");
                Logger.error(ex.getMessage());
            }
        }
        
        File levelZipFile = new File(worldName + "/level.dat.zip");
        if(levelZipFile.exists() && !overwrite) {
            if(!settings.LowDetailMode)
                Logger.info("    Using existing world level zip '" + worldName + "'.");
        } else {
            deleteFile(levelZipFile);
            if(!settings.LowDetailMode)
                Logger.info("    Existing '" + worldName + "' world level zip deleted.");
            Logger.info("    Creating world level zip '" + worldName + "/level.dat.zip'...");
            try {
                long startTime = System.currentTimeMillis();
                ZipFile levelZip = new ZipFile(worldName + "/level.dat.zip");
                levelZip.setRunInThread(false);
                levelZip.createZipFile(new File(worldName + "/level.dat"), new ZipParameters());
                if(!settings.LowDetailMode)
                    Logger.info("        Created world level zip '" + worldName + "/level.dat.zip' in " + (System.currentTimeMillis() - startTime) + "ms.");
            } catch(Exception ex) {
                Logger.error("Unable to create level zip for world '" + worldName + "'.");
                Logger.error(ex.getMessage());
            }
        }
        
        File levelOldFile = new File(worldName + "/level.dat_old");
        if(levelOldFile.exists()) {
            File levelOldZipFile = new File(worldName + "/level.dat_old.zip");
            if(levelOldZipFile.exists() && !overwrite) {
                if(!settings.LowDetailMode)
                    Logger.info("    Using existing world level old zip '" + worldName + "'.");
            } else {
                deleteFile(levelOldZipFile);
                if(!settings.LowDetailMode)
                    Logger.info("    Existing '" + worldName + "' world level old zip deleted.");
                Logger.info("    Creating world level old zip '" + worldName + "/level.dat_old.zip'...");
                try {
                    long startTime = System.currentTimeMillis();
                    ZipFile levelOldZip = new ZipFile(worldName + "/level.dat_old.zip");
                    levelOldZip.setRunInThread(false);
                    levelOldZip.createZipFile(new File(worldName + "/level.dat_old"), new ZipParameters());
                    if(!settings.LowDetailMode)
                        Logger.info("        Created world level old zip '" + worldName + "/level.dat_old.zip' in " + (System.currentTimeMillis() - startTime) + "ms.");
                } catch(Exception ex) {
                    Logger.error("Unable to create level old zip for world '" + worldName + "'.");
                    Logger.error(ex.getMessage());
                }
            }
        }
        
        File uidZipFile = new File(worldName + "/uid.dat.zip");
        if(uidZipFile.exists() && !overwrite) {
            if(!settings.LowDetailMode)
                Logger.info("    Using existing world uid zip '" + worldName + "'.");
        } else {
            deleteFile(uidZipFile);
            if(!settings.LowDetailMode)
                Logger.info("    Existing '" + worldName + "' world uid zip deleted.");
            Logger.info("    Creating world uid zip '" + worldName + "/uid.dat.zip'...");
            try {
                long startTime = System.currentTimeMillis();
                ZipFile uidZip = new ZipFile(worldName + "/uid.dat.zip");
                uidZip.setRunInThread(false);
                uidZip.createZipFile(new File(worldName + "/uid.dat"), new ZipParameters());
                if(!settings.LowDetailMode)
                    Logger.info("        Created world uid zip '" + worldName + "/uid.dat.zip' in " + (System.currentTimeMillis() - startTime) + "ms.");
            } catch(Exception ex) {
                Logger.error("Unable to create uid zip for world '" + worldName + "'.");
                Logger.error(ex.getMessage());
            }
        } **/
    }
    
    public void extractWorldZips(String worldName) {
    	Logger.info("    Extracting world zip '" + worldName + ".zip'...");
        try {
            File regionFolder = new File(worldName);
            ZipFile regionZip = new ZipFile(worldName + ".zip");
            if(regionZip.getFile().exists()) {
                if(regionFolder.isDirectory() || regionFolder.exists()) {
                    deleteDirectory(new File(worldName), true);
                    Logger.info("        Existing world folder deleted.");
                }
                long startTime = System.currentTimeMillis();
                regionZip.setRunInThread(true);
                //if (!regionFolder.exists())
                	//regionFolder.mkdir();
                regionZip.extractAll(".");
                progressMonitors.put(worldName, regionZip.getProgressMonitor());
                if(!settings.LowDetailMode)
                    Logger.info("        Finished extracting world zip '" + worldName + ".zip' in " + (System.currentTimeMillis() - startTime) + "ms.");
            }
        } catch(Exception ex) {
            Logger.error("Unable to unzip world zip! (" + worldName + ".zip)");
            ex.printStackTrace();
        }
        
    	/**Logger.info("    Extracting world region zip '" + worldName + "/region.zip'...");
        try {
            File regionFolder = new File(worldName + "/region");
            ZipFile regionZip = new ZipFile(worldName + "/region.zip");
            if(regionZip.getFile().exists()) {
                if(regionFolder.isDirectory() || regionFolder.exists()) {
                    deleteDirectory(regionFolder, true);
                    Logger.info("        Existing world region folder deleted.");
                }
                long startTime = System.currentTimeMillis();
                regionZip.setRunInThread(true);
                regionZip.extractAll(worldName + "/");
                progressMonitors.put(worldName + ".region", regionZip.getProgressMonitor());
                if(!settings.LowDetailMode)
                    Logger.info("        Finished extracting world region zip '" + worldName + "/region.zip' in " + (System.currentTimeMillis() - startTime) + "ms.");
            }
        } catch(Exception ex) {
            Logger.error("Unable to unzip world region zip! (" + worldName + "/region.zip)");
            ex.printStackTrace();
        }
        
        Logger.info("    Extracting world level zip '" + worldName + "/level.dat.zip'...");
        try {
            File levelFile = new File(worldName + "/level.dat");
            ZipFile levelZip = new ZipFile(worldName + "/level.dat.zip");
            if(levelZip.getFile().exists()) {
                if(levelFile.exists()) {
                    deleteFile(levelFile);
                    Logger.info("        Existing world level file deleted.");
                }
                long startTime = System.currentTimeMillis();
                levelZip.setRunInThread(true);
                levelZip.extractAll(worldName + "/");
                progressMonitors.put(worldName + ".level", levelZip.getProgressMonitor());
                if(!settings.LowDetailMode)
                    Logger.info("        Finished extracting world level zip '" + worldName + "/level.dat.zip' in " + (System.currentTimeMillis() - startTime) + "ms.");
            }
        } catch(Exception ex) {
            Logger.error("Unable to unzip world level zip! (" + worldName + "/level.dat.zip)");
            ex.printStackTrace();
        }
        
        Logger.info("    Extracting world level old zip '" + worldName + "/level.dat_old.zip'...");
        try {
            File levelOldFile = new File(worldName + "/level.dat_old");
            ZipFile levelOldZip = new ZipFile(worldName + "/level.dat_old.zip");
            if(levelOldZip.getFile().exists()) {
                if(levelOldFile.exists()) {
                    deleteFile(levelOldFile);
                    Logger.info("        Existing world level old file deleted.");
                }
                long startTime = System.currentTimeMillis();
                levelOldZip.setRunInThread(true);
                levelOldZip.extractAll(worldName + "/");
                progressMonitors.put(worldName + ".levelOld", levelOldZip.getProgressMonitor());
                if(!settings.LowDetailMode)
                    Logger.info("        Finished extracting world level old zip '" + worldName + "/level.dat_old.zip' in " + (System.currentTimeMillis() - startTime) + "ms.");
            }
        } catch(Exception ex) {
            Logger.error("Unable to unzip world level old zip! (" + worldName + "/level.dat_old.zip)");
            ex.printStackTrace();
        }
        
        Logger.info("    Extracting world uid zip '" + worldName + "/uid.dat.zip'...");
        try {
            File uidFile = new File(worldName + "/uid.dat");
            ZipFile uidZip = new ZipFile(worldName + "/uid.dat.zip");
            if(uidZip.getFile().exists()) {
                if(uidFile.exists()) {
                    deleteFile(uidFile);
                    Logger.info("        Existing world uid file deleted.");
                }
                long startTime = System.currentTimeMillis();
                uidZip.setRunInThread(true);
                uidZip.extractAll(worldName + "/");
                progressMonitors.put(worldName + ".uid", uidZip.getProgressMonitor());
                if(!settings.LowDetailMode)
                    Logger.info("        Finished extracting world uid zip '" + worldName + "/uid.dat.zip' in " + (System.currentTimeMillis() - startTime) + "ms.");
            }
        } catch(Exception ex) {
            Logger.error("Unable to unzip world uid zip! (" + worldName + "/uid.dat.zip)");
            ex.printStackTrace();
        } **/
    }
    
    public void deleteDirectory(File folder, boolean waitTillAvailable) throws IOException {
        deleteDirectory(folder, waitTillAvailable, 3000);
    }
    
    public void deleteDirectory(File folder, boolean waitTillAvailable, long waitTimeout) throws IOException {
        if(folder.isDirectory()) {
            List<File> unavailableFiles = new ArrayList<File>();
            for(File file : folder.listFiles()) {
                if(!file.canWrite() && !deleteFile(file) && waitTillAvailable) {
                    Logger.info(file.getName() + " was unavailable.");
                    unavailableFiles.add(file);
                }
            }
            long startTime = System.currentTimeMillis();
            while(!unavailableFiles.isEmpty() && System.currentTimeMillis() - startTime < waitTimeout) {
                File unavailableFile = unavailableFiles.get(0);
                if(deleteFile(unavailableFile)) {
                    unavailableFiles.remove(unavailableFile);
                }
            }
            if(!unavailableFiles.isEmpty()) {
                throw new IOException(unavailableFiles.size() + " files were unavailable to delete.");
                        
            }
        }
        folder.delete();
    }
    
    public boolean deleteFile(File file) {
        boolean fileAvailable = false;
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            FileChannel channel = raf.getChannel();
            FileLock lock = channel.tryLock();
            if(lock != null) {
                Logger.info("Releasing " + file.getName() + "...");
                lock.release();
                Logger.info("Deleting " + file.getName() + "...");
                file.delete();
                fileAvailable = true;
            }
            raf.close();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return fileAvailable;
    }
}
