package net.shockverse.survivalgames.core;

import java.util.ArrayList;
import java.util.List;
import net.shockverse.survivalgames.SurvivalGames;
import net.shockverse.survivalgames.core.Language.LangKey;
import org.bukkit.entity.Player;

/**
 * @description Handles all plugin help
 * @author Duker02, LegitModern, Tagette
 */
public class Help {

    private static SurvivalGames plugin;
    private static List<CommandHelp> helpList;

    private static class CommandHelp {

        public String name;
        public String desc;
        public String permission;

        public CommandHelp(String _name, String _help, String _permission) {
            this.name = _name;
            this.desc = _help;
            this.permission = _permission;
        }
    }

    public static void initialize(SurvivalGames instance) {
        Help.plugin = instance;
        helpList = new ArrayList<CommandHelp>();
        registerHelp();
    }

    public static void disable() {
        helpList.clear();
        helpList = null;
    }

    public static void registerHelp() {
        // Generic command help.
        register("sg help", "Shows the commands for " + plugin.name + ".");
        if(Constants.debugAllowed)
            register("sg debug", "Puts you and " + plugin.name + " into debug mode.", "survivalgames.admin.debug");
        register("sg reload", "Reloads the config files.", "survivalgames.admin.reload");
        register("sg config get <file> (key)", "Shows the config for a <file> filtered optionally by a (key).", "survivalgames.admin.config");
        register("sg config set <file> <key> <value>", "Modifys the config <file>'s <key> and sets it to <value>.", "survivalgames.admin.config");


        /* Register help here. */
        register("sg start (arenaNumber)", "Starts the game optionally in (arenaNumber) in 60 seconds.", "survivalgames.admin.start");
        register("sg startnow (arenaNumber)", "Starts the game optionally in (arenaNumber) immediately.", "survivalgames.admin.start");
        register("sg stop", "Returns everyone to lobby and ends the game.", "survivalgames.admin.stop");
        register("sg deathmatch", "Starts the deathmatch.", "survivalgames.admin.deathmatch");
        register("sg refillchests", "Refill all chests in the world.", "survivalgames.admin.refill");
        register("sg add <player>", "Add's <player> to the game.", "survivalgames.admin.add");
        register("sg inv <player>", "Opens <player>'s inventory for you to see.", "survivalgames.admin.inventory");
        register("sg enable <worldName>", "Enables and loads <worldName>.", "survivalgames.admin.enable");
        register("sg disable <worldName>", "Disables and unloads <worldName>.", "survivalgames.admin.disable");
        register("sg worlds", "Lists off the arenas by world name.", "survivalgames.admin.edit");
        register("sg addarena <worldName>", "Adds an arena if the world exists.", "survivalgames.admin.addarena");
        register("sg removearena <worldName>", "Removes an arena.", "survivalgames.admin.removearena");
        register("sg edit <worldName>", "Enter an arena to edit it.", "survivalgames.admin.edit");
        register("sg save", "Saves changes to an arena.", "survivalgames.admin.edit");
        register("sg setspawn <spawnNumber>", "Sets <spawnNumber> to where your standing for the arena your editing.", "survivalgames.admin.edit.setspawn");
        register("sg delspawn <spawnNumber>", "Deletes <spawnNumber> from the arena your editing.", "survivalgames.admin.setspawn");
        register("sg tpspawn <spawnNumber>", "Teleports you to spawn <spawnNumber> in the arena your editing.", "survivalgames.admin.edit.tpspawn");
        register("sg spawns", "Lists the spawns in the arena your editing.", "survivalgames.admin.edit");
        register("sg setdmspawn <spawnNumber>", "Sets <spawnNumber> to where your standing for the arena your editing.", "survivalgames.admin.edit.setspawn");
        register("sg deldmspawn <spawnNumber>", "Deletes <spawnNumber> from the arena your editing.", "survivalgames.admin.setspawn");
        register("sg tpdmspawn <spawnNumber>", "Teleports you to spawn <spawnNumber> in the arena your editing.", "survivalgames.admin.edit.tpspawn");
        register("sg dmspawns", "Lists the spawns in the arena your editing.", "survivalgames.admin.edit");
        register("sg timeleft", "Shows the time left before the game starts.", "survivalgames.basic.timeleft");
        register("sg list", "Shows who's playing.", "survivalgames.basic.list");
        register("sg vote <arenaNumber>", "Votes for the next arena to be <arenaNumber>.", "survivalgames.basic.vote");
        
    }

    private static void register(String command, String help, String permission) {
        helpList.add(new CommandHelp(command, help, permission));
    }

    private static void register(String command, String help) {
        helpList.add(new CommandHelp(command, help, ""));
    }

    public static String getReadableHelp(Player player, int page) {
        String readableHelp = Language.getLanguage(LangKey.noHelp);
        if(helpList != null){
            int total = 0;
            if (!helpList.isEmpty()) {
                int maxPerPage = 5;
                int count = 0;
                int currentPage = 1;
                Language.setVar("page", currentPage + "");
                readableHelp = Language.getLanguage(LangKey.helpTitle) + "\n";
                for (CommandHelp help : helpList) {
                    if (player == null || Perms.has(player, help.permission, true)) {
                        Language.setVar("label", help.name);
                        Language.setVar("desc", help.desc);
                        readableHelp += Language.getLanguage(LangKey.helpItem) + "\n";
                        count++;
                        total++;
                    }
                    if(count % maxPerPage == 0) {
                        if(currentPage != page) {
                            count = 0;
                            currentPage++;
                            Language.setVar("page", currentPage + "");
                            readableHelp = Language.getLanguage(LangKey.helpTitle) + "\n";
                        } else {
                            break;
                        }
                    }
                }
                readableHelp = readableHelp.substring(0, readableHelp.length() - 1);
            }
            if(total == 0) {
                readableHelp = Language.getLanguage(LangKey.noHelp);
            }
        }
        return readableHelp;
    }
}
