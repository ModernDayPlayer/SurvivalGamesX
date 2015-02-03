package net.shockverse.survivalgames.core;

import com.alta189.sqlLibrary.SQL.SQLCore;
import com.alta189.sqlLibrary.SQL.SQLCore.SQLMode;
import net.shockverse.survivalgames.SurvivalGames;
import net.shockverse.survivalgames.extras.DataManager;

/**
 * @description Handles SQL database connection
 * @author Duker02, LegitModern, Tagette
 */
public class DataAccess {

    private static SurvivalGames plugin;
    private static DataManager dbm;

    /*
     * Initializes the plugins database connection.
     * 
     * @param instance  An instance of the plugin's main class.
     */
    public static void initialize(SurvivalGames instance) {
        plugin = instance;
        SQLMode dataMode;
        if (plugin.getSettings().useMySQL) {
            dataMode = SQLMode.MySQL;
        } else {
            dataMode = SQLMode.SQLite;
        }
        dbm = new DataManager(plugin, dataMode);

        // Create database here

        String tableQuery = "CREATE TABLE players (";
        if(dataMode == SQLMode.MySQL)
            tableQuery += "id INT AUTO_INCREMENT, ";
        else
            tableQuery += "id INT PRIMARY KEY, ";
        tableQuery += "player VARCHAR(30), "
                + "kills INT(11), "
                + "killstreak INT(3), "
                + "bestkillstreak INT(3), "
                + "deaths INT(11), "
                + "deathstreak INT(5), "
                + "worstdeathstreak INT(5), "
                + "points INT(11), "
                + "wins INT(11), "
                + "winstreak INT(3), "
                + "bestwinstreak INT(3), "
                + "ties INT(11),"
                + "losses INT(11),"
                + "losestreak INT(11),"
                + "worstlosestreak INT(11),"
                + "timeplayed INT(11),"
                + "games INT(11),"
                + "lastplayed BIGINT(18),"
                + "containerslooted INT(11),"
                + "animalskilled INT(11),"
                + "mobskilled INT(11)";
        if(dataMode == SQLMode.MySQL)
            tableQuery += ", PRIMARY KEY (id)";
        tableQuery += ")";
        if (!dbm.tableExists("players") && dbm.createTable(tableQuery)) {
            Logger.info("Table created. (players)");
        }

        tableQuery = "CREATE TABLE kills (";
        if(dataMode == SQLMode.MySQL)
            tableQuery += "id INT AUTO_INCREMENT, ";
        else
            tableQuery += "id INT PRIMARY KEY, ";
        tableQuery += "player VARCHAR(30), "
                + "victim VARCHAR(30), "
                + "points INT(11), "
                + "time BIGINT(18)";
        if(dataMode == SQLMode.MySQL)
            tableQuery += ", PRIMARY KEY (id)";
        tableQuery += ")";
        if (!dbm.tableExists("kills") && dbm.createTable(tableQuery)) {
            Logger.info("Table created. (kills)");
        }
    }
    
    public static boolean connected() {
        return getCore().checkConnection();
    }

    /*
     * Closes the connection to the database.
     */
    public static void disable() {
        dbm.getDbCore().close();
    }
    
    public static DataManager getManager() {
        return dbm;
    }

    /*
     * Gets the Database core.
     * Used for more advanced databasing. :)
     */
    public static SQLCore getCore() {
        return dbm.getDbCore();
    }
}
