package com.alta189.sqlLibrary.SQL;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.logging.Logger;

public class SQLCore {
    /*
     *  @author: alta189
     * 
     */

    public enum SQLMode {

        SQLite, MySQL
    }
    private Logger log;
    private String logPrefix;
    public SQLMode mode;
    public String dbLocation;
    public String dbHost;
    public String dbUser;
    public String dbPass;
    public String dbName;
    private DatabaseHandler dbHandler;

    // Method to set up MySQL
    public SQLCore(Logger log, String logPrefix, String host, String user, String pass, String dbName) {
        mode = SQLMode.MySQL;

        this.log = log;
        this.logPrefix = logPrefix;
        this.dbHost = host;
        this.dbUser = user;
        this.dbPass = pass;
        this.dbName = dbName;

    }

    // Method to set up SQLite
    public SQLCore(Logger log, String logPrefix, String dbLocation, String dbName) {
        mode = SQLMode.SQLite;

        this.log = log;
        this.logPrefix = logPrefix;
        this.dbLocation = dbLocation;
        this.dbName = dbName;
    }

    public Boolean initialize() {
        boolean success = false;
        if (mode == SQLMode.MySQL) {
            dbHandler = new DatabaseHandler(this);
            success = dbHandler.openConnection();
        } else if (mode == SQLMode.SQLite) {
            File dbFolder = new File(dbLocation);
            if (dbName.contains("/") || dbName.contains("\\") || dbName.endsWith(".db")) {
                this.writeError("The database name can not contain: /, \\, or .db in '" + dbName + "'", true);
                return false;
            }
            if (!dbFolder.exists()) {
                dbFolder.mkdir();
            }

            File dbFile = new File(dbFolder.getAbsolutePath() + "/" + dbName);

            dbHandler = new DatabaseHandler(this, dbFile);
            success = dbHandler.openConnection();
        }

        return success;
    }

    public ResultSet sqlQuery(String query) {
        return this.dbHandler.sqlQuery(query);
    }

    public Boolean createTable(String query) {
        return this.dbHandler.createTable(query);
    }

    public boolean insertQuery(String query) {
        return this.dbHandler.insertQuery(query);
    }

    public boolean updateQuery(String query) {
        return this.dbHandler.updateQuery(query);
    }

    public boolean deleteQuery(String query) {
        return this.dbHandler.deleteQuery(query);
    }

    public boolean checkTable(String table) {
        return this.dbHandler.checkTable(table);
    }

    public boolean checkField(String table, String field) {
        return this.dbHandler.checkField(table, field);
    }

    public boolean wipeTable(String table) {
        return this.dbHandler.wipeTable(table);
    }

    public Connection getConnection() {
        return this.dbHandler.getConnection();
    }

    public void close() {
        this.dbHandler.closeConnection();
    }

    public boolean checkConnection() {
        return dbHandler.getConnection() != null;
    }

    public void writeInfo(String toWrite) {
        if (toWrite != null) {
            this.log.info(this.logPrefix + toWrite);
        }
    }

    public void writeError(String toWrite, Boolean severe) {
        if (severe) {
            if (toWrite != null) {
                this.log.severe(this.logPrefix + toWrite);
            }
        } else {
            if (toWrite != null) {
                this.log.warning(this.logPrefix + toWrite);
            }
        }
    }
}
