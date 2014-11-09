package net.shockverse.survivalgames;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.*;

import net.shockverse.survivalgames.core.Language;
import net.shockverse.survivalgames.core.Language.LangKey;
import net.shockverse.survivalgames.core.Logger;
import net.shockverse.survivalgames.core.Perms;
import net.shockverse.survivalgames.core.Settings;
import net.shockverse.survivalgames.core.Tools;
import net.shockverse.survivalgames.core.Treasury;
import net.shockverse.survivalgames.data.ArenaData;
import net.shockverse.survivalgames.extras.FireworkEffectPlayer;
import net.shockverse.survivalgames.extras.GameTask;
import net.shockverse.survivalgames.extras.ItemUtils;
import net.shockverse.survivalgames.extras.TaskManager;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.Vector;

public class GameManager implements Listener {
    
    public enum SGGameState {
        
        LOBBY,
        STARTING,
        GAME,
        PRE_DEATHMATCH,
        DEATHMATCH,
        GAME_OVER,
        RESETTING,
        RESTARTING
        
    }
    private SurvivalGames plugin;
    private ArenaManager arenaMan;
    private StatManager statMan;
    private TaskManager taskMan;
    private SGGameState STATE = SGGameState.LOBBY;
    private Scoreboard emptyScoreboard;
    private Scoreboard gameScoreboard;
    private Scoreboard lobbyScoreboard;
    private int gamesPlayed;
    public long nextEndGame;
    public long nextDeathmatch;
    public long nextGame;
    public long nextGameStart;
    public long gameStartTime;
    private boolean deatchMatchCounting;
    private long lastRefill;
    private int refillCount;
    public ArrayList<String> tributes;
    public ArrayList<Player> vanished;
    private ArrayList<String> spectators;
    private ArrayList<String> killedPlayers;
    public HashMap<String, PlayerDamage> damagecause;
    public HashMap<String, Integer> spectatorWatching;
    public ArrayList<String> grace;
    public HashMap<String, Integer> playerGames;
    public String name;
    public String version;
    public Player winner;

    public GameManager(SurvivalGames plugin) {
        this.plugin = plugin;
        this.arenaMan = plugin.getArenaManager();
        this.statMan = plugin.getStatManager();
        taskMan = plugin.getTaskManager();
        tributes = new ArrayList<String>();
        vanished = new ArrayList<Player>();
        killedPlayers = new ArrayList<String>();
        spectators = new ArrayList<String>();
        damagecause = new HashMap<String, PlayerDamage>();
        spectatorWatching = new HashMap<String, Integer>();
        grace = new ArrayList<String>();
        playerGames = new HashMap<String, Integer>();
        
        nextGame = System.currentTimeMillis() + (arenaMan.getLobby().gameTime * 1000);
        
        name = plugin.getServer().getClass().getPackage().getName();
        version = name.substring(name.lastIndexOf('.') + 1);
        
        schedulePeriodTasks();
        
        if(plugin.getSettings().enableScoreboard) {
            ScoreboardManager boardManager = plugin.getServer().getScoreboardManager();

            lobbyScoreboard = boardManager.getNewScoreboard();
            lobbyScoreboard.registerNewObjective("lobby", "dummy");

            gameScoreboard = boardManager.getNewScoreboard();
            gameScoreboard.registerNewObjective("game", "dummy");
            gameScoreboard.registerNewObjective("bounty", "dummy");

            if(!arenaMan.arenaOrder.isEmpty())
                updateScoreBoards();
        }
    }
    
    public void disable() {
        tributes.clear();
        spectators.clear();
        damagecause.clear();
        spectatorWatching.clear();
        grace.clear();
        playerGames.clear();
    }
    
    public void cancelTasks() {
        taskMan.clearTasks("stateMessage");
        taskMan.clearTasks("update");
        schedulePeriodTasks();
        taskMan.clearTasks("countdown");
        taskMan.clearTasks("grace");
        taskMan.clearTasks("game");
    }
    
    public SGGameState getState() {
        return STATE;
    }
    
    private void schedulePeriodTasks() {
        taskMan.addTask("update", new GameTask(plugin, 0, 20L){
            @Override
            public void run() {
                update();
            }
        });
        if (!arenaMan.arenaOrder.isEmpty()) {
            long stateRepeatTime = arenaMan.getCurrentArena().stateMessageTime * 20L;
            taskMan.addTask("stateMessage", new GameTask(plugin, 0 , stateRepeatTime) {
                @Override
                public void run() {
                    stateMessage();
                }
            });
        }
    }
    
    public void GracePeriodMessages() {
        if ((this.STATE == SGGameState.GAME) 
                || (this.STATE == SGGameState.STARTING)
                || (this.STATE == SGGameState.DEATHMATCH)) {
            if(!arenaMan.arenaOrder.isEmpty() 
                    && arenaMan.getCurrentArena().graceTime > 0) {
                taskMan.addTask("grace", new GameTask(plugin) {
                    
                    @Override
                    public void run() {
                        Language.setVar("time", arenaMan.getCurrentArena().graceTime + "");
                        Language.broadcastLanguage(LangKey.graceBegin);
                    }
                    
                });
                
                taskMan.addTask("grace", new GameTask(plugin, arenaMan.getCurrentArena().graceTime * 20L){

                    @Override
                    public void run() {
                        Language.setVar("time", arenaMan.getCurrentArena().graceTime + "");
                        Language.broadcastLanguage(LangKey.graceEnd);
                        grace.clear();
                    }
                    
                });
            }
        }
    }
    
    private void update() {
        if(nextGame - System.currentTimeMillis() < 0)
            nextGame = System.currentTimeMillis();
        if(nextDeathmatch - System.currentTimeMillis() < 0)
            nextDeathmatch = System.currentTimeMillis();
        if(nextEndGame - System.currentTimeMillis() < 0)
            nextEndGame = System.currentTimeMillis();
        if(!arenaMan.arenaOrder.isEmpty()) {
            if(arenaMan.getNextArena() == null)
                arenaMan.setNextArena(arenaMan.arenaOrder.get(0));
            if(getTributeNames().size() < arenaMan.getNextArena().minStartTributes) {
                plugin.getGameManager().nextGame = System.currentTimeMillis() + (arenaMan.getLobby().gameTime * 1000) + 200;
            }
            updateScoreBoards();
            switch (STATE) {
                case STARTING:
                    for(Player p : plugin.getServer().getWorld(arenaMan.getCurrentArena().worldName).getPlayers()) {
                        p.setLevel((int) ((nextGameStart - System.currentTimeMillis()) / 1000));
                    }
                    for (Player p : getSpectators()) {
                    	if (p.getGameMode() != GameMode.ADVENTURE) p.setGameMode(GameMode.ADVENTURE);
                    	if (!p.getAllowFlight()) p.setAllowFlight(true);
                    }
                    break;
                case LOBBY:
                    if (nextGame - System.currentTimeMillis() <= 0
                            && getTributeNames().size() >= arenaMan.getNextArena().minStartTributes) {
                        startGame(-1);
                    }
                    for(Player p : plugin.getServer().getWorld(arenaMan.getCurrentArena().worldName).getPlayers()) {
                        p.setLevel((int) ((nextGame - System.currentTimeMillis()) / 1000));
                    }
                    break;
                case GAME:
                    long worldTime = Bukkit.getWorld(arenaMan.getCurrentArena().worldName).getTime();
                    if(worldTime < lastRefill)
                        worldTime += 24000;
                    long difference = worldTime - lastRefill;
                    if(difference >= arenaMan.getCurrentArena().refillWorldTime
                            && refillCount < arenaMan.getCurrentArena().refillCount) {
                        lastRefill = Bukkit.getWorld(arenaMan.getCurrentArena().worldName).getTime();
                        refillCount++;
                        arenaMan.clearContainers();
                        arenaMan.refillContainers();
                        Language.broadcastLanguage(LangKey.chestsRefilled);
                    }

                    if (this.nextDeathmatch - System.currentTimeMillis() <= 0 
                            && !deatchMatchCounting) {
                        Language.broadcastLanguage(LangKey.timeLimitOver);
                        startDeathmatch();
                    }
                    if (getTributeNames().size() <= arenaMan.getCurrentArena().minTributes 
                            && grace.isEmpty()
                            && !deatchMatchCounting) {
                        Language.setVar("amount", arenaMan.getCurrentArena().minTributes + "");
                        Language.broadcastLanguage(LangKey.minTributesRemain);
                        startDeathmatch();
                    }
                    if (getTributeNames().size() <= 1 && grace.isEmpty()) {
                        endGame();
                    }
                    for (Player p : getSpectators()) {
                    	if (p.getGameMode() != GameMode.ADVENTURE) p.setGameMode(GameMode.ADVENTURE);
                    	if (!p.getAllowFlight()) p.setAllowFlight(true);
                    }
                    break;
                case PRE_DEATHMATCH:
                	for (Player p : getSpectators()) {
                    	if (p.getGameMode() != GameMode.ADVENTURE) p.setGameMode(GameMode.ADVENTURE);
                    	if (!p.getAllowFlight()) p.setAllowFlight(true);
                    }
                case DEATHMATCH:
                    if (getTributeNames().size() <= 1 && grace.isEmpty()) {
                        endGame();
                    }
                    for (Player p : getSpectators()) {
                    	if (p.getGameMode() != GameMode.ADVENTURE) p.setGameMode(GameMode.ADVENTURE);
                    	if (!p.getAllowFlight()) p.setAllowFlight(true);
                    }
                    break;
            }
            
            if(STATE == SGGameState.LOBBY) {
                // Move spectators into tribute if there is room.
                int count = 0;
                plugin.getDebug().everything("Checking for room or kick... (tributes:" + tributes.size() + " max:" + arenaMan.getNextArena().spawns.size() + " specs:" + spectators.size());
                while(tributes.size() < arenaMan.getNextArena().spawns.size()
                        && spectators.size() > 0 && count < 50) { /// Only loop a max of 50 times.
                    int highestKickLevel = 0;
                    Player bestChoice = null;
                    for(Player spec : getSpectators()) {
                        int kickLevel = getKickJoinLevel(spec);
                        if(kickLevel >= highestKickLevel) {
                            highestKickLevel = kickLevel;
                            bestChoice = spec;
                        }
                    }
                    if(bestChoice != null) {
                        setTribute(bestChoice);
                        bestChoice.setGameMode(GameMode.ADVENTURE);
                        bestChoice.setFlying(false);
                        bestChoice.setAllowFlight(false);
                        setVanished(bestChoice, false);
                        resetPlayer(bestChoice);
                        Language.setVar("rank", "tribute");
                        Language.setUser(bestChoice);
                        Language.sendLanguage(bestChoice, LangKey.youChangeRank);
                    }
                    count++;
                }
            }
            
        } else {
            if(STATE == SGGameState.LOBBY) {
                if(plugin.getSettings().enableScoreboard) {
                    if(emptyScoreboard == null)
                        emptyScoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard();
                    for(Player player : plugin.getServer().getOnlinePlayers()) {
                        if(player.getScoreboard() != emptyScoreboard)
                        player.setScoreboard(emptyScoreboard);
                    }
                }
                // Move tributes to spectator.
                for(Player tribute : getTributes()) {
                    setSpectator(tribute);
                    tribute.setGameMode(GameMode.SURVIVAL);
                    tribute.setFlying(false);
                    tribute.setAllowFlight(false);
                    setVanished(tribute, true);
                    resetPlayer(tribute);
                    Language.setVar("rank", "spectator");
                    Language.setUser(tribute);
                    Language.sendLanguage(tribute, LangKey.youChangeRank);
                }
            }
        }
        
        if(STATE == SGGameState.RESETTING && !arenaMan.isResetting()) {
            if (plugin.getSettings().restartMinutes > 0
                    && System.currentTimeMillis() > plugin.restartTime) {
                plugin.getServer().broadcastMessage(Language.getLanguage(LangKey.serverShutdown));
                taskMan.addTask("restart", new GameTask(plugin, 3 * 20L) {
                    @Override public void run() { restartServer(); }
                });
            } else if(plugin.getSettings().restartGames > 0 
                    && plugin.getSettings().restartGames <= gamesPlayed) {
                plugin.getServer().broadcastMessage(Language.getLanguage(LangKey.serverShutdown));
                taskMan.addTask("restart", new GameTask(plugin, 3 * 20L) {
                    @Override public void run() { restartServer(); }
                });
            }
        }
    }
    
    public void resetScoreBoards() {
        if(plugin.getSettings().enableScoreboard) {
            lobbyScoreboard.clearSlot(DisplaySlot.SIDEBAR);

            gameScoreboard.clearSlot(DisplaySlot.SIDEBAR);
            gameScoreboard.clearSlot(DisplaySlot.BELOW_NAME);
        }
    }
    
    public void updateScoreBoards() {
        if(plugin.getSettings().enableScoreboard) {
            if(getTributeNames().size() > 0) {
                Objective ob;
                Score score;
                String lang;

                ob = lobbyScoreboard.getObjective("lobby");
                ob.setDisplaySlot(DisplaySlot.SIDEBAR);

                Language.setVar("arenaname", plugin.getArenaManager().getCurrentArena().name);
                Language.setVar("worldname", plugin.getArenaManager().getCurrentArena().worldName);
                Language.setVar("arenanumber", (arenaMan.arenaOrder.indexOf(arenaMan.getCurrentArena().worldName) + 1) + "");
                ob.setDisplayName(Language.getLanguage(LangKey.scoreLobbyTitle));


                lang = Language.getLanguage(LangKey.scoreLobbyTime);
                if(lang.length() > 16)
                    lang = lang.substring(0, 14) + "..";
                if(!Tools.isNullEmptyWhite(lang)) {
                    score = ob.getScore(Bukkit.getOfflinePlayer(lang));
                    if(nextGame > System.currentTimeMillis())
                        score.setScore((int) ((nextGame - System.currentTimeMillis()) / 1000));
                    else
                        score.setScore(arenaMan.getLobby().gameTime);
                }

                for(String key : arenaMan.arenaOrder) {
                    Language.setVar("arenaname", arenaMan.get(key).name);
                    Language.setVar("worldname", key);
                    Language.setVar("arenanumber", (arenaMan.arenaOrder.indexOf(key) + 1) + "");
                    lang = Language.getLanguage(LangKey.scoreVoteArena);
                    if(lang.length() > 16)
                        lang = lang.substring(0, 14) + "..";
                    if(!Tools.isNullEmptyWhite(lang)) {
                        score = ob.getScore(Bukkit.getOfflinePlayer(lang));
                        score.setScore(plugin.getArenaManager().getVoteManager().totalVotes(key));
                    }
                }

                ob = gameScoreboard.getObjective("game");
                ob.setDisplaySlot(DisplaySlot.SIDEBAR);

                Language.setVar("arenaname", plugin.getArenaManager().getCurrentArena().name);
                Language.setVar("worldname", plugin.getArenaManager().getCurrentArena().worldName);
                Language.setVar("arenanumber", arenaMan.arenaOrder.indexOf(arenaMan.getCurrentArena().worldName) + "");
                ob.setDisplayName(Language.getLanguage(LangKey.scoreGameTitle));

                lang = Language.getLanguage(LangKey.scoreGameTime);
                if(lang.length() > 16)
                    lang = lang.substring(0, 14) + "..";
                if(!Tools.isNullEmptyWhite(lang)) {
                    score = ob.getScore(Bukkit.getOfflinePlayer(lang));
                    switch(STATE) {
                        case STARTING:
                            score.setScore((int) ((nextGameStart - System.currentTimeMillis()) / 1000));
                            break;
                        case GAME:
                            score.setScore((int) ((nextDeathmatch - System.currentTimeMillis()) / 1000));
                            break;
                    }
                }

                lang = Language.getLanguage(LangKey.scoreGameTributes);
                if(lang.length() > 16)
                    lang = lang.substring(0, 14) + "..";
                if(!Tools.isNullEmptyWhite(lang)) {
                    score = ob.getScore(Bukkit.getOfflinePlayer(lang));
                    score.setScore(getTributeNames().size());
                }


                lang = Language.getLanguage(LangKey.scoreGameSpectators);
                if(lang.length() > 16)
                    lang = lang.substring(0, 14) + "..";
                if(!Tools.isNullEmptyWhite(lang)) {
                    score = ob.getScore(Bukkit.getOfflinePlayer(lang));
                    score.setScore(getSpectatorNames().size());
                }


                ob = gameScoreboard.getObjective("bounty");
                ob.setDisplaySlot(DisplaySlot.BELOW_NAME);
                ob.setDisplayName(Language.getLanguage(LangKey.bountyTitle));

                for(Player p : plugin.getServer().getOnlinePlayers()) {
                    score = ob.getScore(p);
                    score.setScore(plugin.getStatManager().getBounty(p.getName()));
                }

                switch(STATE) {
                    case LOBBY:
                        for(Player p : plugin.getServer().getOnlinePlayers())
                            if(p.getScoreboard() != lobbyScoreboard)
                                p.setScoreboard(lobbyScoreboard);
                        break;
                    case STARTING:
                    case GAME:
                    case PRE_DEATHMATCH:
                    case DEATHMATCH:
                        for(Player p : plugin.getServer().getOnlinePlayers())
                            if(p.getScoreboard() != gameScoreboard)
                                p.setScoreboard(gameScoreboard);
                        break;
                }
            }
        }
    }
    
    private void stateMessage() {
        if(getTributeNames().size() > 0) {
            if(arenaMan.arenaOrder.size() > 0) {
                if (this.STATE == SGGameState.LOBBY) {
                    VoteManager voteMan = arenaMan.getVoteManager();
                    // Get other languages before setting variables.
                    String arenaVotesStr = "";

                    for (String key : arenaMan.arenaOrder) {
                        if (!key.equals(arenaMan.getLobby().worldName)) {
                            int votes = voteMan.containsKey(key) ? voteMan.totalVotes(key) : 0;
                            Language.setVar("arenanum", (arenaMan.arenaOrder.indexOf(key) + 1) + "");
                            Language.setVar("arenaname", arenaMan.get(key).name);
                            Language.setVar("arenavotes", votes + "");
                            arenaVotesStr += Language.getLanguage(LangKey.arenaVotes, true) + "\n";
                        }
                    }
                    if (arenaVotesStr.length() > 0) {
                        arenaVotesStr = arenaVotesStr.substring(0, arenaVotesStr.length() - 1);
                    }

                    // Set variables.
                    Language.setVar("arenavotes", arenaVotesStr);
                    Language.setVar("nextarena", arenaMan.getNextArena().name);

                    Language.setVar("waitingplayers", getTributeNames().size() + "");
                    Language.setVar("maxplayers", arenaMan.getNextArena().spawns.size() + "");
                    if (getTributeNames().size() >= arenaMan.getNextArena().minStartTributes) {
                        Language.setVar("secstillgame", (int) ((nextGame - System.currentTimeMillis()) / 1000) + "");
                        Language.setVar("timetillgame", Tools.getTime((int) ((nextGame - System.currentTimeMillis()))));
                        Language.setVar("fulltimetillgame", Tools.getFullTime((int) ((nextGame - System.currentTimeMillis()))));
                        Language.broadcastLanguage(LangKey.stateMessageLobby);
                    } else {
                        Language.setVar("playersneeded", (arenaMan.getNextArena().minStartTributes - getTributeNames().size()) + "");
                        Language.broadcastLanguage(LangKey.stateMessageLobbyWaiting);
                    }
                } else if (this.STATE == SGGameState.GAME) {
                    Language.setVar("tributesleft", getTributeNames().size() + "");
                    Language.setVar("secstilldm", (int) ((nextDeathmatch - System.currentTimeMillis()) / 1000) + "");
                    Language.setVar("timetilldm", Tools.getTime((int) ((nextDeathmatch - System.currentTimeMillis()))));
                    Language.setVar("fulltimetilldm", Tools.getFullTime((int) ((nextDeathmatch - System.currentTimeMillis()))));
                    Language.setVar("spectators", getSpectatorNames().size() + "");
                    Language.broadcastLanguage(LangKey.stateMessageGame);
                }
            }
        }
    }
    
    public void startGame(int seconds) {
        if (this.STATE == SGGameState.LOBBY && !arenaMan.arenaOrder.isEmpty()) {
            this.STATE = SGGameState.STARTING;
            arenaMan.setLastArena(arenaMan.getCurrentArena());
            arenaMan.setCurrentArena(arenaMan.getNextArena());
            gameStartTime = System.currentTimeMillis();
            
            ArenaData aData = arenaMan.getCurrentArena();
            
            if (seconds < 0)
                seconds = aData.gameCountdown;
            
            Language.setVar("arena", aData.name);
            Language.broadcastCustomLanguage(aData.arenaDetails, true);
            
            nextGameStart = System.currentTimeMillis() + (seconds * 1000);
            scheduleCountdown(seconds, LangKey.gameCountdown);
            
            World world = plugin.getServer().getWorld(aData.worldName);
            
            for(Entity entity : world.getEntities()) {
                if(entity.getType() != EntityType.PLAYER) {
                	if (version.equals("v1_7_R1"))
                	//	((org.bukkit.craftbukkit.v1_7_R1.entity.CraftEntity)entity).getHandle().die();
                	if (version.equals("v1_7_R2"))
                	//	((org.bukkit.craftbukkit.v1_7_R2.entity.CraftEntity)entity).getHandle().die();
                    if (version.equals("v1_7_R3"))
                        ((org.bukkit.craftbukkit.v1_7_R3.entity.CraftEntity)entity).getHandle().die();
                   // if (version.equals("v1_7_R4"))
                   //     ((org.bukkit.craftbukkit.v1_7_R4.entity.CraftEntity)entity).getHandle().die();
                }
            }
            
            world.setStorm(aData.stormy);
            world.setThundering(false);
            world.setThunderDuration(aData.gameTime * 60 * 20);
            world.setWeatherDuration(aData.gameTime * 60 * 20);
            world.setTime(aData.worldStartTime);
            
            
            arenaMan.clearContainers();
            arenaMan.refillContainers();
            lastRefill = Bukkit.getWorld(aData.worldName).getTime();
            
            int nextPad = 0; // Where to put the first player.
            double padSpacing = aData.spawns.size() / plugin.getGameManager().getTributeNames().size();
            List<Location> spawns = arenaMan.getSpawnsInOrder(aData.worldName);
            List<Player> tributesList = getTributes();
            for (int i = 0; i < getTributeNames().size(); i++) {
                // Determines the next place to put a player.
                if(i < getTributeNames().size()) {
                    nextPad = (int) (padSpacing * i);
                    if(tributesList.get(i) != null) {
                        Player tribute = tributesList.get(i);
                        tribute.setVelocity(new Vector());
                        if(nextPad == spawns.size())
                            nextPad = spawns.size() - 1;
                        final Location padLoc = spawns.get(nextPad);
                        final String pName = tribute.getName();
                        // Do each teleport when the server can.
                        new GameTask(plugin, i * 2L) {
                            @Override
                            public void run() {
                                Player tribute = plugin.getServer().getPlayer(pName);
                                tribute.teleport(padLoc);
                            }
                        };
                        setTribute(tribute);
                        tribute.setGameMode(GameMode.SURVIVAL);
                        tribute.setFlying(false);
                        tribute.setAllowFlight(false);
                        tribute.getInventory().clear();
                        tribute.getInventory().setHelmet(null);
                        tribute.getInventory().setChestplate(null);
                        tribute.getInventory().setLeggings(null);
                        tribute.getInventory().setBoots(null);
                        setVanished(tribute, false);
                        resetPlayer(tribute);
                    }
                }
            }
            List<Player> spectatorList = getSpectators();
            for (int i = 0; i < getSpectatorNames().size(); i++) {
                // Determines the next place to put a player.
                if(i < getSpectatorNames().size()) {
                    nextPad = (int) (padSpacing * i);
                    if(spectatorList.get(i) != null) {
                        Player spectator = spectatorList.get(i);
                        spectator.setVelocity(new Vector());
                        final Location padLoc = aData.spectatorSpawn;
                        final String pName = spectator.getName();
                        // Do each teleport when the server can. So vanishNoPacket doesn't break. :P
                        new GameTask(plugin, (int)(i / 5)) {
                            @Override
                            public void run() {
                                Player spectator = plugin.getServer().getPlayer(pName);
                                spectator.teleport(padLoc);
                            }
                        };
                        setSpectator(spectator);
                        spectator.setGameMode(GameMode.ADVENTURE);
                        spectator.setAllowFlight(true);
                        spectator.setFlying(true);
                        setVanished(spectator, true);
                        resetPlayer(spectator);
                    }
                }
            }
            
            taskMan.addTask("game", new GameTask(plugin, seconds * 20L) {

                @Override
                public void run() {
                    Language.broadcastLanguage(LangKey.gameHasBegun);
                    STATE = SGGameState.GAME;
                    // Reset level to 0 after level countdown.
                    for(Player p : plugin.getServer().getWorld(arenaMan.getCurrentArena().worldName).getPlayers()) {
                        p.setLevel(0);
                    }
                    if(arenaMan.getCurrentArena().graceTime > 0) {
                        grace.addAll(tributes);
                        GracePeriodMessages();
                    }
                    nextDeathmatch = (System.currentTimeMillis() + (arenaMan.getCurrentArena().gameTime * 1000));
                }
            });
        }
    }
    
    public void startDeathmatch() {
        // Ugly but needed.
        deatchMatchCounting = true;
        
        int seconds = arenaMan.getCurrentArena().deathMatchCountdown;

        // Delay the messages for the countdown.
        scheduleCountdown(seconds, LangKey.dmCountdown);

        // Set the state to pre_deathmatch so they won't be able to move. (20 seconds before deathmatch)
        taskMan.addTask("game", new GameTask(plugin, (seconds >= 20 ? seconds - 20 : 0) * 20L){
            @Override
            public void run() {
                STATE = SGGameState.PRE_DEATHMATCH;

                ArenaData aData = arenaMan.getCurrentArena();
                // Teleport the remaining tributes to a spawn pad.
                List<Location> spawns = arenaMan.getSpawnsInOrder(aData.worldName);
                if(!aData.dmSpawns.isEmpty())
                    spawns = arenaMan.getDMSpawnsInOrder(aData.worldName);
                int nextPad = 0; // Where to put the first player.
                // Determines how much to spread each player.
                double padSpacing = spawns.size() / plugin.getGameManager().getTributeNames().size();
                List<Player> tributesList = getTributes();
                for (int i = 0; i < tributesList.size(); i++) {
                    // Determines the next place to put a player.
                    Player tribute = tributesList.get(i);
                    nextPad = (int) (padSpacing * i);
                    if(tribute != null) {
                        tribute.setVelocity(new Vector());
                        if(nextPad == aData.spawns.size())
                            nextPad = spawns.size() - 1;
                        tribute.teleport(spawns.get(nextPad));
                        tribute.setVelocity(new Vector());
                    }
                }
                for (Player spec : getSpectators()) {
                    if (spec != null && !spec.getWorld().getName().equals(arenaMan.getLobby().worldName)) {
                        spec.teleport(arenaMan.getCurrentArena().dmCenter);
                    }
                }
            }
        });
        // Finally switch the state to deathmatch so they can move.
        taskMan.addTask("game", new GameTask(plugin, seconds * 20L) {
            @Override
            public void run() {
                Language.broadcastLanguage(LangKey.dmStart);
                nextEndGame = System.currentTimeMillis() + (arenaMan.getCurrentArena().deathMatchTime * 1000);
                STATE = SGGameState.DEATHMATCH;
            }
        });
    }
    
    public void endGame() {
        STATE = SGGameState.GAME_OVER;
        cancelTasks();
        
        ArenaData aData = arenaMan.getCurrentArena();
        if (getTributeNames().size() == 1) {
            winner = (Player) getTributes().get(0);
            if(winner != null) {
                Language.setTarget(winner);
                Language.broadcastLanguage(LangKey.tributeWon);
                gamesPlayed++;
                PlayerStats wStats = statMan.getPlayer(winner.getName());
                wStats.setWins(wStats.getWins() + 1);
                wStats.setWinStreak(wStats.getWinStreak() + 1);
                if(wStats.getWinStreak() > wStats.getBestWinStreak())
                    wStats.setBestWinStreak(wStats.getWinStreak());
                wStats.setLoseStreak(0);
                wStats.setGamesPlayed(wStats.getGamesPlayed() + 1);
                wStats.setTimePlayed(wStats.getTimePlayed() + (int) (System.currentTimeMillis() - gameStartTime));
                int points = aData.winPoints + statMan.getBounty(winner.getName());
                wStats.setPoints(wStats.getPoints() + points);
                Language.setVar("points", points + "");
                Language.setUser(winner);
                Language.sendLanguage(winner, LangKey.wonPoints);
                wStats.save();
                // Economy
                Treasury treasury = plugin.getTreasury();
                if(treasury.hasEconomy() && aData.moneyMultiplier > 0) {
                    treasury.getEconomy().depositPlayer(winner.getName(), points * aData.moneyMultiplier);
                }
                // Increment games played.
                int numGames = playerGames.containsKey(winner.getName()) ? playerGames.get(winner.getName()) : 0;
                playerGames.put(winner.getName(), numGames + 1);

                new BukkitRunnable() {
                    int cd = 10;

                    @Override
                    public void run() {
                        FireworkEffectPlayer.playToLocation(winner.getLocation(),
                                FireworkEffect.builder()
                                        .with(FireworkEffect.Type.BALL)
                                        .withColor(Color.fromRGB(
                                                plugin.getRandom().nextInt(255),
                                                plugin.getRandom().nextInt(255),
                                                plugin.getRandom().nextInt(255)))
                                        .withFlicker()
                                        .build());

                        if(cd == 0) {
                            this.cancel();
                        }
                        cd--;
                    }
                }.runTaskTimer(plugin, 0, 20);
            }
        } else if (nextEndGame < System.currentTimeMillis()) {
            // Tie!
            gamesPlayed++;

            if(!getTributeNames().isEmpty() && !killedPlayers.isEmpty()) {
                // Divide up the points. TODO: Config winning points.
                int tiePoints = (int) Math.ceil(aData.winPoints / (tributes.size() + killedPlayers.size()));

                for (final Player tiedPlayer : getTributes()) {
                    if(tiedPlayer != null) {
                        PlayerStats tStats = statMan.getPlayer(tiedPlayer.getName());
                        tStats.setWins(tStats.getWins() + 1);
                        tStats.setWinStreak(tStats.getWinStreak() + 1);
                        if(tStats.getWinStreak() > tStats.getBestWinStreak())
                            tStats.setBestWinStreak(tStats.getWinStreak());
                        tStats.setLoseStreak(0);
                        tStats.setGamesPlayed(tStats.getGamesPlayed() + 1);
                        tStats.setTimePlayed(tStats.getTimePlayed() + (int) (System.currentTimeMillis() - gameStartTime));
                        int points = aData.winPoints + statMan.getBounty(tiedPlayer.getName());
                        tStats.setPoints(tStats.getPoints() + tiePoints);
                        Language.setVar("points", points + "");
                        Language.setUser(tiedPlayer);
                        Language.sendLanguage(tiedPlayer, LangKey.wonPoints);
                        tStats.save();
                        
                        // Economy
                        Treasury treasury = plugin.getTreasury();
                        if(treasury.hasEconomy() && aData.moneyMultiplier > 0) {
                            treasury.getEconomy().depositPlayer(tiedPlayer.getName(), points * aData.moneyMultiplier);
                        }
                        
                        int numGames = playerGames.containsKey(tiedPlayer.getName()) ? playerGames.get(tiedPlayer.getName()) : 0;
                        playerGames.put(tiedPlayer.getName(), numGames + 1);

                        new BukkitRunnable() {
                            int cd = 10;

                            @Override
                            public void run() {
                                FireworkEffectPlayer.playToLocation(tiedPlayer.getLocation(),
                                        FireworkEffect.builder()
                                                .with(FireworkEffect.Type.BALL)
                                                .withColor(Color.fromRGB(
                                                        plugin.getRandom().nextInt(255),
                                                        plugin.getRandom().nextInt(255),
                                                        plugin.getRandom().nextInt(255)))
                                                .withFlicker()
                                                .build());

                                if(cd == 0) {
                                    this.cancel();
                                }
                                cd--;
                            }
                        }.runTaskTimer(plugin, 0, 20);
                    }
                }
            }
        } else {
            // Game ended premature?
        }
        taskMan.addTask("reset", new GameTask(plugin, plugin.getSettings().delayAfterGame * 20L) {
            @Override
            public void run() {
                reset();
            }
        });
    }
    
    public void reset() {
        STATE = SGGameState.RESETTING;
        plugin.getDebug().normal("Resetting...");
        plugin.getDebug().normal("Moving players....");
        Language.broadcastLanguage(LangKey.gameRestarting);
        World world = plugin.getServer().getWorld(arenaMan.getCurrentArena().worldName);
        HashMap<Integer, List<Player>> joinLevels = new HashMap<Integer, List<Player>>();
        // Teleport even admins incase arena is disabling.
        Player[] onlinePlayers = plugin.getServer().getOnlinePlayers();
        for (int i = 0; i < onlinePlayers.length; i++) {
            final Player p = onlinePlayers[i];
            if (p.hasPotionEffect(PotionEffectType.INVISIBILITY)) p.removePotionEffect(PotionEffectType.INVISIBILITY);
            if(p.getWorld() == world) {
                // Set them to spectators
                if(Perms.has(p, "survivalgames.admin.login", p.isOp())) {
                    Language.sendLanguage(p, LangKey.joinAdmin);
                } else {
                    int joinLevel = getKickJoinLevel(p);
                    if(!joinLevels.containsKey(joinLevel))
                        joinLevels.put(joinLevel, new ArrayList<Player>());
                    joinLevels.get(joinLevel).add(p);
                }
                final String fName = p.getName();
                new GameTask(plugin, (int)(i / 5)) {
                    @Override
                    public void run() {
                        Player fPlayer = plugin.getServer().getPlayer(fName);
                        if(fPlayer != null) {
                        	fPlayer.showPlayer(fPlayer);
                        	p.teleport(plugin.getArenaManager().getLobby().spectatorSpawn);
                            if((plugin.getSettings().restartMinutes > 0
                                && System.currentTimeMillis() > plugin.restartTime)
                                || (plugin.getSettings().restartGames > 0 
                                && plugin.getSettings().restartGames <= gamesPlayed)) {
                                // If the server is going to shut down then kick the players.
                                    if(plugin.getSettings().useBungee && !Tools.isNullEmptyWhite(plugin.getSettings().bungeeServer)) {
                                        try {
                                            ByteArrayOutputStream b = new ByteArrayOutputStream ();
                                            DataOutputStream out = new DataOutputStream (b);
                                            out.writeUTF("Connect");
                                            out.writeUTF(plugin.getSettings().bungeeServer);
                                            fPlayer.sendMessage(Language.getLanguage(LangKey.serverShutdown));
                                            fPlayer.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
                                        } catch(Exception ex) {
                                            Logger.warning("Unable to send player to Bungee server.");
                                            fPlayer.kickPlayer(Language.getLanguage(LangKey.serverShutdown));
                                        }
                                    } else {
                                        fPlayer.kickPlayer(Language.getLanguage(LangKey.serverShutdown));
                                    }
                            } else {
                                int numGames = playerGames.containsKey(fPlayer.getName()) ? playerGames.get(fPlayer.getName()) : 0;
                                if(plugin.getSettings().kickGames <= 0
                                        || numGames < plugin.getSettings().kickGames) {
                                    fPlayer.teleport(arenaMan.getLobby().spectatorSpawn);
                                    fPlayer.setGameMode(GameMode.ADVENTURE);
                                    fPlayer.setFlying(false);
                                    fPlayer.setAllowFlight(false);
                                } else if(numGames >= plugin.getSettings().kickGames){
                                    playerGames.remove(fPlayer.getName());
                                    if(plugin.getSettings().useBungee && !Tools.isNullEmptyWhite(plugin.getSettings().bungeeServer)) {
                                        try {
                                            ByteArrayOutputStream b = new ByteArrayOutputStream ();
                                            DataOutputStream out = new DataOutputStream (b);
                                            out.writeUTF("Connect");
                                            out.writeUTF(plugin.getSettings().bungeeServer);
                                            fPlayer.sendMessage(Language.getLanguage(LangKey.gameLimit));
                                            fPlayer.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
                                        } catch(Exception ex) {
                                            Logger.warning("Unable to send player to Bungee server.");
                                            fPlayer.kickPlayer(Language.getLanguage(LangKey.gameLimit));
                                        }
                                    } else {
                                        fPlayer.kickPlayer(Language.getLanguage(LangKey.gameLimit));
                                    }
                                }
                            }
                        }
                    }
                };
            }
        }

        plugin.getDebug().normal("Resetting game...");
        nextGame = System.currentTimeMillis() + (arenaMan.getLobby().gameTime * 1000);

        killedPlayers.clear();
        spectatorWatching.clear();
        arenaMan.getVoteManager().clear();
        arenaMan.clearContainers();
        deatchMatchCounting = false;
        refillCount = 0;
        spectators.clear();
        tributes.clear();
        
        final String fwName = arenaMan.getCurrentArena().worldName;
        // Disable the arena so it isn't chosen in the next batch of maps.
        arenaMan.disableArena(fwName, false);
        new GameTask(plugin, 0, 10L) {
            @Override
            public void run() {
                plugin.getDebug().normal("Trying to reset " + fwName + "...");
                if(arenaMan.isResetting(fwName)) {
                    plugin.getDebug().important(fwName + " was already resetting. Cancelling task.");
                    // If the arena has started resetting then cancel this check.
                    setCancelled(true);
                } else if(plugin.getServer().getWorld(fwName).getPlayers().isEmpty()) {
                    // Disables and waits until the world is unloaded before it resets the map.
                    arenaMan.resetArena(fwName);
                    // If the arena has started resetting then cancel this check.
                    setCancelled(true);
                }
            }
        };

        
        if((plugin.getSettings().restartMinutes > 0
            && System.currentTimeMillis() > plugin.restartTime)
            || (plugin.getSettings().restartGames > 0 
            && plugin.getSettings().restartGames <= gamesPlayed)) {
            // If the server is going to shutdown then don't allow new players.
            plugin.getServer().setWhitelist(true);
        } else {
            // Choose a random world to be next.
            arenaMan.randomizeArenaOrder();
            // Update the current arena.
            arenaMan.setLastArena(arenaMan.getCurrentArena());
            arenaMan.setCurrentArena(arenaMan.getLobby());
            if(!arenaMan.arenaOrder.isEmpty())
                arenaMan.setNextArena(arenaMan.arenaOrder.get(0));

            // Kickjoin
            for(int joinLevel : joinLevels.keySet()) {
                for(Player p : joinLevels.get(joinLevel)) {
                    if(tributes.size() < arenaMan.getNextArena().spawns.size()) {
                        setTribute(p);
                        setVanished(p, false);
                    } else {
                        setSpectator(p);
                        setVanished(p, true);
                    }
                    p.setGameMode(GameMode.SURVIVAL);
                    p.setFlying(false);
                    p.setAllowFlight(false);
                    resetPlayer(p);
                }
            }

            STATE = SGGameState.LOBBY;
        }
    }
    
    public void scheduleCountdown(int seconds, LangKey lang) {
        int initialSecs = seconds;
        final LangKey langKey = lang;
        while (seconds >= 0) {
            if (seconds > 30 * 60) {
                int scheduleTime = seconds - (seconds % (5 * 60));
                final int displaySecs = seconds;
                taskMan.addTask("countdown", new GameTask(plugin, (initialSecs - scheduleTime) * 20L) {
                    @Override
                    public void run() {
                        Language.setVar("seconds", displaySecs + "");
                        Language.setVar("time", Tools.getTime(displaySecs * 1000));
                        Language.setVar("fulltime", Tools.getFullTime(displaySecs * 1000));
                        Language.broadcastLanguage(langKey);
                    }
                });
                seconds -= 5 * 60;
            } else if (seconds > 5 * 60) {
                int scheduleTime = seconds - (seconds % (5 * 60));
                final int displaySecs = seconds;
                taskMan.addTask("countdown", new GameTask(plugin, (initialSecs - scheduleTime) * 20L) {
                    @Override
                    public void run() {
                        Language.setVar("seconds", displaySecs + "");
                        Language.setVar("time", Tools.getTime(displaySecs * 1000));
                        Language.setVar("fulltime", Tools.getFullTime(displaySecs * 1000));
                        Language.broadcastLanguage(langKey);
                    }
                });
                seconds -= 5 * 60;
            } else if (seconds > 60) {
                int scheduleTime = seconds - (seconds % 60);
                final int displaySecs = seconds;
                taskMan.addTask("countdown", new GameTask(plugin, (initialSecs - scheduleTime) * 20L) {
                    @Override
                    public void run() {
                        Language.setVar("seconds", displaySecs + "");
                        Language.setVar("time", Tools.getTime(displaySecs * 1000));
                        Language.setVar("fulltime", Tools.getFullTime(displaySecs * 1000));
                        Language.broadcastLanguage(langKey);
                    }
                });
                seconds -= 60;
            } else if (seconds > 15) {
                int scheduleTime = seconds - (seconds % 15);
                final int displaySecs = seconds;
                taskMan.addTask("countdown", new GameTask(plugin, (initialSecs - scheduleTime) * 20L) {
                    @Override
                    public void run() {
                        Language.setVar("seconds", displaySecs + "");
                        Language.setVar("time", Tools.getTime(displaySecs * 1000));
                        Language.setVar("fulltime", Tools.getFullTime(displaySecs * 1000));
                        Language.broadcastLanguage(langKey);
                    }
                });
                seconds -= 15;
            } else if (seconds > 5) {
                int scheduleTime = seconds - (seconds % 5);
                final int displaySecs = seconds;
                taskMan.addTask("countdown", new GameTask(plugin, (initialSecs - scheduleTime) * 20L) {
                    @Override
                    public void run() {
                        Language.setVar("seconds", displaySecs + "");
                        Language.setVar("time", Tools.getTime(displaySecs * 1000));
                        Language.setVar("fulltime", Tools.getFullTime(displaySecs * 1000));
                        Language.broadcastLanguage(langKey);
                    }
                });
                seconds -= 5;
            } else if(seconds > 0) {
                int scheduleTime = seconds;
                final int displaySecs = seconds;
                taskMan.addTask("countdown", new GameTask(plugin, (initialSecs - scheduleTime) * 20L) {
                    @Override
                    public void run() {
                        Language.setVar("seconds", displaySecs + "");
                        Language.setVar("time", Tools.getTime(displaySecs * 1000));
                        Language.setVar("fulltime", Tools.getFullTime(displaySecs * 1000));
                        Language.broadcastLanguage(langKey);
                        if(!Tools.isNullEmptyWhite(plugin.getSettings().countdown)) {
                            for(Player p : plugin.getServer().getOnlinePlayers()) {
                                p.playSound(p.getLocation(), Sound.valueOf(plugin.getSettings().countdown), 10f, 0.5f);
                            }
                        }
                    }
                });
                seconds -= 1;
            } else {
                taskMan.addTask("countdown", new GameTask(plugin, initialSecs * 20L) {
                    @Override
                    public void run() {
                        if(!Tools.isNullEmptyWhite(plugin.getSettings().countdownFinish)) {
                            for(Player p : plugin.getServer().getOnlinePlayers()) {
                                p.playSound(p.getLocation(), Sound.valueOf(plugin.getSettings().countdownFinish), 10f, 1f);
                            }
                        }
                    }
                });
                break;
            }
        }
    }
    
    public int getKickJoinLevel(Player p) {
        int kickJoinLevel = 0;
        for(int i = 10; i >= 1; i--) {
            if(Perms.has(p, "survivalgames.kickjoin." + i, p.isOp())) {
                kickJoinLevel = i;
                break;
            }
        }
        return kickJoinLevel;
    }
    
    public boolean isSpectator(Player p) {
        return this.spectators.contains(p.getName());
    }
    
    public void setSpectator(Player p) {
        if(!spectators.contains(p.getName()))
            spectators.add(p.getName());
        tributes.remove(p.getName());
    }
    
    public boolean isTribute(Player p) {
        return tributes.contains(p.getName());
    }
    
    public void setTribute(Player p) {
        if(!tributes.contains(p.getName()))
            tributes.add(p.getName());
        spectators.remove(p.getName());
    }
    
    public void dropPlayerItems(Player p) {
    }
    
    public void resetPlayer(Player p) {
        p.setFoodLevel(20);
        p.setHealth(20D);
        p.setSaturation(10.0F);
        p.setExp(0);
        p.setLevel(0);
        p.setVelocity(new Vector());
        if(!Perms.has(p, "survivalgames.admin.keepinventory", p.isOp())) {
            p.getInventory().setArmorContents(
                    new ItemStack[p.getInventory().getArmorContents().length]);
            p.getInventory().setContents(
                    new ItemStack[p.getInventory().getContents().length]);
        }
    }
    
    public void respawnPlayer(Player p) {
        resetPlayer(p);
        Location spawnLoc = p.getLocation();
        if (plugin.getGameManager().getState() != SGGameState.LOBBY) {
            if(plugin.getGameManager().isSpectator(p)) {
                plugin.getGameManager().setVanished(p, true);

                ItemStack compass = ItemUtils.createItemStack(
                        "&a&lSpectate Players &7(Left Click)",
                        Arrays.asList(
                                "&c&lLEFT &7click to open the spectating menu!"
                        ),
                        Material.COMPASS
                );

                p.getInventory().setItem(0, compass);

                if(plugin.getGameManager().getState() == SGGameState.GAME) {
                    spawnLoc = plugin.getArenaManager().getCurrentArena().spectatorSpawn;
                } else {
                    spawnLoc = plugin.getArenaManager().getCurrentArena().dmSpectatorSpawn;
                }
            } else {
                plugin.getGameManager().setVanished(p, false);
                p.setGameMode(GameMode.SURVIVAL);
                p.setFlying(false);
                p.setAllowFlight(false);
                spawnLoc = p.getLocation();
            }
        } else {
            p.setGameMode(GameMode.ADVENTURE);
            p.setFlying(false);
            p.setAllowFlight(false);
            spawnLoc = Bukkit.getWorld(plugin.getArenaManager().getLobby().worldName).getSpawnLocation();
        }
        p.teleport(spawnLoc);
        if (plugin.getGameManager().getState() != SGGameState.LOBBY && plugin.getGameManager().isSpectator(p)) {
        	p.setGameMode(GameMode.ADVENTURE);
            p.setAllowFlight(true);
            p.setFlying(true);
        }
    }
    
    public boolean isAdmin(Player p) {
        return !tributes.contains(p.getName()) && !spectators.contains(p.getName());
    }
    
    public void setAdmin(Player p) {
        tributes.remove(p.getName());
        spectators.remove(p.getName());
    }
    
    public boolean isVanished(Player p) {
        return vanished.contains(p);
    }
    
    public void setVanished(Player p, boolean hide) {
        if(p != null) {
                if (hide && !isVanished(p)) {
                	vanished.add(p);
                    for (Player p1 : getTributes()) {
                    	p1.hidePlayer(p);
                    }
                    for (Player p1 : getSpectators()) {
                    	p1.hidePlayer(p);
                    }
                } else if (!hide && isVanished(p)) {
                	vanished.remove(p);
                	for (Player p1 : getTributes()) {
                    	p1.showPlayer(p);
                    }
                	for (Player p1 : getSpectators()) {
                		p1.showPlayer(p);
                	}
                }
        } else {
            plugin.getDebug().normal("Player was null on vanish. (" + hide + ")");
        }
    }
    
    public List<String> getSpectatorNames() {
        return spectators;
    }
    
    public List<Player> getSpectators() {
        List<Player> list = new ArrayList<Player>();
        
        for (String specName : spectators) {
            list.add(plugin.getServer().getPlayer(specName));
        }
        return list;
    }
    
    public List<String> getTributeNames() {
        return tributes;
    }
    
    public List<Player> getTributes() {
        List<Player> list = new ArrayList<Player>();
        
        for (String specName : tributes) {
            list.add(plugin.getServer().getPlayer(specName));
        }
        return list;
    }
    
    public void removePlayer(Player p) {
        tributes.remove(p.getName());
        spectators.remove(p.getName());
        spectatorWatching.remove(p.getName());
    }
    
    public void revivePlayer(Player p) {
        if (STATE != SGGameState.LOBBY && STATE != SGGameState.RESETTING && isSpectator(p)) {
            if (this.damagecause.containsKey(p.getName())) {
                this.damagecause.remove(p.getName());
            }
            Language.setTarget(p);
            Language.broadcastLanguage(LangKey.tributeRevived);
            
            Language.setUser(p);
            Language.sendLanguage(p, LangKey.youRevived, false);
            
            setTribute(p);
            setVanished(p, false);
            p.setGameMode(GameMode.SURVIVAL);
            p.setFlying(false);
            p.setAllowFlight(false);
            resetPlayer(p);
            
            Language.setVar("amount", getTributeNames().size() + "");
            Language.broadcastLanguage(LangKey.tributesRemaining);
        }
    }
    
    public void killPlayer(Player p, Location loc) {
        if (STATE != SGGameState.LOBBY && STATE != SGGameState.RESETTING && isTribute(p)) {
            Language.setTarget(p);
            Language.broadcastLanguage(LangKey.tributeFallen);
            ArenaData aData = arenaMan.getCurrentArena();
            Language.broadcastLanguage(LangKey.cannon);
            Language.setVar("amount", getTributeNames().size()-1 + "");
            Language.broadcastLanguage(LangKey.tributesRemaining);
            
            PlayerStats playerStats = statMan.getPlayer(p.getName());
            
            PlayerDamage lastDamage = damagecause.get(p.getName());
            if(lastDamage != null) {
                Player killer = plugin.getServer().getPlayer(lastDamage.attackerName);

                // If the killer is still in proximity of the player then allow the points.
                // Also only allow the points if it's within 10 seconds.
                if(killer != null && (System.currentTimeMillis() - damagecause.get(p.getName()).timeAttacked) < 10 * 1000) {
                    Language.setTarget(p);
                    Language.setUser(killer);
                    Language.sendLanguage(killer, LangKey.youKilled);

                    // Set stats for killer.
                    PlayerStats killerStats = statMan.getPlayer(killer.getName());
                    killerStats.setKills(killerStats.getKills() + 1);
                    killerStats.setKillStreak(killerStats.getKillStreak() + 1);
                    killerStats.setDeathStreak(0);
                    if(killerStats.getKillStreak() > killerStats.getBestKillStreak())
                        killerStats.setBestKillStreak(killerStats.getKillStreak());
                    int points = aData.killPoints;
                    if(aData.killPercent > 0) {
                        int percentPoints = (int) (playerStats.getPoints() * aData.killPercent * 0.01D);
                        plugin.getDebug().normal("Points: "  + playerStats.getPoints() + ", Points: " + percentPoints);
                        points += percentPoints;
                    }
                    points += statMan.getBounty(p.getName());
                    killerStats.setPoints(killerStats.getPoints() + points);
                    Language.setUser(killer);
                    Language.setVar("points", points + "");
                    Language.sendLanguage(killer, LangKey.wonPoints);
                    killerStats.save();
                        
                    // Economy
                    Treasury treasury = plugin.getTreasury();
                    if(treasury.hasEconomy() && aData.moneyMultiplier > 0) {
                        treasury.getEconomy().depositPlayer(killer.getName(), points * aData.moneyMultiplier);
                    }

                    Language.setTarget(killer);
                }
            }
            
            Language.setUser(p);
            Language.sendLanguage(p, LangKey.youDied, false);
            Language.sendLanguage(p, LangKey.nowSpec);
            
            statMan.setBounty(p.getName(), 0);
            setSpectator(p);
            setVanished(p, true);
            
            final Location fLoc = loc;
            // Drop items.
            for(ItemStack item : p.getInventory().getContents())
            	if (item != null && item.getType() != Material.AIR)
                    p.getWorld().dropItemNaturally(fLoc, item);
            for(ItemStack item : p.getInventory().getArmorContents())
            	if (item != null && item.getType() != Material.AIR)
                    p.getWorld().dropItemNaturally(fLoc, item);
            respawnPlayer(p);
            
            // Deaths
            playerStats.setDeaths(playerStats.getDeaths() + 1);
            playerStats.setDeathStreak(playerStats.getDeathStreak() + 1);
            playerStats.setKillStreak(0);
            if(playerStats.getDeathStreak() > playerStats.getWorstDeathStreak())
                playerStats.setWorstDeathStreak(playerStats.getDeathStreak());
            // Losses
            playerStats.setLosses(playerStats.getLosses() + 1);
            playerStats.setLoseStreak(playerStats.getLoseStreak() + 1);
            if(playerStats.getLoseStreak() > playerStats.getWorstLoseStreak())
                playerStats.setWorstLoseStreak(playerStats.getLoseStreak());
            playerStats.setWinStreak(0);
            
            playerStats.setGamesPlayed(playerStats.getGamesPlayed() + 1);
            playerStats.setTimePlayed(playerStats.getTimePlayed() + (int) (System.currentTimeMillis() - gameStartTime));
            
            // Points
            int points = aData.killPoints;
            if(aData.killPercent > 0) {
                points += (int) (playerStats.getPoints() * aData.killPercent * 0.01D);
            }
            playerStats.setPoints(playerStats.getPoints() - points);
            if(playerStats.getPoints() < 0)
                playerStats.setPoints(0);
            Language.setUser(p);
            Language.setVar("points", points + "");
            Language.sendLanguage(p, LangKey.lostPoints);
            playerStats.save();
                        
            // Economy
            Treasury treasury = plugin.getTreasury();
            if(treasury.hasEconomy() && aData.moneyMultiplier > 0 && aData.loseMoneyOnDeath) {
                treasury.getEconomy().withdrawPlayer(p.getName(), points * aData.moneyMultiplier);
            }
            
            int numGames = playerGames.containsKey(p.getName()) ? playerGames.get(p.getName()) : 0;
            playerGames.put(p.getName(), numGames + 1);
            
            p.getWorld().strikeLightningEffect(p.getLocation());
            
            if (this.damagecause.containsKey(p.getName())) {
                this.damagecause.remove(p.getName());
            }
            
            if (getTributeNames().size() <= 1) {
                endGame();
            }
        }
    }
    
    public void switchSpectate(Player p) {
        if (!this.spectatorWatching.containsKey(p.getName())) {
            this.spectatorWatching.put(p.getName(), Integer.valueOf(-1));
        }
        int watching = ((Integer) this.spectatorWatching.get(p.getName())).intValue() + 1;
        if (watching >= getTributeNames().size()) {
            watching = 0;
        }
        this.spectatorWatching.put(p.getName(), Integer.valueOf(watching));
        try {
            Player tribute = (Player) getTributes().get(watching);
            if (tribute != null) {
                p.teleport(tribute);
                Language.setUser(p);
                Language.setTarget(tribute);
                Language.sendLanguage(p, LangKey.changeSpectate);
            }
        } catch (Exception localException) {

        }
    }
    
    public void restartServer() {
        STATE = SGGameState.RESTARTING;
        cancelTasks();
        plugin.getServer().setWhitelist(true);
        Settings settings = plugin.getSettings();
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if(settings.useBungee && !Tools.isNullEmptyWhite(settings.bungeeServer)) {
                final String serverName = settings.bungeeServer;
                final String pName = p.getName();
                new GameTask(plugin) {
                    @Override
                    public void run() {
                        Player player = plugin.getServer().getPlayer(pName);
                        try {
                            ByteArrayOutputStream b = new ByteArrayOutputStream ();
                            DataOutputStream out = new DataOutputStream (b);
                            out.writeUTF("Connect");
                            out.writeUTF(serverName);
                            player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
                        } catch(Exception ex) {
                            Logger.warning("Unable to send player to bungee server.");
                            player.kickPlayer(Language.getLanguage(LangKey.serverShutdown));
                        }
                    }
                };
            } else {
                p.kickPlayer(Language.getLanguage(LangKey.serverShutdown));
            }
        }
        // Restart after 5 seconds.
        plugin.getTaskManager().addTask("shutdown", new GameTask(plugin, 5 * 20L) {
            @Override
            public void run() {
                Bukkit.shutdown();
            }
        });
    }
}
