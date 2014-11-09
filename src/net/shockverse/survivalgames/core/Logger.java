package net.shockverse.survivalgames.core;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @description Handles the logging of the plugin
 * @author Duker02, LegitModern, Tagette
 */
public class Logger {

    private static ConsoleCommandSender log;
    private static JavaPlugin plugin;
    private static String prefix;

    public static void initialize(JavaPlugin instance) {
        plugin = instance;
        prefix = "";
        log = plugin.getServer().getConsoleSender();
    }
    
    public static void disable(){
        log = null;
    }

    public static ConsoleCommandSender getLogger() {
        return log;
    }

    public static String getPrefix() {
        return prefix;
    }

    public static void setPrefix(String p) {
        prefix = p;
    }

    public static void info(String message) {
        log.sendMessage(getPrefix() + message);
    }

    public static void error(String message) {
        log.sendMessage(getPrefix() + ChatColor.RED + message);
    }

    public static void warning(String message) {
        log.sendMessage(getPrefix() + ChatColor.GOLD + message);
    }

    public static void config(String message) {
        log.sendMessage(getPrefix() + ChatColor.DARK_BLUE + message);
    }
}
