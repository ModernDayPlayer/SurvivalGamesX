package net.shockverse.survivalgames.commands;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import net.shockverse.survivalgames.*;
import net.shockverse.survivalgames.GameManager.SGGameState;
import net.shockverse.survivalgames.api.SGAPI;
import net.shockverse.survivalgames.core.Language.LangKey;
import net.shockverse.survivalgames.core.*;
import net.shockverse.survivalgames.data.ArenaData;
import net.shockverse.survivalgames.extras.*;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @description Handles a command.
 *
 * @author Duker02, LegitModern, Tagette
 */
public class SGCmd implements CommandExecutor {

    private SurvivalGames plugin;
    private Debug debug;
    private CommandSender cSender;

    public SGCmd(SurvivalGames instance) {
        plugin = instance;
        debug = plugin.getDebug();
    }

    // Generic commands for the plugin.
    private boolean defaultCommand(String label, String[] args) {
        boolean isDefault = false;
        if (is(args[0], "debug")) {
            if (Constants.debugAllowed) {
                isDefault = true;
                if (isPlayer()) {
                    if (Perms.hasAll(getPlayer()) || Perms.has(getPlayer(), "survivalgames.admin.debug", getPlayer().isOp())
                            || plugin.getDescription().getAuthors().contains(getName())) {
                        if (!debug.inDebugMode()) {
                            Logger.info("Debug mode initiated.");
                            sendMessage(colorizeText(plugin.name, ChatColor.GREEN) + " has begun debugging.");
                        }
                        // Toggles debug mode for player.
                        if (!debug.isDebugging(getPlayer())) {
                            debug.startDebugging(getPlayer());
                            sendMessage("You have entered debug mode for " + colorizeText(plugin.name, ChatColor.GREEN) + ".");
                        } else if (args.length == 1) {
                            debug.stopDebugging(getPlayer());
                            sendMessage("You have exited debug mode for " + colorizeText(plugin.name, ChatColor.GREEN) + ".");
                            if (!debug.inDebugMode()) {
                                Logger.info("Debug mode terminated.");
                            }
                        }
                        if (args.length == 2) {
                            DebugDetailLevel debugLevel = DebugDetailLevel.NORMAL;
                            try {
                                debugLevel = DebugDetailLevel.valueOf(args[1].toUpperCase());
                            } catch (Exception e) {
                                int index = 1;
                                try {
                                    index = Integer.parseInt(args[1]);
                                } catch (Exception fe) {
                                    index = 1;
                                }
                                if (index >= 0 && index < DebugDetailLevel.values().length) {
                                    debugLevel = DebugDetailLevel.values()[index];
                                }
                            }
                            if (debugLevel != null) {
                                debug.setDetailLevel(getPlayer(), debugLevel);
                                sendMessage("You have set your debug detail to " + debugLevel.toString() + ".");
                            }
                        }
                    }
                } else {
                    if (!debug.inDebugMode()) {
                        Logger.info("Debug mode initiated.");
                        debug.startDebugging();
                    } else if (args.length == 1) {
                        Logger.info("Debug mode terminated.");
                        debug.stopDebugging("You have exited debug mode for " + colorizeText(plugin.name, ChatColor.GREEN) + ".");
                    }
                    if (args.length == 2) {
                        DebugDetailLevel debugLevel = DebugDetailLevel.NORMAL;
                        try {
                            debugLevel = DebugDetailLevel.valueOf(args[1].toUpperCase());
                        } catch (Exception e) {
                            int index = 1;
                            try {
                                index = Integer.parseInt(args[1]);
                            } catch (Exception fe) {
                                index = 1;
                            }
                            if (index >= 0 && index < DebugDetailLevel.values().length) {
                                debugLevel = DebugDetailLevel.values()[index];
                            }
                        }
                        if (debugLevel != null) {
                            debug.setDetailLevel(debugLevel);
                            Logger.info("Debug detail: " + debugLevel.name());
                        }
                    }
                }
            }
        } else if (is(args[0], "reload")) {
            isDefault = true;
            if (isPlayer()) {
                if (Perms.has(getPlayer(), "survivalgames.admin.reload", getPlayer().isOp())) {
                    plugin.onDisable();
                    plugin.onEnable();
                    sendMessage(colorizeText(plugin.name, ChatColor.GREEN) + " version " + colorizeText(plugin.version, ChatColor.GREEN) + " has been reloaded.");

                }
            } else {
                plugin.onDisable();
                plugin.onEnable();
                Logger.info(plugin.name + " version " + plugin.version + " has been reloaded.");
            }
        } else if (is(args[0], "config")) { // /sg config [set|remove|get] [path/file] [shortcut] [newValue]
            if (!isPlayer() || Perms.has(getPlayer(), "survivalgames.admin.config", getPlayer().isOp())) {
                if(args.length > 1) {
                    String action = args[1];
                    if(is(action, "set") && args.length == 5) {
                        isDefault = true;
                        String pathFile = args[2];
                        String shortcut = args[3];
                        String newValue = args[4];
                        PropertyFile file = PropertyFile.getFile(pathFile);
                        if(file != null) {
                            PropertyEntry entry = file.getProperty(shortcut);
                            if(entry != null) {
                                entry.setValue(newValue);
                                Language.setVar("file", pathFile);
                                Language.setVar("key", shortcut);
                                Language.setVar("value", newValue);
                                file.saveProperties();
                                sendLanguage(LangKey.configSet);
                            } else {
                                Language.setVar("file", pathFile);
                                Language.setVar("key", shortcut);
                                Language.setVar("value", newValue);
                                sendLanguage(LangKey.configNotKey);
                            }
                        } else {
                            Language.setVar("file", pathFile);
                            sendLanguage(LangKey.configNotFile);
                        }
                    } else if(is(action, "remove") && args.length == 4) {
                        isDefault = true;
                        String pathFile = args[2];
                        String shortcut = args[3];
                        PropertyFile file = PropertyFile.getFile(pathFile);
                        if(file != null) {
                            PropertyEntry entry = file.getProperty(shortcut);
                            if(entry != null) {
                                PropertyList parent = (PropertyList) entry.getParent();
                                parent.removeProperty(entry.getKey());
                                Language.setVar("file", pathFile);
                                Language.setVar("key", shortcut);
                                file.saveProperties();
                                sendLanguage(LangKey.configRemoved);
                            } else {
                                Language.setVar("file", pathFile);
                                Language.setVar("key", shortcut);
                                sendLanguage(LangKey.configNotKey);
                            }
                        } else {
                            Language.setVar("file", pathFile);
                            sendLanguage(LangKey.configNotFile);
                        }
                    } else if(is(action, "get") && args.length >= 3 && args.length <= 4) {
                        isDefault = true;
                        String pathFile = args[2];
                        String shortcut = args.length == 4 ? args[3] : null;
                        PropertyFile file = PropertyFile.getFile(pathFile);
                        if(file != null) {
                            PropertyEntry entry = file.getProperty(shortcut);
                            if(entry != null) {
                                String configs = "";
                                if(entry instanceof PropertyList) {
                                    for(PropertyEntry listEntry : ((PropertyList) entry).getProperties()) {
                                        Language.setVar("key", listEntry.getKey());
                                        Language.setVar("value", (listEntry instanceof PropertyList ? "(List)" : listEntry.getValue()));
                                        Language.setVar("comment", listEntry.getComment());
                                        configs += Language.getLanguage(LangKey.configGet) + "\n";
                                    }
                                    if(configs.length() > 0)
                                        configs.substring(0, configs.length() - 1);
                                } else {
                                    Language.setVar("key", entry.getKey());
                                    Language.setVar("value", entry.getValue());
                                    Language.setVar("comment", "# " + entry.getComment());
                                    configs += Language.getLanguage(LangKey.configGet);
                                }
                                if(Tools.isNullEmptyWhite(configs))
                                    configs = "No config.";
                                Language.setVar("file", pathFile);
                                Language.setVar("configs", configs);
                                sendLanguage(LangKey.configGetList);
                            } else {
                                Language.setVar("file", pathFile);
                                Language.setVar("key", shortcut);
                                sendLanguage(LangKey.configNotKey);
                            }
                        } else {
                            Language.setVar("file", pathFile);
                            sendLanguage(LangKey.configNotFile);
                        }
                    }
                }
            }
        } else if (is(args[0], "help") || is(args[0], "?")) {
            isDefault = true;
            int page = 1;
            if(args.length > 1 && Tools.isInt(args[1])) {
                page = Integer.parseInt(args[1]);
            }
            for (String help : Help.getReadableHelp(getPlayer(), page).split("\n")) {
                sendMessage(help);
            }
        }
        return isDefault;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Assigning cSender is required if you plan to use the built
        //      in commands such as sendMessage(), sendLog() or getName().
        cSender = sender;
        boolean handled = false;
        // This is when the user types only the command label.
        // For example '/template'.
        if (args == null || args.length == 0) {
            handled = true;

            // Will send message only if the sender is a player.
            sendMessage("You are using " + colorizeText(plugin.name, ChatColor.GREEN)
                    + " version " + colorizeText(plugin.version, ChatColor.GREEN) + " by " + plugin.getDescription().getAuthors() + ".");
        } // This checks and runs the default commands. If the command is not a 
        //     default command then it continues to your commands.
        // There are 3 default commands: debug, reload and help.
        else if (!defaultCommand(label, args)) {
            
            GameManager gameMan = plugin.getGameManager();
            ArenaManager arenaMan = plugin.getArenaManager();

            // Put your commands in here
            // help, reload, debug are already used.
            if (is(label, "sg") || is(label, "survivalgames") 
                    || is(label, "hg") || is(label, "hungergames")) {
                if (is(args[0], "start")) {
                    handled = true;
                    if (!isPlayer() || Perms.has(getPlayer(), "survivalgames.admin.start", getPlayer().isOp())) {
                        if (args.length == 1) {
                            sendMessage(SGAPI.startGame().message);
                        } else if (Tools.isInt(args[1])) {
                            int arenaNumber = Integer.parseInt(args[1]);
                            if (arenaNumber > 0 && arenaNumber <= arenaMan.arenaOrder.size()) {
                                sendMessage(SGAPI.startGame(arenaMan.arenaOrder.get(arenaNumber - 1)).message);
                            } else {
                                Language.setVar("min", 1 + "");
                                Language.setVar("max", arenaMan.arenaOrder.size() + "");
                                sendLanguage(LangKey.outOfRange);
                            }
                        } else {
                            Language.setVar("min", 1 + "");
                            Language.setVar("max", arenaMan.arenaOrder.size() + "");
                            sendLanguage(LangKey.outOfRange);
                        }
                    } else {
                        // No permission
                    }
                } else if (is(args[0], "startnow")) {
                    handled = true;
                    if (!isPlayer() || Perms.has(getPlayer(), "survivalgames.admin.start", getPlayer().isOp())) {
                        if (args.length == 1) {
                            sendMessage(SGAPI.startGame(0).message);
                        } else if (Tools.isInt(args[1])) {
                            int arenaNumber = Integer.parseInt(args[1]);
                            if (arenaNumber > 0 && arenaNumber <= arenaMan.arenaOrder.size()) {
                                sendMessage(SGAPI.startGame(arenaMan.arenaOrder.get(arenaNumber - 1), 0).message);
                            } else {
                                Language.setVar("min", 1 + "");
                                Language.setVar("max", arenaMan.arenaOrder.size() + "");
                                sendLanguage(LangKey.outOfRange);
                            }
                        } else {
                            Language.setVar("min", 1 + "");
                            Language.setVar("max", arenaMan.arenaOrder.size() + "");
                            sendLanguage(LangKey.outOfRange);
                        }
                    } else {
                        // No permission
                    }
                } else if (is(args[0], "stop")) {
                    handled = true;
                    if (!isPlayer() || Perms.has(getPlayer(), "survivalgames.admin.stop", getPlayer().isOp())) {
                        if (gameMan.getState() != SGGameState.LOBBY) {
                            gameMan.cancelTasks();
                            gameMan.endGame();
                            sendLanguage(LangKey.adminGameStopped);
                        } else {
                            sendLanguage(LangKey.adminGameNotStarted);
                        }
                    } else {
                        // No permission
                    }
                } else if (is(args[0], "deathmatch") || is(args[0], "dm")) {
                    handled = true;
                    if (!isPlayer() || Perms.has(getPlayer(), "survivalgames.admin.deathmatch", getPlayer().isOp())) {
                        sendMessage(SGAPI.startDeathmatch().message);
                    } else {
                        // No permission
                    }
                } else if (is(args[0], "refillchests") || is(args[0], "refill")) {
                    handled = true;
                    if (!isPlayer() || Perms.has(getPlayer(), "survivalgames.admin.refill", getPlayer().isOp())) {
                        arenaMan.refillContainers();
                        sendLanguage(LangKey.adminRefillChests);
                        
                    } else {
                        // No permission
                    }
                } else if (is(args[0], "join") || is(args[0], "add") 
                        || is(args[0], "revive") || is(args[0], "heal")) {
                    handled = true;
                    if (!isPlayer() || Perms.has(getPlayer(), "survivalgames.admin.add", getPlayer().isOp())) {
                        if(args.length == 1) {
                            if(isPlayer()) {
                                sendMessage(SGAPI.addPlayerToGame(getPlayer()).message);
                            } else {
                                sendLanguage(LangKey.notPlayer);
                            }
                        } else {
                            List<Player> matched = plugin.getServer().matchPlayer(args[1]);
                            if (matched.size() > 0) {
                                Player target = matched.get(0);
                                sendMessage(SGAPI.addPlayerToGame(target).message);
                            } else {
                                Language.setVar("notFound", args[1]);
                                sendLanguage(LangKey.playerNotFound);
                            }
                        }
                    }
                } else if (is(args[0], "leave") || is(args[0], "spectate") 
                        || is(args[0], "spec") || is(args[0], "exit") 
                        || is(args[0], "remove")) {
                    handled = true;
                    if (!isPlayer() || Perms.has(getPlayer(), "survivalgames.admin.add", getPlayer().isOp())) {
                        if(args.length == 1) {
                            if(isPlayer()) {
                                sendMessage(SGAPI.removePlayerFromGame(getPlayer()).message);
                            } else {
                                sendLanguage(LangKey.notPlayer);
                            }
                        } else {
                            List<Player> matched = plugin.getServer().matchPlayer(args[1]);
                            if (matched.size() > 0) {
                                Player target = matched.get(0);
                                sendMessage(SGAPI.removePlayerFromGame(target).message);
                            } else {
                                Language.setVar("notFound", args[1]);
                                sendLanguage(LangKey.playerNotFound);
                            }
                        }
                    }
                }
                else if (is(args[0], "inv") || is(args[0], "inventory") || is(args[0], "view")) {
                    handled = true;
                    if (isPlayer()) {
                        if (Perms.has(getPlayer(), "survivalgames.basic.inventory", getPlayer().isOp())) {
                            if (gameMan.getState() != SGGameState.LOBBY) {
                                List<Player> matched = plugin.getServer().matchPlayer(args[1]);
                                if (matched.size() > 0) {
                                    Player target = matched.get(0);
                                    if (gameMan.isTribute(target)) {
                                        getPlayer().openInventory(target.getInventory());
                                        Language.setTarget(target);
                                        sendLanguage(LangKey.adminViewInv);
                                    } else if (gameMan.isSpectator(target)) {
                                        Language.setTarget(target);
                                        sendLanguage(LangKey.noSpecInv);
                                    }
                                } else {
                                    Language.setVar("notFound", args[1]);
                                    sendLanguage(LangKey.playerNotFound);
                                }
                            } else {
                                sendLanguage(LangKey.noLobbyInv);
                            }
                        } else {
                            // No permission
                        }
                    } else {
                        sendLanguage(LangKey.notPlayer);
                    }
                } else if (is(args[0], "enable")) {
                    handled = true;
                    if (!isPlayer() || Perms.has(getPlayer(), "survivalgames.admin.enable", getPlayer().isOp())) {
                        String wName = Tools.join(1, args, " ");
                        ArenaData aData = arenaMan.get(wName);
                        if(aData != null) {
                            if(!aData.enabled) {
                                arenaMan.enableArena(wName, true);
                            } else {
                                Language.setVar("arena", wName);
                                sendLanguage(LangKey.arenaAlreadyEnabled);
                            }
                        } else {
                            Language.setVar("worldname", wName);
                            sendLanguage(LangKey.arenaNotFound);
                        }
                    } else {
                        sendLanguage(LangKey.noPermission);
                    }
                } else if (is(args[0], "disable")) {
                    handled = true;
                    if (!isPlayer() || Perms.has(getPlayer(), "survivalgames.admin.disable", getPlayer().isOp())) {
                        String wName = Tools.join(1, args, " ");
                        ArenaData aData = arenaMan.get(wName);
                        if(aData != null) {
                            if(aData.enabled) {
                                arenaMan.disableArena(wName, true);
                                gameMan.updateScoreBoards();
                            } else {
                                Language.setVar("arena", wName);
                                sendLanguage(LangKey.arenaAlreadyDisabled);
                            }
                        } else {
                            Language.setVar("worldname", wName);
                            sendLanguage(LangKey.arenaNotFound);
                        }
                    } else {
                        sendLanguage(LangKey.noPermission);
                    }
                } else if (is(args[0], "addarena") || is(args[0], "newarena")
                        || is(args[0], "createarena")) {
                    handled = true;
                    if (!isPlayer() || Perms.has(getPlayer(), "survivalgames.admin.addarena", getPlayer().isOp())) {
                        if(args.length > 1) {
                            String wName = Tools.join(1, args, " ");
                            ArenaData aData = arenaMan.get(wName);
                            if(aData == null) {
                                Language.setVar("worldname", wName);
                                sendLanguage(LangKey.arenaAdding);
                                if(arenaMan.addArena(wName)) {
                                    aData = arenaMan.get(wName);
                                    Language.setVar("worldname", aData.worldName);
                                    Language.setVar("arenaname", aData.name);
                                    Language.setVar("spawns", aData.spawns.size() + "");
                                    Language.setVar("dmspawns", aData.dmSpawns.size() + "");
                                    Language.setVar("containers", aData.containers.size() + "");
                                    Language.setVar("placelist", aData.placeWhitelist.size() + "");
                                    Language.setVar("breaklist", aData.breakWhitelist.size() + "");
                                    sendLanguage(LangKey.arenaAdded);
                                    if(aData.enabled) {
                                        arenaMan.arenaOrder.add(aData.worldName);
                                        if(arenaMan.getNextArena() == null)
                                            arenaMan.setNextArena(aData);
                                    } else
                                        sendLanguage(LangKey.arenaNoSpawns);
                                } else {
                                    Language.setVar("worldname", wName);
                                    sendLanguage(LangKey.arenaNotAdded);
                                }
                            } else {
                                Language.setVar("worldname", wName);
                                sendLanguage(LangKey.arenaAlreadyExists);
                            }
                        } else {
                            handled = false;
                        }
                    } else {
                        sendLanguage(LangKey.noPermission);
                    }
                } else if (is(args[0], "removearena") || is(args[0], "delarena")
                        || is(args[0], "destroyarena")) {
                    handled = true;
                    if (!isPlayer() || Perms.has(getPlayer(), "survivalgames.admin.removearena", getPlayer().isOp())) {
                        if(args.length > 1) {
                            String wName = Tools.join(1, args, " ");
                            ArenaData aData = arenaMan.get(wName);
                            if(aData != null && arenaMan.removeArena(wName)) {
                                arenaMan.arenaOrder.remove(wName);
                                if(arenaMan.getNextArena() == null && !arenaMan.arenaOrder.isEmpty())
                                    arenaMan.setNextArena(arenaMan.arenaOrder.get(0));
                                Language.setVar("worldname", wName);
                                sendLanguage(LangKey.arenaRemoved);
                            } else {
                                Language.setVar("worldname", wName);
                                sendLanguage(LangKey.arenaNotFound);
                            }
                        } else {
                            handled = false;
                        }
                    } else {
                        sendLanguage(LangKey.noPermission);
                    }
                } else if (is(args[0], "spawns") || is(args[0], "listspawns") || is(args[0], "showspawns")) {
                    handled = true;
                    if(isPlayer()) {
                        if (Perms.has(getPlayer(), "survivalgames.admin.setspawn", getPlayer().isOp())
                                || Perms.has(getPlayer(), "survivalgames.admin.tpspawn", getPlayer().isOp())) {
                            World world = getPlayer().getWorld();
                            ArenaData aData = arenaMan.get(world.getName());
                            Language.setVar("arenaname", aData.name);
                            Language.setVar("worldname", aData.worldName);
                            String spawnsStr = "";
                            for(int spawnNumber : aData.spawns.keySet()) {
                                Language.setVar("number", spawnNumber + "");
                                Language.setVar("location", Tools.toString(aData.spawns.get(spawnNumber), false));
                                spawnsStr += Language.getLanguage(LangKey.adminSpawnListItem) + "\n";
                            }
                            if(Tools.isNullEmptyWhite(spawnsStr)) {
                                spawnsStr = Language.getLanguage(LangKey.adminNoSpawns);
                            } else {
                                spawnsStr = spawnsStr.substring(0, spawnsStr.length() - 1);
                            }
                            Language.setVar("spawns", spawnsStr);
                            sendLanguage(LangKey.adminSpawnList);
                        } else {
                            sendLanguage(LangKey.noPermission);
                        }
                    } else {
                        sendLanguage(LangKey.notPlayer);
                    }
                } else if (is(args[0], "tpspawn") || is(args[0], "gotospawn") || is(args[0], "tospawn")) {
                    handled = true;
                    if(isPlayer()) {
                        if (Perms.has(getPlayer(), "survivalgames.admin.tpspawn", getPlayer().isOp())) {
                            if(Tools.isInt(args[1])) {
                                int spawnNumber = Integer.parseInt(args[1]);
                                World world = getPlayer().getWorld();
                                ArenaData aData = arenaMan.get(world.getName());
                                if(arenaMan.isEditing(getName(), aData.worldName)){
                                    if(aData.spawns.containsKey(spawnNumber)) {
                                        Location loc = aData.spawns.get(spawnNumber);
                                        getPlayer().teleport(loc);
                                        Language.setVar("number", spawnNumber + "");
                                        Language.setVar("location", loc.toString());
                                        sendLanguage(LangKey.adminTpSpawn);
                                    } else {
                                        Language.setVar("number", spawnNumber + "");
                                        sendLanguage(LangKey.adminNotSpawn);
                                    }
                                } else {
                                    Language.setVar("worldname", world.getName());
                                    Language.setVar("arenaname", aData.name);
                                    sendLanguage(LangKey.adminNotEditing);
                                }
                            }
                        } else {
                            sendLanguage(LangKey.noPermission);
                        }
                    } else {
                        sendLanguage(LangKey.notPlayer);
                    }
                } else if (is(args[0], "setspawn") || is(args[0], "addspawn") 
                        || is(args[0], "newspawn")) {
                    handled = true;
                    if(isPlayer()) {
                        if (Perms.has(getPlayer(), "survivalgames.admin.setspawn", getPlayer().isOp())) {
                            if(Tools.isInt(args[1]) || is(args[1], "next")) {
                                World world = getPlayer().getWorld();
                                ArenaData aData = arenaMan.get(world.getName());
                                int spawnNumber = 0;
                                if(is(args[1], "next")) {
                                    for(int spawnKey : aData.spawns.keySet()) {
                                        if(spawnKey > spawnNumber)
                                            spawnNumber = spawnKey;
                                    }
                                    spawnNumber++;
                                } else {
                                    spawnNumber = Integer.parseInt(args[1]);
                                }
                                if(arenaMan.isEditor(getName())){
                                    Location pLoc = getPlayer().getLocation();
                                    arenaMan.setSpawn(aData.worldName, spawnNumber, Tools.round(pLoc, 2)); 
                                    Language.setVar("number", spawnNumber + "");
                                    Language.setVar("location", Tools.toString(Tools.round(pLoc, 2), false));
                                    sendLanguage(LangKey.adminSpawnSet);
                                } else {
                                    Language.setVar("worldname", world.getName());
                                    Language.setVar("arenaname", aData.name);
                                    sendLanguage(LangKey.adminNotEditing);
                                }
                            }
                        } else {
                            sendLanguage(LangKey.noPermission);
                        }
                    } else {
                        sendLanguage(LangKey.notPlayer);
                    }
                } else if (is(args[0], "removespawn") || is(args[0], "delspawn")
                         || is(args[0], "deletespawn") || is(args[0], "clearspawn")
                         || is(args[0], "rmspawn")) {
                    handled = true;
                    if(isPlayer()) {
                        if (Perms.has(getPlayer(), "survivalgames.admin.setspawn", getPlayer().isOp())) {
                            if(Tools.isInt(args[1])) {
                                int spawnNumber = Integer.parseInt(args[1]);
                                World world = getPlayer().getWorld();
                                ArenaData aData = arenaMan.get(world.getName());
                                if(arenaMan.isEditor(getName())){
                                    arenaMan.removeSpawn(aData.worldName, spawnNumber); 
                                    Language.setVar("number", spawnNumber + "");
                                    sendLanguage(LangKey.adminSpawnRemove);
                                } else {
                                    Language.setVar("worldname", world.getName());
                                    Language.setVar("arenaname", aData.name);
                                    sendLanguage(LangKey.adminNotEditing);
                                }
                            }
                        } else {
                            sendLanguage(LangKey.noPermission);
                        }
                    } else {
                        sendLanguage(LangKey.notPlayer);
                    }
                } else if (is(args[0], "setspecspawn") || is(args[0], "addspecspawn") 
                        || is(args[0], "newspecspawn")) {
                    handled = true;
                    if(isPlayer()) {
                        if (Perms.has(getPlayer(), "survivalgames.admin.setspawn", getPlayer().isOp())) {
                            World world = getPlayer().getWorld();
                            ArenaData aData = arenaMan.get(world.getName());
                            if(arenaMan.isEditor(getName())){
                                Location pLoc = getPlayer().getLocation();
                                arenaMan.setSpectatorSpawn(aData.worldName, Tools.round(pLoc, 2));
                                Language.setVar("location", Tools.toString(Tools.round(pLoc, 2), false));
                                sendLanguage(LangKey.adminSpecSpawnSet);
                            } else {
                                Language.setVar("worldname", world.getName());
                                Language.setVar("arenaname", aData.name);
                                sendLanguage(LangKey.adminNotEditing);
                            }
                        } else {
                            sendLanguage(LangKey.noPermission);
                        }
                    } else {
                        sendLanguage(LangKey.notPlayer);
                    }
                } else if (is(args[0], "dmspawns") || is(args[0], "listdmspawns") || is(args[0], "showdmspawns")) {
                    handled = true;
                    if(isPlayer()) {
                        if (Perms.has(getPlayer(), "survivalgames.admin.setspawn", getPlayer().isOp())
                                || Perms.has(getPlayer(), "survivalgames.admin.tpspawn", getPlayer().isOp())) {
                            World world = getPlayer().getWorld();
                            ArenaData aData = arenaMan.get(world.getName());
                            Language.setVar("arenaname", aData.name);
                            Language.setVar("worldname", aData.worldName);
                            String spawnsStr = "";
                            for(int spawnNumber : aData.dmSpawns.keySet()) {
                                Language.setVar("number", spawnNumber + "");
                                Language.setVar("location", Tools.toString(aData.dmSpawns.get(spawnNumber), false));
                                spawnsStr += Language.getLanguage(LangKey.adminDMSpawnListItem) + "\n";
                            }
                            if(Tools.isNullEmptyWhite(spawnsStr)) {
                                spawnsStr = Language.getLanguage(LangKey.adminNoDMSpawns);
                            } else {
                                spawnsStr = spawnsStr.substring(0, spawnsStr.length() - 1);
                            }
                            Language.setVar("spawns", spawnsStr);
                            sendLanguage(LangKey.adminDMSpawnList);
                        } else {
                            sendLanguage(LangKey.noPermission);
                        }
                    } else {
                        sendLanguage(LangKey.notPlayer);
                    }
                } else if (is(args[0], "tpdmspawn") || is(args[0], "gotodmspawn") || is(args[0], "todmspawn")) {
                    handled = true;
                    if(isPlayer()) {
                        if (Perms.has(getPlayer(), "survivalgames.admin.tpspawn", getPlayer().isOp())) {
                            if(Tools.isInt(args[1])) {
                                int spawnNumber = Integer.parseInt(args[1]);
                                World world = getPlayer().getWorld();
                                ArenaData aData = arenaMan.get(world.getName());
                                if(arenaMan.isEditing(getName(), aData.worldName)){
                                    if(aData.dmSpawns.containsKey(spawnNumber)) {
                                        Location loc = aData.dmSpawns.get(spawnNumber);
                                        getPlayer().teleport(loc);
                                        Language.setVar("number", spawnNumber + "");
                                        Language.setVar("location", loc.toString());
                                        sendLanguage(LangKey.adminTpDMSpawn);
                                    } else {
                                        Language.setVar("number", spawnNumber + "");
                                        sendLanguage(LangKey.adminNotDMSpawn);
                                    }
                                } else {
                                    Language.setVar("worldname", world.getName());
                                    Language.setVar("arenaname", aData.name);
                                    sendLanguage(LangKey.adminNotEditing);
                                }
                            }
                        } else {
                            sendLanguage(LangKey.noPermission);
                        }
                    } else {
                        sendLanguage(LangKey.notPlayer);
                    }
                } else if (is(args[0], "setdmspawn") || is(args[0], "adddmspawn") 
                        || is(args[0], "newdmspawn")) {
                    handled = true;
                    if(isPlayer()) {
                        if (Perms.has(getPlayer(), "survivalgames.admin.setspawn", getPlayer().isOp())) {
                            if(Tools.isInt(args[1]) || is(args[1], "next")) {
                                World world = getPlayer().getWorld();
                                ArenaData aData = arenaMan.get(world.getName());
                                int spawnNumber = 0;
                                if(is(args[1], "next")) {
                                    for(int spawnKey : aData.dmSpawns.keySet()) {
                                        if(spawnKey > spawnNumber)
                                            spawnNumber = spawnKey;
                                    }
                                    spawnNumber++;
                                } else {
                                    spawnNumber = Integer.parseInt(args[1]);
                                }
                                if(arenaMan.isEditor(getName())){
                                    Location pLoc = getPlayer().getLocation();
                                    arenaMan.setDMSpawn(aData.worldName, spawnNumber, Tools.round(pLoc, 2)); 
                                    Language.setVar("number", spawnNumber + "");
                                    Language.setVar("location", Tools.toString(Tools.round(pLoc, 2), false));
                                    sendLanguage(LangKey.adminDMSpawnSet);
                                } else {
                                    Language.setVar("worldname", world.getName());
                                    Language.setVar("arenaname", aData.name);
                                    sendLanguage(LangKey.adminNotEditing);
                                }
                            }
                        } else {
                            sendLanguage(LangKey.noPermission);
                        }
                    } else {
                        sendLanguage(LangKey.notPlayer);
                    }
                } else if (is(args[0], "removedmspawn") || is(args[0], "deldmspawn")
                         || is(args[0], "deletedmspawn") || is(args[0], "cleardmspawn")
                         || is(args[0], "rmdmspawn")) {
                    handled = true;
                    if(isPlayer()) {
                        if (Perms.has(getPlayer(), "survivalgames.admin.setspawn", getPlayer().isOp())) {
                            if(Tools.isInt(args[1])) {
                                int spawnNumber = Integer.parseInt(args[1]);
                                World world = getPlayer().getWorld();
                                ArenaData aData = arenaMan.get(world.getName());
                                if(arenaMan.isEditor(getName())){
                                    arenaMan.removeDMSpawn(aData.worldName, spawnNumber); 
                                    Language.setVar("number", spawnNumber + "");
                                    sendLanguage(LangKey.adminDMSpawnRemove);
                                } else {
                                    Language.setVar("worldname", world.getName());
                                    Language.setVar("arenaname", aData.name);
                                    sendLanguage(LangKey.adminNotEditing);
                                }
                            }
                        } else {
                            sendLanguage(LangKey.noPermission);
                        }
                    } else {
                        sendLanguage(LangKey.notPlayer);
                    }
                } else if (is(args[0], "setdmspecspawn") || is(args[0], "adddmspecspawn") 
                        || is(args[0], "newdmspecspawn")) {
                    handled = true;
                    if(isPlayer()) {
                        if (Perms.has(getPlayer(), "survivalgames.admin.setspawn", getPlayer().isOp())) {
                            World world = getPlayer().getWorld();
                            ArenaData aData = arenaMan.get(world.getName());
                            if(arenaMan.isEditor(getName())){
                                Location pLoc = getPlayer().getLocation();
                                arenaMan.setDMSpectatorSpawn(aData.worldName, Tools.round(pLoc, 2));
                                Language.setVar("location", Tools.toString(Tools.round(pLoc, 2), false));
                                sendLanguage(LangKey.adminDMSpecSpawnSet);
                            } else {
                                Language.setVar("worldname", world.getName());
                                Language.setVar("arenaname", aData.name);
                                sendLanguage(LangKey.adminNotEditing);
                            }
                        } else {
                            sendLanguage(LangKey.noPermission);
                        }
                    } else {
                        sendLanguage(LangKey.notPlayer);
                    }
                } else if (is(args[0], "setdmcenter") || is(args[0], "adddmcenter") 
                        || is(args[0], "newdmcenter")) {
                    handled = true;
                    if(isPlayer()) {
                        if (Perms.has(getPlayer(), "survivalgames.admin.setspawn", getPlayer().isOp())) {
                            World world = getPlayer().getWorld();
                            ArenaData aData = arenaMan.get(world.getName());
                            if(arenaMan.isEditor(getName())){
                                Location pLoc = getPlayer().getLocation();
                                arenaMan.setDMCenter(aData.worldName, Tools.round(pLoc, 2));
                                Language.setVar("location", Tools.toString(Tools.round(pLoc, 2), false));
                                sendLanguage(LangKey.adminDMCenterSet);
                            } else {
                                Language.setVar("worldname", world.getName());
                                Language.setVar("arenaname", aData.name);
                                sendLanguage(LangKey.adminNotEditing);
                            }
                        } else {
                            sendLanguage(LangKey.noPermission);
                        }
                    } else {
                        sendLanguage(LangKey.notPlayer);
                    }
                } else if (is(args[0], "setlobbyspawn")) {
                    handled = true;
                    if(isPlayer()) {
                        if (Perms.has(getPlayer(), "survivalgames.admin.setspawn", getPlayer().isOp())) {
                            World world = getPlayer().getWorld();
                            ArenaData aData = arenaMan.getLobby();
                            if(arenaMan.isEditing(getName(), aData.worldName)) {
                                Location pLoc = getPlayer().getLocation();
                                arenaMan.setLobbySpawn(Tools.round(pLoc, 2));
                                Language.setVar("location", Tools.toString(Tools.round(pLoc, 2), false));
                                sendLanguage(LangKey.adminLobbySpawnSet);
                            } else {
                                Language.setVar("worldname", world.getName());
                                Language.setVar("arenaname", aData.name);
                                sendLanguage(LangKey.adminNotEditing);
                            }
                        } else {
                            sendLanguage(LangKey.noPermission);
                        }
                    } else {
                        sendLanguage(LangKey.notPlayer);
                    }
                } else if (is(args[0], "edit") || is(args[0], "modify")) {
                    handled = true;
                    if (!isPlayer() || Perms.has(getPlayer(), "survivalgames.admin.edit", getPlayer().isOp())) {
                        if(args.length == 1) {
                            String arenas = Language.getLanguage(LangKey.noLoadedArenas);
                            if(arenaMan.size() > 0) {
                                arenas = "";
                                for(String arena : arenaMan.keySet()) {
                                    ArenaData aData = arenaMan.get(arena);
                                    Language.setVar("arenaname", aData.name);
                                    Language.setVar("worldname", arena);
                                    Language.setVar("enabled", aData.enabled ? "enabled" : "disabled");
                                    arenas += Language.getLanguage(LangKey.adminEditListItem) + "\n";
                                }
                                if(arenas.length() > 0)
                                    arenas = arenas.substring(0, arenas.length() - 1);
                            }
                            Language.setVar("arenas", arenas);
                            sendLanguage(LangKey.adminEditList);
                        } else {
                            if(isPlayer()) {
                                String wName = Tools.join(1, args, " ");
                                ArenaData aData = arenaMan.get(wName);
                                if(aData != null) {
                                    if(!arenaMan.isEditor(getName())) {
                                        arenaMan.startEditting(getName(), aData.worldName);
                                        Language.setVar("worldname", aData.worldName);
                                        Language.setVar("arenaname", aData.name);
                                        sendLanguage(LangKey.adminEditBegun);
                                        arenaMan.disableArena(wName, true);
                                        World world = Bukkit.getWorld(wName);
                                        world.setTime(0);
                                        world.setStorm(false);
                                        world.setThundering(false);
                                        if(getPlayer().getWorld() != world)
                                            getPlayer().teleport(aData.spectatorSpawn);
                                        getPlayer().setGameMode(GameMode.CREATIVE);
                                    } else {
                                        Language.setVar("worldname", aData.worldName);
                                        Language.setVar("arenaname", aData.name);
                                        sendLanguage(LangKey.adminAlreadyEditing);
                                    }
                                } else {
                                    Language.setVar("worldname", wName);
                                    sendLanguage(LangKey.arenaNotFound);
                                }
                            } else {
                                sendLanguage(LangKey.notPlayer);
                            }
                        }
                    } else {
                        sendLanguage(LangKey.noPermission);
                    }
                } else if (is(args[0], "save") || is(args[0], "keep") 
                        || is(args[0], "done") || is(args[0], "finish")) {
                    handled = true;
                    if (isPlayer()) {
                        if(Perms.has(getPlayer(), "survivalgames.admin.edit", getPlayer().isOp())) {
                            if(args.length == 1) {
                                ArenaData aData = arenaMan.get(getPlayer().getWorld().getName());
                                // Check if the player is editting this map.
                                if(arenaMan.isEditing(getName(), aData.worldName)) {
                                    Language.setVar("worldname", aData.worldName);
                                    Language.setVar("arenaname", aData.name);
                                    sendLanguage(LangKey.adminSaving);
                                    // Stop editting for this player.
                                    arenaMan.finishEditting(getName());
                                } else {
                                    Language.setVar("worldname", aData.worldName);
                                    Language.setVar("arenaname", aData.name);
                                    sendLanguage(LangKey.adminNotEditing);
                                }
                            }
                        } else {
                            sendLanguage(LangKey.noPermission);
                        }
                    } else {
                        sendLanguage(LangKey.notPlayer);
                    }
                } else if (is(args[0], "cancel") || is(args[0], "discard")) {
                    handled = true;
                    if (isPlayer()) {
                        if(Perms.has(getPlayer(), "survivalgames.admin.edit", getPlayer().isOp())) {
                            if(args.length == 1) {
                                ArenaData aData = arenaMan.get(getPlayer().getWorld().getName());
                                // Check if the player is editting this map.
                                if(arenaMan.isEditing(getName(), aData.worldName)) {
                                    Language.setVar("worldname", aData.worldName);
                                    Language.setVar("arenaname", aData.name);
                                    sendLanguage(LangKey.adminCancelling);
                                    // Stop editting for this player.
                                    arenaMan.finishEditting(getName());
                                } else {
                                    Language.setVar("worldname", aData.worldName);
                                    Language.setVar("arenaname", aData.name);
                                    sendLanguage(LangKey.adminNotEditing);
                                }
                            }
                        } else {
                            sendLanguage(LangKey.noPermission);
                        }
                    } else {
                        sendLanguage(LangKey.notPlayer);
                    }
                } else if (is(args[0], "world") || is(args[0], "worlds") 
                        || is(args[0], "tpworld") || is(args[0], "changeworld") 
                        || is(args[0], "switchworld")) {
                    handled = true;
                    if (isPlayer()) {
                        if (Perms.has(getPlayer(), "survivalgames.admin.tpworld", getPlayer().isOp())) {
                            if(args.length == 1) {
                                String arenas = Language.getLanguage(LangKey.noLoadedArenas);
                                if(arenaMan.size() > 0) {
                                    arenas = "";
                                    for(String arena : arenaMan.keySet()) {
                                        ArenaData aData = arenaMan.get(arena);
                                        Language.setVar("arenaname", aData.name);
                                        Language.setVar("worldname", arena);
                                        Language.setVar("enabled", aData.enabled ? "enabled" : "disabled");
                                        arenas += Language.getLanguage(LangKey.adminEditListItem) + "\n";
                                    }
                                    if(arenas.length() > 0)
                                        arenas = arenas.substring(0, arenas.length() - 1);
                                }
                                Language.setVar("arenas", arenas);
                                sendLanguage(LangKey.adminEditList);
                            } else if(args.length == 2) {
                                if(isPlayer()) {
                                    String wName = Tools.join(1, args, " ");
                                    ArenaData aData = arenaMan.get(wName);
                                    if(aData != null) {
                                        if(aData.spectatorSpawn != null)
                                            getPlayer().teleport(aData.spectatorSpawn);
                                        else
                                            getPlayer().teleport(plugin.getServer().getWorld(wName).getSpawnLocation());
                                    } else {
                                        Language.setVar("worldname", wName);
                                        sendLanguage(LangKey.arenaNotFound);
                                    }
                                } else {
                                    sendLanguage(LangKey.notPlayer);
                                }
                            }
                        } else {
                            sendLanguage(LangKey.noPermission);
                        }
                    } else {
                        sendLanguage(LangKey.notPlayer);
                    }
                } else if (is(args[0], "time") || is(args[0], "timeleft")) {
                    handled = true;
                    if (!isPlayer() || Perms.has(getPlayer(), "survivalgames.basic.timeleft", true)) {
                        if (gameMan.getState() == SGGameState.LOBBY) {
                            Language.setVar("time", ((int)((gameMan.nextGame - System.currentTimeMillis()) / 1000)) + "");
                            sendLanguage(LangKey.timeLeftLobby);
                        } else if (gameMan.getState() == SGGameState.STARTING) {
                            Language.setVar("time", ((int)((gameMan.nextGameStart - System.currentTimeMillis()) / 1000)) + "");
                            sendLanguage(LangKey.timeLeftStarting);
                        } else if (gameMan.getState() == SGGameState.GAME) {
                            Language.setVar("time", ((int)((gameMan.nextDeathmatch - System.currentTimeMillis()) / 1000)) + "");
                            sendLanguage(LangKey.timeLeftGame);
                        } else if (gameMan.getState() == SGGameState.PRE_DEATHMATCH) {
                            Language.setVar("time", ((int)((gameMan.nextDeathmatch - System.currentTimeMillis()) / 1000)) + "");
                            sendLanguage(LangKey.timeLeftPreDM);
                        } else if (gameMan.getState() == SGGameState.DEATHMATCH) {
                            Language.setVar("time", ((int)((gameMan.nextEndGame - System.currentTimeMillis()) / 1000)) + "");
                            sendLanguage(LangKey.timeLeftDm);
                        }
                    } else {
                        sendLanguage(LangKey.noPermission);
                    }
                } else if (is(args[0], "list")) {
                    handled = true;
                    if (!isPlayer() || Perms.has(getPlayer(), "survivalgames.basic.list", true)) {
                        String tributes = "";
                        for (String tributeName : gameMan.getTributeNames()) {
                            Player tribute = plugin.getServer().getPlayer(tributeName);
                            if(tribute != null)
                                tributeName = tribute.getDisplayName();
                            tributes += tributeName + ChatColor.WHITE + ", ";
                        }
                        if(!Tools.isNullEmptyWhite(tributes))
                            tributes = tributes.substring(0, tributes.length() - 2);
                        String specs = "";
                        for (String specName : gameMan.getSpectatorNames()) {
                            Player spec = plugin.getServer().getPlayer(specName);
                            if(spec != null)
                                specName = spec.getDisplayName();
                            specs += specName + ChatColor.WHITE + ", ";
                        }
                        if(!Tools.isNullEmptyWhite(specs))
                            specs = specs.substring(0, specs.length() - 2);
                        if (gameMan.getState() != SGGameState.LOBBY) {
                            Language.setVar("tributes", tributes);
                            Language.setVar("amount", gameMan.getTributeNames().size() + "");
                            sendLanguage(LangKey.listTributes);
                            Language.setVar("spectators", specs);
                            Language.setVar("amount", gameMan.getSpectatorNames().size() + "");
                            sendLanguage(LangKey.listSpectators);
                        } else {
                            Language.setVar("waiting", tributes);
                            Language.setVar("amount", gameMan.getTributeNames().size() + "");
                            sendLanguage(LangKey.listWaiting);
                        }
                    } else {
                        sendLanguage(LangKey.noPermission);
                    }
                } else if (is(args[0], "stats")) {
                    handled = true;
                    if (args.length == 1 && isPlayer()) {
                        PlayerStats pStats = plugin.getStatManager().getPlayer(getName());
                        Language.setTarget(getPlayer());
                        Language.setVar("kills", pStats.getKills() + "");
                        Language.setVar("killstreak", pStats.getKillStreak() + "");
                        Language.setVar("bestkillstreak", pStats.getBestKillStreak() + "");
                        Language.setVar("deaths", pStats.getDeaths() + "");
                        Language.setVar("deathstreak", pStats.getDeathStreak() + "");
                        Language.setVar("worstdeathstreak", pStats.getWorstDeathStreak() + "");
                        Language.setVar("wins", pStats.getWins() + "");
                        Language.setVar("winstreak", pStats.getWinStreak() + "");
                        Language.setVar("bestwinstreak", pStats.getBestWinStreak() + "");
                        Language.setVar("ties", pStats.getTies() + "");
                        Language.setVar("losses", pStats.getLosses() + "");
                        Language.setVar("losestreak", pStats.getLoseStreak() + "");
                        Language.setVar("worstlosestreak", pStats.getWorstLoseStreak() + "");
                        Language.setVar("gamesplayed", pStats.getGamesPlayed() + "");
                        Language.setVar("points", pStats.getPoints() + "");
                        Language.setVar("secondsplayed", ((int)(pStats.getTimePlayed() / 1000)) + "");
                        Language.setVar("timeplayed", Tools.getTime(pStats.getTimePlayed()));
                        Language.setVar("fulltimeplayed", Tools.getFullTime(pStats.getTimePlayed()));
                        Language.setVar("secslastplayed", "Now");
                        Language.setVar("lastplayed", "Now");
                        Language.setVar("fulllastplayed", "Now");
                        Language.setVar("animalskilled", pStats.getAnimalsKilled() + "");
                        Language.setVar("mobskilled", pStats.getMobsKilled() + "");
                        Language.setVar("looted", pStats.getContainersLooted() + "");
                        sendLanguage(LangKey.playerStats);
                    } else {
                        String tName = args[1];
                        if(plugin.getStatManager().getPlayer(tName) != null) {
                            PlayerStats pStats = plugin.getStatManager().getPlayer(tName);
                            Player target = plugin.getServer().getPlayer(tName);
                            if(target != null)
                                Language.setTarget(target);
                            else {
                                Language.setVar("tdisplay", tName);
                                Language.setVar("target", tName);
                            }
                            Language.setVar("kills", pStats.getKills() + "");
                            Language.setVar("killstreak", pStats.getKillStreak() + "");
                            Language.setVar("bestkillstreak", pStats.getKillStreak() + "");
                            Language.setVar("deaths", pStats.getDeaths() + "");
                            Language.setVar("deathstreak", pStats.getDeathStreak() + "");
                            Language.setVar("worstdeathstreak", pStats.getWorstDeathStreak() + "");
                            Language.setVar("wins", pStats.getWins() + "");
                            Language.setVar("winstreak", pStats.getWinStreak() + "");
                            Language.setVar("bestwinstreak", pStats.getBestWinStreak() + "");
                            Language.setVar("ties", pStats.getTies() + "");
                            Language.setVar("losses", pStats.getLosses() + "");
                            Language.setVar("losestreak", pStats.getLoseStreak() + "");
                            Language.setVar("worstlosestreak", pStats.getWorstLoseStreak() + "");
                            Language.setVar("gamesplayed", pStats.getGamesPlayed() + "");
                            Language.setVar("points", pStats.getPoints() + "");
                            Language.setVar("secondsplayed", ((int)(pStats.getTimePlayed() / 1000)) + "");
                            Language.setVar("timeplayed", Tools.getTime(pStats.getTimePlayed()));
                            Language.setVar("fulltimeplayed", Tools.getFullTime(pStats.getTimePlayed()));
                            Language.setVar("secslastplayed", (target == null ? ((int) ((System.currentTimeMillis() - pStats.getLastPlayed()) / 1000)) + "" : "Now"));
                            Language.setVar("lastplayed", (target == null ? Tools.getTime(System.currentTimeMillis() - pStats.getLastPlayed()) : "Now"));
                            Language.setVar("fulllastplayed", (target == null ? Tools.getFullTime(System.currentTimeMillis() - pStats.getLastPlayed()) : "Now"));
                            Language.setVar("animalskilled", pStats.getAnimalsKilled() + "");
                            Language.setVar("mobskilled", pStats.getMobsKilled() + "");
                            Language.setVar("looted", pStats.getContainersLooted() + "");
                            sendLanguage(LangKey.playerStats);
                        } else {
                            Language.setVar("notfound", tName);
                            sendLanguage(LangKey.playerNotFound);
                        }
                    }
                } else if (is(args[0], "vote")) {
                    handled = true;
                    VoteManager voteMan = arenaMan.getVoteManager();
                    if (!isPlayer() || Perms.has(getPlayer(), "survivalgames.basic.vote", true)) {
                        // In the lobby?
                        if (gameMan.getState() == SGGameState.LOBBY) {
                                if(arenaMan.arenaOrder.size() > 0) {
                                    if (args.length == 1) {
                                        String arenaVotes = Language.getLanguage(LangKey.noLoadedArenas);
                                            arenaVotes = "";
                                            for (int i = 0; i < arenaMan.arenaOrder.size(); i++) {
                                                int votes = voteMan.get(arenaMan.arenaOrder.get(i)) != null
                                                        ? voteMan.totalVotes(arenaMan.arenaOrder.get(i)) : 0;
                                                Language.setVar("arenanum", (i + 1) + "");
                                                Language.setVar("arenaname", arenaMan.get(arenaMan.arenaOrder.get(i)).name);
                                                Language.setVar("arenavotes", votes + "");
                                                arenaVotes += Language.getLanguage(LangKey.arenaVotes) + "\n";
                                            }
                                        if (arenaVotes.length() > 0)
                                            arenaVotes = arenaVotes.substring(0, arenaVotes.length() - 1);
                                        Language.setVar("arenavotes", arenaVotes);
                                        Language.setVar("nextarena", arenaMan.getNextArena().name);
                                        sendLanguage(LangKey.voteInfo);
                                    } else if (isPlayer()) {
                                        if(!gameMan.isAdmin(getPlayer())) {
                                            boolean isInt = true;
                                            int arena = 0;

                                            // Parse the string to an integer.
                                            try {
                                                arena = Integer.parseInt(args[1]);
                                            } catch (NumberFormatException nfe) {
                                                isInt = false; // Could not parse. Was not
                                                // integer.
                                            }

                                            // Check whether a correct arena number was entered.
                                            if (isInt && arena > 0
                                                    && arena <= arenaMan.arenaOrder.size()) {

                                                // Only allow arenas that were not just played.
                                                if (arenaMan.getLastArena() == null 
                                                        || !arenaMan.arenaOrder.get(arena - 1).equals(arenaMan.getLastArena().worldName) 
                                                        || arenaMan.arenaOrder.size() == 1) {

                                                    // Check if the player has already voted for this arena.
                                                    if (!voteMan.hasVotedFor(arenaMan.arenaOrder.get(arena - 1), getName())) {

                                                        // Remove the players vote from other arenas.
                                                        // If not then add the player to the arena votes.
                                                        int worth = 1;
                                                        for(int i = 10; i >= 1; i--) {
                                                        	if(Perms.has(getPlayer(), "survivalgames.basic.vote." + i, false)) {
                                                                worth = i;
                                                                break;
                                                            }
                                                        }
                                                        voteMan.vote(arenaMan.arenaOrder.get(arena - 1), getName(), worth);

                                                        // Switch the world to the most voted one.
                                                        arenaMan.setNextArena(arenaMan.get(voteMan.getMostVoted()).worldName);
                                                        // Broadcast message to all players in the lobby world.
                                                        Language.setTarget(getPlayer());
                                                        Language.setVar("arenanum", arena + "");
                                                        Language.setVar("arena", arenaMan.get(arenaMan.arenaOrder.get(arena - 1)).name);
                                                        Language.broadcastAndBlockLanguage(LangKey.votedForArena, 10 * 1000, true);
                                                    } else {
                                                        Language.setVar("arena", arenaMan.get(arenaMan.arenaOrder.get(arena - 1)).name);
                                                        sendLanguage(LangKey.alreadyVoted);
                                                    }
                                                } else {
                                                    Language.setVar("arena", arenaMan.get(arenaMan.arenaOrder.get(arena - 1)).name);
                                                    sendLanguage(LangKey.arenaJustPlayed);
                                                }
                                            } else {
                                                Language.setVar("min", 1 + "");
                                                Language.setVar("max", arenaMan.arenaOrder.size() + "");
                                                sendLanguage(LangKey.outOfRange);
                                            }
                                        } else {
                                            sendLanguage(LangKey.adminJoinFirst);
                                        }
                                    } else {
                                        sendLanguage(LangKey.notPlayer);
                                    }
                                } else {
                                    sendLanguage(LangKey.noLoadedArenas);
                                }
                        } else {
                            sendLanguage(LangKey.voteInLobby);
                        }
                    } else {
                        sendLanguage(LangKey.noPermission);
                    }
                } else if (is(args[0], "top") || is(args[0], "leaderboard") 
                        || is(args[0], "leaderboards")) {
                    handled = true;
                    if(!isPlayer() || Perms.has(getPlayer(), "survivalgames.basic.top", getPlayer().isOp())) {
                        int page = 1;
                        if(args.length == 2 && Tools.isInt(args[1])) {
                            page = Integer.parseInt(args[1]);
                        }
                        String query = "SELECT player, points, wins, kills FROM players ORDER BY points DESC, wins DESC, kills DESC";
                        DataManager dbm = DataAccess.getManager();
                        String topRanks = "No ranks yet.";
                        int cPage = 0;
                        int count = 0;
                        try {
                            ResultSet rs = dbm.query(query);
                            while(rs.next()) {
                                if(count % 5 == 0){
                                    cPage++;
                                    if(cPage > page) {
                                        cPage = page;
                                        break;
                                    } else
                                        topRanks = "";
                                }
                                String name = rs.getString("player");
                                int points = rs.getInt("points");
                                int wins = rs.getInt("wins");
                                int kills = rs.getInt("kills");
                                Language.setVar("rank", (count + 1) + "");
                                Language.setVar("target", name);
                                Language.setVar("points", points + "");
                                Language.setVar("wins", wins + "");
                                Language.setVar("kills", kills + "");
                                topRanks += Language.getLanguage(LangKey.topItem) + "\n";
                                count++;
                            }
                        } catch(SQLException se) {
                            Logger.error("Top rank: " + se.getMessage());
                            se.printStackTrace();
                        }
                        if(count > 0)
                            topRanks = topRanks.substring(0, topRanks.length() - 1);
                        Language.setVar("page", cPage + "");
                        Language.setVar("topranks", topRanks);
                        sendLanguage(LangKey.topList);
                    } else {
                        sendLanguage(LangKey.noPermission);
                    }
                } else if (is(args[0], "bounty") || is(args[0], "setbounty")
                         || is(args[0], "givebounty")  || is(args[0], "addbounty")) {
                    handled = true;
                    if(!isPlayer() || Perms.has(getPlayer(), "survivalgames.basic.bounty", getPlayer().isOp())) {
                        if(args.length == 3) {
                            String targetName = args[1];
                            Player target = plugin.getServer().getPlayer(targetName);
                            if(target != null) {
                                if(Tools.isInt(args[2])) {
                                    int bounty = Integer.parseInt(args[2]);
                                    StatManager statMan = plugin.getStatManager();
                                    PlayerStats pStats = statMan.getPlayer(getName());
                                    PlayerStats tStats = statMan.getPlayer(target.getName());
                                    if(!isPlayer() || gameMan.isTribute(getPlayer()) || gameMan.isSpectator(getPlayer())) {
                                        if(gameMan.isTribute(target)) {
                                            if(bounty > 0) {
                                                if(bounty <= pStats.getPoints()) {
                                                    statMan.setBounty(target.getName(), statMan.getBounty(target.getName()) + bounty);
                                                    pStats.setPoints(pStats.getPoints() - bounty, true);
                                                    Language.setVar("amount", bounty + "");
                                                    Language.setVar("bounty", statMan.getBounty(target.getName()) + "");
                                                    Language.setTarget(target);
                                                    Language.broadcastLanguage(LangKey.bountySet);
                                                } else {
                                                    // Cannot afford.
                                                    Language.setVar("points", pStats.getPoints() + "");
                                                    Language.setVar("bounty", bounty + "");
                                                    Language.setTarget(target);
                                                    sendLanguage(LangKey.bountyCantAfford);
                                                }
                                            } else {
                                                Language.setVar("min", "0");
                                                Language.setVar("max", pStats.getPoints() + "");
                                                sendLanguage(LangKey.outOfRange);
                                            }
                                        } else {
                                            Language.setTarget(target);
                                            sendLanguage(LangKey.notTribute);
                                        }
                                    } else {
                                        sendLanguage(LangKey.youNotIngame);
                                    }
                                } else {
                                    Language.setVar("number", args[2]);
                                    sendLanguage(LangKey.notNumber);
                                }
                            } else {
                                sendLanguage(LangKey.playerNotFound);
                            }
                        }
                    } else {
                        sendLanguage(LangKey.noPermission);
                    }
                }

                if (!handled) {
                    handled = true;
                    if (isPlayer()) {
                        Language.setVar("command", "/" + label + " " + Tools.join(args, " "));
                    } else {
                        Language.setVar("command", label + " " + Tools.join(args, " "));
                    }
                    if (isPlayer()) {
                        debug.everything(getName() + " tried entering an incorrect command: /" + label + " " + Tools.join(args, " "));
                    }
                    sendLanguage(LangKey.unknownCommand);
                }
            }
        } else {
            handled = true;
        }
        return handled;
    }

    // Simplifies and shortens the if statements for commands.
    private boolean is(String entered, String label) {
        return entered.equalsIgnoreCase(label);
    }

    // Checks if the current user is actually a player.
    private boolean isPlayer() {
        return cSender != null && cSender instanceof Player;
    }

    // Checks if the current user is actually a player and sends a message to that player.
    private boolean sendMessage(String message) {
        boolean sent = false;
        if (isPlayer() && sendMessage(getPlayer(), message)) {
            sent = true;
        } else {
            if (message != null && !message.equals("")) {
                Logger.info(message);
                sent = true;
            }
        }
        return sent;
    }

    // Sends a message to a player.
    private boolean sendMessage(Player player, String message) {
        boolean sent = false;
        if (message != null && !message.equals("")) {
            player.sendMessage(message);
            sent = true;
        }
        return sent;
    }

    // Checks if the current user is actually a player and sends a language to that player.
    private boolean sendLanguage(LangKey key) {
        boolean sent = false;
        if (isPlayer() && sendLanguage(getPlayer(), key)) {
            sent = true;
        } else {
            String message = Language.getLanguage(key);
            if (message != null && !message.equals("")) {
                if(message.contains("\n")) {
                    for(String split : message.split("\\n")) {
                        Logger.info(split);
                    }
                } else {
                    Logger.info(message);
                }
                sent = true;
            }
        }
        return sent;
    }

    // Sends a language to a player.
    private boolean sendLanguage(Player player, LangKey key) {
        boolean sent = false;
        Language.setUser(player);
        String message = Language.getLanguage(key, false);
        if (message != null && !message.equals("")) {
            Language.sendLanguage(player, key);
            sent = true;
        }
        return sent;
    }

    // Checks if the current user is actually a player and returns the name of that player.
    private String getName() {
        String name = "Console";
        if (isPlayer()) {
            name = getPlayer().getName();
        }
        return name;
    }

    // Checks if the current user is actually a player and returns the name of that player.
    private String getDisplayName() {
        String name = "Console";
        if (isPlayer()) {
            name = getPlayer().getDisplayName();
        }
        return name;
    }

    // Gets the player if the current user is actually a player.
    private Player getPlayer() {
        Player player = null;
        if (isPlayer()) {
            player = (Player) cSender;
        } else {
            debug.normal("Tried to get player from console.");
        }
        return player;
    }

    private String colorizeText(String text, ChatColor color) {
        return color + text + ChatColor.WHITE;
    }
}
