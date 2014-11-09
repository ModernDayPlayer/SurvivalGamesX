package net.shockverse.survivalgames;

import java.util.Random;
import net.gravitydevelopment.updater.Updater;
import net.gravitydevelopment.updater.Updater.ChannelType;
import net.shockverse.survivalgames.api.SGAPI;
import net.shockverse.survivalgames.commands.SGCmd;
import net.shockverse.survivalgames.core.*;
import net.shockverse.survivalgames.extras.AntiPlayerSpam;
import net.shockverse.survivalgames.extras.CommandManager;
import net.shockverse.survivalgames.extras.TaskManager;
import net.shockverse.survivalgames.listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.ItemList;


/**
 * @description Main class for SurvivalGames plugin for Bukkit.
 * @author Duker02, LegitModern, Tagette
 */
public class SurvivalGames extends JavaPlugin {

    private static SurvivalGames instance;
    public static SurvivalGames getInstance() {
        return instance;
    }

    // Listeners
    private final CommandManager commandManager = new CommandManager(this);
    private final ServerListener serverListener = new ServerListener(this);
    private final EntityListener entityListener = new EntityListener(this);
    private final PlayerListener playerListener = new PlayerListener(this);
    private final BlockListener blockListener = new BlockListener(this);
    private final WorldListener worldListener = new WorldListener(this);
    private final SGListener sgListener = new SGListener(this);
    //private final ItemListener itemListener = new ItemListener(this);
    
    public String name;
    public String version;
    public long restartTime;
    private AntiPlayerSpam antiSpammer;
    
    private Random random;
    private Settings settings;
    private Debug debug;
    private Treasury treasury;
    private StatManager statMan;
    private GameManager gameMan;
    private ArenaManager arenaMan;
    private TaskManager taskMan;

    /*
     * This method runs when the plugin is enabled.
     */
    @Override
    public void onEnable() {
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        name = this.getDescription().getName();
        version = this.getDescription().getVersion();
        antiSpammer = new AntiPlayerSpam(this);
        random = new Random();
        taskMan = new TaskManager();
        
        // Logger
        Logger.initialize(this);
        Logger.setPrefix(ChatColor.DARK_GREEN + "[" + ChatColor.YELLOW + name + ChatColor.DARK_GREEN +  "] " + ChatColor.WHITE);
        
        // Settings
        settings = new Settings(this, "Settings");
        settings.load();
        // Debug
        debug = new Debug(this);
        // Language
        Language.initialize(this);
        
        arenaMan = new ArenaManager(this, "Arenas", "Rewards", "Spawns");
        arenaMan.load();
        arenaMan.randomizeArenaOrder();
        arenaMan.setCurrentArena(arenaMan.getLobby());
        
        if(settings.restartMinutes > 0) {
            Logger.warning("Auto restart enabled. (" + Tools.getTime(settings.restartMinutes * 60 * 1000) + ")");
            restartTime = System.currentTimeMillis() + settings.restartMinutes * 60 * 1000;
        } else if(settings.restartGames > 0) {
            Logger.warning("Auto restart enabled. (" + settings.restartGames + " games)");
        }

        // Database.
        if (Constants.databaseEnabled) {
            DataAccess.initialize(this);
        }

        // Permissions using vault.
        Perms.initialize(this);
        
        // Economy using vault.
        treasury = new Treasury(this);
        
        PluginManager pm = getServer().getPluginManager();
        
        // Makes sure all plugins are correctly loaded
        pm.registerEvents(serverListener, this);
        
        // Register our event listeners.
        pm.registerEvents(entityListener, this);
        pm.registerEvents(playerListener, this);
        pm.registerEvents(blockListener, this);
        pm.registerEvents(worldListener, this);
        pm.registerEvents(sgListener, this);
        //pm.registerEvents(itemListener, this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "RubberBand");

        // Commands
        setupCommands();
        
        // Components
        Help.initialize(this);
        
        ExampleCreator exCreator = new ExampleCreator(this);
        exCreator.Create();
        
        // Initialize you own classes here.
        statMan = new StatManager(this);
        gameMan = new GameManager(this);
        
        // Initialize the API.
        SGAPI.Intialize(this);
        
        if(settings.autoUpdate) {
            Logger.info("Checking for an update..");
            Updater updater = new Updater(this, 62228, this.getFile(), Updater.UpdateType.DEFAULT, ChannelType.getType(settings.updateChannel), true);
            if(updater.getResult() == Updater.UpdateResult.SUCCESS) {
                Logger.warning(name + " has been updated. The new version will be applied after restart.");
            } else if(updater.getResult() == Updater.UpdateResult.NO_UPDATE) {
                Logger.info(name + " v" + version + " is up to date.");
            } else if(updater.getResult() == Updater.UpdateResult.SPECIAL_BUILD) {
                Logger.warning(name + " v" + version + " is a special build. It cannot be updated.");
            }
        }

        Bukkit.getServer().setWhitelist(false);
        
        // Done enabling.
        Logger.info(name + " version " + version + " is enabled!");
        
        int onlineCount = 0;
        for(Player alreadyOnline : Bukkit.getServer().getOnlinePlayers()) {
            playerListener.onPlayerJoin(new PlayerJoinEvent(alreadyOnline, alreadyOnline.getName() + " joined the game."));
            onlineCount++;
        }
        if(onlineCount > 0)
            Logger.info(onlineCount + " players were online already.");
        
    }
    

    /*
     * Sets up the core commands of the plugin.
     */
    private void setupCommands() {
        // Commands here must also be in your plugin.yml.
        
        // Declare the executor class you made.
        SGCmd sgCmd = new SGCmd(this);
        
        // Add the commands.
        addCommand("sg", sgCmd);
        addCommand("survivalgames", sgCmd);
        addCommand("hg", sgCmd);
        addCommand("hungergames", sgCmd);
    }

    /*
     * This method runs when the plugin is disabling.
     */
    @Override
    public void onDisable() {
        taskMan.clearTasks();
        Bukkit.getScheduler().cancelTasks(this);
        
        // Unregister event listeners
        serverListener.disable();
        entityListener.disable();
        playerListener.disable();
        blockListener.disable();
        worldListener.disable();
        //itemListener.disable();

        // Disable the core components.
        Perms.disable();
        if(Constants.databaseEnabled) {
            DataAccess.disable();
        }
        debug.disable();
        Language.disable();
        settings.disable();
        antiSpammer = null;
        
        /* Disable your classes here. */
        
        arenaMan.disable();
        arenaMan = null;
        gameMan.disable();
        gameMan = null;


        // Removes all entities from the world (player drops).
        for (World worlds : Bukkit.getServer().getWorlds()) {
            for (Entity entity : worlds.getEntities()) {
                entity.remove();
            }
        }
        
        Logger.info(name + " disabled.");
        Logger.disable();
    }
    
    /* No need to change anything below. */

    /*
     * Executes a command when a command event is received.
     * 
     * @param sender    The thing that sent the command.
     * @param cmd       The complete command object.
     * @param label     The label of the command.
     * @param args      The arguments of the command.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        return commandManager.dispatch(sender, cmd, label, args); // No touchy.
    }

    /*
     * Adds the specified command to the command manager and server.
     * 
     * @param command   The label of the command.
     * @param executor  The command class that excecutes the command.
     */
    private void addCommand(String command, CommandExecutor executor) {
        getCommand(command).setExecutor(executor);
        commandManager.addCommand(command, executor);
    }
    
    public Random getRandom() {
        return random;
    }

    public AntiPlayerSpam getAntiSpammer() {
        return antiSpammer;
    }
    
    public Settings getSettings() {
        return settings;
    }
    
    public Debug getDebug() {
        return debug;
    }
    
    public Treasury getTreasury() {
        return treasury;
    }
    
    public GameManager getGameManager() {
        return gameMan;
    }
    
    public ArenaManager getArenaManager() {
        return arenaMan;
    }
    
    public StatManager getStatManager() {
        return statMan;
    }
    
    public TaskManager getTaskManager() {
        return taskMan;
    }
}
