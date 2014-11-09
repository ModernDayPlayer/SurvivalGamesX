package net.shockverse.survivalgames.extras;

import java.util.HashMap;
import java.util.Map;
import net.shockverse.survivalgames.SurvivalGames;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * @description Handles commands for this plugin.
 * @author Duker02, LegitModern, Tagette
 */
public class CommandManager {

    private SurvivalGames plugin;
    private Map<String, CommandExecutor> commands = new HashMap<String, CommandExecutor>();

    public CommandManager(SurvivalGames instance) {
        this.plugin = instance;
    }
    
    public void disable() {
        commands.clear();
        commands = null;
    }

    public void addCommand(String label, CommandExecutor executor) {
        commands.put(label, executor);
    }

    public boolean dispatch(CommandSender sender, Command command, String label, String[] args) {
        if (!commands.containsKey(label)) {
            return false;
        }

        boolean handled = true;

        CommandExecutor ce = commands.get(label);
        handled = ce.onCommand(sender, command, label, args);

        return handled;
    }
}