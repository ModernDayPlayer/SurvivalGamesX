package net.shockverse.survivalgames.extras;

import net.shockverse.survivalgames.SurvivalGames;
import net.shockverse.survivalgames.core.Logger;
import com.alta189.sqlLibrary.SQL.SQLCore;
import com.alta189.sqlLibrary.SQL.SQLCore.SQLMode;
import java.sql.ResultSet;
import net.shockverse.survivalgames.core.Constants;

/**
 * @description Handles database connections
 * @author Duker02, LegitModern, Tagette
 */
public class DataManager {

    private SurvivalGames plugin;
    private SQLCore dbCore;
    public boolean connected;

    /*
     * Initializes the DataManager class.
     * 
     * @param instance  An instance of the plugin's main class.
     */
    public DataManager(SurvivalGames instance, SQLMode mode) {
        plugin = instance;
        if (mode == SQLMode.MySQL) {
            dbCore = new SQLCore(plugin.getLogger(), Logger.getPrefix(), plugin.getSettings().MySQLHost,
                    plugin.getSettings().MySQLUser, plugin.getSettings().MySQLPass, plugin.getSettings().MySQLDBName);
        } else if (mode == SQLMode.SQLite) {
            dbCore = new SQLCore(plugin.getLogger(), Logger.getPrefix(),
                    plugin.getDataFolder().getPath() + "/Data", plugin.name);
        }
        if (dbCore.initialize()) {
            if(!plugin.getSettings().LowDetailMode) {
                connected = true;
                Logger.info("Database connection established.");
            }
        } else {
            Logger.error("Database connection failed.");
        }
    }

    /*
     * Used for more advanced database interactions.
     */
    public SQLCore getDbCore() {
        return dbCore;
    }

    /*
     * Used to create a table in the database.
     */
    public boolean createTable(String query) {
        boolean wasCreated = false;
        if(Constants.databaseEnabled) {
            plugin.getDebug().normal("Database.createTable Query: \"" + query + "\"");
            wasCreated = dbCore.createTable(query);
        }
        return wasCreated;
    }

    /*
     * Deletes a table from the database.
     */
    public boolean deleteTable(String tableName) {
        boolean wasDeleted = false;
        if (!tableName.isEmpty()) {
            String query = "DROP TABLE '" + tableName + "'";
            wasDeleted = update(query);
        } else {
            Logger.error("Database.DeleteTable: Could not delete table because table name was empty.");
        }
        return wasDeleted;
    }

    public boolean execute(String query) {
        boolean wasCreated = false;
        if(Constants.databaseEnabled) {
            plugin.getDebug().normal("Database.execute Query: \"" + query + "\"");
            wasCreated = dbCore.insertQuery(query);
        }
        return wasCreated;
    }

    public boolean update(String query) {
        if(Constants.databaseEnabled) {
            plugin.getDebug().normal("Database.update Query: \"" + query + "\"");
        }
        return dbCore.updateQuery(query);
    }

    public boolean insert(String query) {
        if(Constants.databaseEnabled) {
            plugin.getDebug().normal("Database.insert Query: \"" + query + "\"");
        }
        return dbCore.insertQuery(query);
    }

    public ResultSet query(String query) {
        if(Constants.databaseEnabled) {
            plugin.getDebug().normal("Database.query Query: \"" + query + "\"");
        }
        return dbCore.sqlQuery(query);
    }

    public boolean tableExists(String tableName) {
        return dbCore.checkTable(tableName);
    }

    public boolean fieldExists(String tableName, String fieldName) {
        return dbCore.checkField(tableName, fieldName);
    }
}
