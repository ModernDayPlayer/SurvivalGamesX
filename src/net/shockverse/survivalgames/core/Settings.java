package net.shockverse.survivalgames.core;

import java.io.File;
import net.shockverse.survivalgames.SurvivalGames;
import net.shockverse.survivalgames.exceptions.PropertyException;
import net.shockverse.survivalgames.extras.DebugDetailLevel;
import net.shockverse.survivalgames.extras.PropertyFile;
import net.shockverse.survivalgames.extras.PropertyList;
import net.shockverse.survivalgames.interfaces.IConfig;

/**
 * @description Handles property files
 * @author Duker02, LegitModern, Tagette
 */
public class Settings implements IConfig {

    private String settingsConfig = "Settings";
    private SurvivalGames plugin;
    private PropertyFile settingsFile;
    
    // Add settings here
    public boolean autoUpdate;
    public String updateChannel;
    public String motdLobby;
    public String motdStarting;
    public String motdInGame;
    public String motdPreDM;
    public String motdDeathMatch;
    public boolean enableScoreboard;
    public String motdRestarting;
    public int maxVoteList;
    public int playerLimit;
    public long restartMinutes;
    public long restartGames;
    public long kickGames;
    public int delayAfterGame;
    public boolean useCustomChat;
    public int tntTicks;
    public int bloodIntensity;
    public boolean createExamples;
    
    // Sounds
    public String countdown;
    public String countdownFinish;
    
    public boolean useBungee;
    public String bungeeServer;
    
    // Debug
    public boolean initialDebug;
    public DebugDetailLevel debugLevel;
    public String previousVersion;
    
    // Database
    public boolean useMySQL;
    public String MySQLHost;
    public String MySQLUser;
    public String MySQLPass;
    public String MySQLDBName;
    public boolean LowDetailMode;
    
    public Settings(SurvivalGames instance, String settingsFile) {
        plugin = instance;
        this.settingsConfig = settingsFile;
    }

    @Override
    public void load() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // Dont know why but needed to repeat..
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        try {
            File configFile = new File(plugin.getDataFolder(), settingsConfig + ".cfg");
            settingsFile = new PropertyFile(settingsConfig, configFile);
            settingsFile.loadProperties();
            PropertyList props = settingsFile.getProperties();
            setup(props);
            settingsFile.saveProperties(); 
            if (!LowDetailMode) {
                Logger.info("Settings loaded.");
            }
        } catch (PropertyException pe) {
            Logger.error(pe.getMessage());
        } catch (Exception ex) {
            Logger.error("Could not load " + settingsConfig + ".cfg file.");
            ex.printStackTrace();
        }
    }
    
    @Override
    public void disable(){
    }

    private void setup(PropertyList props) {
        // Declare settings here. Note that if config is not found these values should be placed into a new config file.

    	props.comment("-- Settings --");
    	props.newLine();
        autoUpdate = props.getBoolean("autoUpdate", true, "If true your plugin will update to the "
                + "latest release of the plugin automatically.");
        updateChannel = props.getString("updateChannel", "Release", "The channel you will update "
                + "on. Release, Beta, Alpha");
        motdLobby = props.getString("stateLobby", "Lobby");
        motdStarting = props.getString("stateStarting", "Starting...");
        motdInGame = props.getString("stateInGame", "In-Game");
        motdPreDM = props.getString("statePreDM", "Pre-DeathMatch");
        motdDeathMatch = props.getString("stateDeathMatch", "DeathMatch");
        motdRestarting = props.getString("stateRestarting", "Restarting");
        enableScoreboard = props.getBoolean("enableScoreboard", true);
        
        maxVoteList = props.getInt("maxVoteList", 5, "The amount of arenas to show in the vote list.");
        playerLimit = props.getInt("playerLimit", 24, "The amount of (non-admin) players allowed on the server. If it goes over it will kick.");
        props.comment("You will need a script that restarts the server once it's shutdown.");
        restartMinutes = props.getInt("restartMinutes", 0, "Restart the server after this many minutes. 0 to disable.");
        restartGames = props.getInt("restartGames", 0, "Restart the server after this many games. 0 to disable.");
        kickGames = props.getInt("kickGames", 0, "Will kick the players after this many games. 0 to disable.");
        delayAfterGame = props.getInt("delayAfterGame", 7, "The delay before the arena is reset in seconds.");
        useCustomChat = props.getBoolean("useCustomChat", true, "If true then this plugins chat will be used.");
        tntTicks = props.getInt("tntTicks", 3 * 20, "The amount of ticks before tnt explodes.");
        bloodIntensity = props.getInt("bloodIntensity", 3, "If greater than 0, a red particle effect will show when someone is hit.");
        createExamples = props.getBoolean("createExamples", true, "If true, example config and scripts will be created.");
        
        props.newLine();
        props.comment("-- Sounds --");
        props.newLine();
        
        countdown = props.getString("countdown", "NOTE_STICKS");
        countdownFinish = props.getString("countdownFinish", "NOTE_STICKS");
        
    	props.newLine();
    	props.comment("-- Bungee --");
        props.newLine();
    	
        useBungee = props.getBoolean("useBungee", false);
        bungeeServer = props.getString("bungeeServer", "", "The bungee server name to connect to.");
        
    	props.newLine();
    	props.comment("-- Plugin --");
    	props.newLine();
        
        previousVersion = props.getString("version", plugin.version, "Do not change this.");
        if(versionChange()) {
            Logger.warning("Version change detected. (" + previousVersion + " -> " + plugin.version + ")");
        }
        props.setString("version", plugin.version);
        
        if(Constants.debugAllowed){
            initialDebug = props.getBoolean("initialDebug", false, "If true the plugin will start in debug mode.");
            debugLevel = DebugDetailLevel.values()[props.getInt("debugDetailLevel", 1, "How much detail the debugger shows. (0 = Everything, 2 = Important Only) Default: 1")];
        }
        if(Constants.lowDetailAllowed){
            LowDetailMode = props.getBoolean("lowDetailMode", false, "Displays less info when starting if true.");
        } else {
            LowDetailMode = false;
        }

        if(Constants.databaseEnabled){
        	props.newLine();
        	props.comment("-- MySQL Settings --");
        	props.newLine();

            useMySQL = props.getBoolean("useMySQL", false, "If set to false, SQLite will be used instead.");
            MySQLHost = props.getString("host", "localhost", "The host of the MySQL database. Default: localhost");
            MySQLUser = props.getString("user", "root", "The username to access the MySQL database with.");
            MySQLPass = props.getString("pass", "", "The password for the user.");
            MySQLDBName = props.getString("dbname", "", "The name of the database.");
        }
    }
    
    public boolean versionChange() {
        return !previousVersion.equals(plugin.version);
    }
}
