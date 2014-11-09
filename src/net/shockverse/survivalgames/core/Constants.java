package net.shockverse.survivalgames.core;

/**
 * @description Holds all of the plugins constants.
 * @author Duker02, LegitModern, Tagette
 */
public class Constants {
    // Used for the Plugin Auto-Updater
    public static boolean updaterEnabled = false;
    
    public static double currentVersion = 1.0;
    public static double currentSubVersion = 0;
    // These need to be direct links to the files.
    public static String updateHistoryUrl = "HTML URL";
    public static String downloadUrl = "JAR URL";
    
    // Set to false to stop the plugin from using a database.
    public static boolean databaseEnabled = true;
    
    // Set to true if /debug is allowed for admin users of this plugin.
    public static boolean debugAllowed = true;
    
    // Set to true if lowDetailMode is allowed for the plugin.
    // Warning! If this is set to false then the option will not be available in the settings file.
    public static boolean lowDetailAllowed = true;
}
