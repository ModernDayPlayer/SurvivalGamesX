package com.alta189.sqlLibrary.SQL;

import com.alta189.sqlLibrary.SQL.SQLCore.SQLMode;
import java.io.File;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHandler {
    /*
     * @author: alta189
     * 
     */

    private SQLCore core;
    private Connection connection;
    private File SQLFile;

    // Method to setup MySQL
    public DatabaseHandler(SQLCore core) {
        this.core = core;
    }

    // Method to set up SQLite
    public DatabaseHandler(SQLCore core, File SQLFile) {
        this.core = core;
        this.SQLFile = SQLFile;
    }

    public boolean openConnection() {
        boolean connected = false;
        if (core.mode == SQLMode.MySQL) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + core.dbHost + "/" + core.dbName, core.dbUser, core.dbPass);
                connected = true;
            } catch (ClassNotFoundException cnfe) {
                core.writeError("MySQL class not found " + cnfe, true);
            } catch (SQLException se) {
                core.writeError("MySQL exception on initilaize  " + se, true);
            }
        } else if (core.mode == SQLMode.SQLite) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + SQLFile.getAbsolutePath());
                connected = true;
            } catch (ClassNotFoundException cnfe) {
                core.writeError("You need the SQLite library " + cnfe, true);
            } catch (SQLException se) {
                core.writeError("SQLite exception on initialize " + se, true);
            }
        }
        return connected;
    }

    public Boolean checkConnection() {
        boolean connected = false;
        if (core.mode == SQLMode.MySQL) {
            if (connection == null) {
                openConnection();
                connected = true;
            }
        } else if (core.mode == SQLMode.SQLite) {
            connected = connection != null;
        }
        return connected;
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean closeConnection() {
        boolean closed = false;
        if (this.connection != null) {
            try {
                this.connection.close();
                closed = true;
            } catch (Exception ex) {
                this.core.writeError("Error on connection close: " + ex, true);
            }
        }
        return closed;
    }

    public boolean createTable(String query) {
        try {
            if (query == null) {
                core.writeError("SQL Create Table query empty.", true);
                return false;
            }

            Statement statement = connection.createStatement();
            statement.execute(query);
            return true;
        } catch (SQLException ex) {
            core.writeError(ex.getMessage(), true);
            return false;
        }
    }

    public ResultSet sqlQuery(String query) {
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(query);
            return result;
        } catch (SQLException ex) {
            if (ex.getMessage().toLowerCase().contains("locking") || ex.getMessage().toLowerCase().contains("locked")) {
                return retryResult(query);
            } else {
                core.writeError("Error at SQL Query: " + ex.getMessage(), false);
            }
        }
        return null;
    }

    public boolean insertQuery(String query) {
        boolean success = false;
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            success = true;
        } catch (SQLException ex) {
            if (ex.getMessage().toLowerCase().contains("locking") || ex.getMessage().toLowerCase().contains("locked")) {
                retry(query);
            } else {
                if (!ex.toString().contains("not return ResultSet")) {
                    core.writeError("Error at SQL INSERT Query: " + ex, false);
                }
            }
        }
        return success;
    }

    public boolean updateQuery(String query) {
        boolean success = false;
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
            success = true;
        } catch (SQLException ex) {
            if (ex.getMessage().toLowerCase().contains("locking") || ex.getMessage().toLowerCase().contains("locked")) {
                retry(query);
            } else {
                if (!ex.toString().contains("not return ResultSet")) {
                    core.writeError("Error at SQL UPDATE Query: " + ex, false);
                }
            }
        }
        return success;
    }

    public boolean deleteQuery(String query) {
        boolean success = false;
        try {
            Statement statement = connection.createStatement();
            statement.executeQuery(query);
            success = true;
        } catch (SQLException ex) {
            if (ex.getMessage().toLowerCase().contains("locking") || ex.getMessage().toLowerCase().contains("locked")) {
                retry(query);
            } else {
                if (!ex.toString().contains("not return ResultSet")) {
                    core.writeError("Error at SQL DELETE Query: " + ex, false);
                }
            }
        }
        return success;
    }

    public boolean wipeTable(String table) {
        try {
            if (!core.checkTable(table)) {
                core.writeError("Error at Wipe Table: table, " + table + ", does not exist", true);
                return false;
            }
            Statement statement = connection.createStatement();
            String query = "DELETE FROM '" + table + "'";
            statement.executeQuery(query);

            return true;
        } catch (SQLException ex) {
            if (ex.getMessage().toLowerCase().contains("locking") || ex.getMessage().toLowerCase().contains("locked")) {
                //retryWipe(query);
            } else {
                if (!ex.toString().contains("not return ResultSet")) {
                    core.writeError("Error at SQL WIPE TABLE Query: " + ex, false);
                }
            }
            return false;
        }
    }

    public boolean checkTable(String table) {
        DatabaseMetaData dbm;
        try {
            dbm = connection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, table, null);
            if (tables.next()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            core.writeError("Failed to check if table \"" + table + "\" exists: " + e.getMessage(), true);
            return false;
        }

    }

    public boolean checkField(String table, String column) {
        DatabaseMetaData dbm;
        boolean exists = false;
        try {
            dbm = connection.getMetaData();
            ResultSet columns = dbm.getColumns(null, null, table, column);
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                if (columnName.equals(column)) {
                    exists = true;
                    break;
                }
            }
        } catch (SQLException se) {
            core.writeError("Failed to check if column \"" + column + "\" exists: " + se.getMessage(), true);
            exists = false;
        }
        return exists;
    }

    private ResultSet retryResult(String query) {
        Boolean passed = false;

        while (!passed) {
            try {
                Statement statement = connection.createStatement();

                ResultSet result = statement.executeQuery(query);

                passed = true;

                return result;
            } catch (SQLException ex) {

                if (ex.getMessage().toLowerCase().contains("locking") || ex.getMessage().toLowerCase().contains("locked")) {
                    passed = false;
                } else {
                    core.writeError("Error at SQL Query: " + ex.getMessage(), false);
                }
            }
        }

        return null;
    }

    private void retry(String query) {
        boolean passed = false;
        while (!passed) {
            try {
                Statement statement = connection.createStatement();
                statement.executeQuery(query);
                passed = true;
                return;
            } catch (SQLException ex) {
                if (ex.getMessage().toLowerCase().contains("locking") || ex.getMessage().toLowerCase().contains("locked")) {
                    passed = false;
                } else {
                    core.writeError("Error at SQL Query: " + ex.getMessage(), false);
                }
            }
        }
    }
}
