package net.shockverse.survivalgames.core;

import java.io.File;
import java.util.*;
import net.shockverse.survivalgames.SurvivalGames;
import net.shockverse.survivalgames.exceptions.PropertyException;
import net.shockverse.survivalgames.extras.PropertyFile;
import net.shockverse.survivalgames.extras.PropertyList;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * @description Handles the language used in the plugin
 * @author Duker02, LegitModern, Tagette
 */
public class Language {
    
    public enum LangKey {
        // Login
        joinSpec,
        joinTribute,
        joinAdmin,
        joinKickOther,
        joinKick,
        joinKickFull,
        joinKickSoft,
        joinKickSoftFull,
        joinBanned,
        joinFull,
        joinServerDisabled,
        
        // Lobby
        lobbyCountdown,
        nextArena,
        
        // Voting
        arenaVotes,
        votedForArena,
        alreadyVoted,
        arenaJustPlayed,
        voteInLobby,
        voteInfo,
        
        // Scoreboard
        scoreLobbyTitle,
        scoreLobbyTime,
        scoreVoteArena,
        scoreVoteMost,
        scoreGameTitle,
        scoreGameTime,
        scoreGameTributes,
        scoreGameSpectators,
        
        // Game
        gameCountdown,
        gameHasBegun,
        graceBegin,
        graceEnd,
        chestsRefilled,
        timeLimitOver,
        
        // Player Death
        tributeRevived,
        tributeFallen,
        tributesRemaining,
        youRevived,
        youKilled,
        youDied,
        nowSpec,
        cannon,
        
        // Death Match
        minTributesRemain,
        dmTeleport,
        dmCountdown,
        dmStart,
        
        // End Game
        tributeWon,
        gameOver,
        gameRestarting,
        
        // Commands
        
        // Tribute
        listTributes,
        listSpectators,
        listWaiting,
        youWereAdded,
        youChangeRank,
        youNotTribute,
        youNotSpectator,
        youNotIngame,
        playerStats,
        timeLeftLobby,
        timeLeftStarting,
        timeLeftPreDM,
        timeLeftGame,
        timeLeftDm,
        topList,
        topItem,
        bountyTitle,
        bountySet,
        bountyCantAfford,
        bountyGained,
        wonPoints,
        lostPoints,
        gameLimit,
        
        // Spectators
        changeSpectate,
        
        // Admin
        adminJoinFirst,
        adminGameStarted,
        adminAlreadyStarted,
        adminDeathmatchStart,
        adminGameStopped,
        adminGameNotStarted,
        adminRefillChests,
        adminChangeRank,
        adminPlayerAdded,
        adminAddedSelf,
        adminPlayerRemoved,
        adminRemovedSelf,
        adminViewInv,
        adminEditBegun,
        adminSaving,
        adminSave,
        adminCancelling,
        adminCancel,
        adminNotEditing,
        adminAlreadyEditing,
        adminSpecSpawnSet,
        adminNotSpawn,
        adminNoSpawns,
        adminTpSpawn,
        adminSpawnSet,
        adminSpawnRemove,
        adminSpawnList,
        adminSpawnListItem,
        adminDMCenterSet,
        adminDMSpecSpawnSet,
        adminNotDMSpawn,
        adminNoDMSpawns,
        adminTpDMSpawn,
        adminDMSpawnSet,
        adminDMSpawnRemove,
        adminDMSpawnList,
        adminDMSpawnListItem,
        adminLobbySpawnSet,
        adminEditList,
        adminEditListItem,
        
        // World
        arenaLoaded,
        arenaUnloaded,
        arenaAlreadyExists,
        arenaAdding,
        arenaAdded,
        arenaNotAdded,
        arenaRemoved,
        arenaAlreadyEnabled,
        arenaAlreadyDisabled,
        arenaNoSpawns,
        
        // Global
        serverShutdown,
        crateName,
        pluginPrefix,
        pluginSuffix,
        helpTitle,
        helpItem,
        noHelp,
        stateMessageGame,
        stateMessageLobby,
        stateMessageLobbyWaiting,
        
        // Config
        
        configSet,
        configRemoved,
        configNotFile,
        configNotKey,
        configGetList,
        configGet,
        
        // Errors
        
        noLoadedArenas,
        arenaNotFound,
        outOfRange,
        notNumber,
        playerNotFound,
        playerInGame,
        playerIsGM,
        noSpecInv,
        noLobbyInv,
        notInLobby,
        noPermission,
        notPlayer,
        notTribute,
        adminNoTributes,
        unknownCommand,

        // Items
        teleportSuccessful
    }

    private static final String settingsFile = "Language";
    private static EnumMap<LangKey, String> languages;
    private static SurvivalGames plugin;
    // Text Variables
    private static HashMap<String, String> vars;
    
    public static PropertyFile languageFile;

    public static void initialize(SurvivalGames instance) {
        Language.plugin = instance;
        languages = new EnumMap<LangKey, String>(LangKey.class);
        vars = new HashMap<String, String>();
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        try {
            File configFile = new File(plugin.getDataFolder(), settingsFile + ".cfg");
            languageFile = new PropertyFile(settingsFile, configFile);
            languageFile.loadProperties();
            setup(languageFile.getProperties());
            languageFile.saveProperties();
            resetVariables();
            if (!plugin.getSettings().LowDetailMode) {
                Logger.info("Language loaded.");
            }
        } catch (PropertyException pe) {
            Logger.error(pe.getMessage());
        } catch(Exception ex) {
            Logger.error("Could not load " + settingsFile + ".cfg file.");
            ex.printStackTrace();
        }
    }
    
    public static void disable(){
        languages.clear();
        languages = null;
        vars.clear();
        vars = null;
    }

    public static void setup(PropertyList props) {
        /* %user% = The player that used the command.
         * %target% = The player that was targeted using the command.
         * %vars% = Displays available variables.
         */
        LangKey key;
        String value = "";
        String comment = "";
        // Declare settings here. Note that if config is not found these values will be placed into a new config file.

        props.comment("-- Languages --");
        props.newLine();
        
        props.comment("- Login -");

        key = LangKey.joinSpec;
        value = "You have joined as a spectator.";
        comment = "Displays when you join the game as a spectator.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.joinTribute;
        value = "You have been chosen to be a tribute.";
        comment = "Displays when you join as a tribute.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.joinAdmin;
        value = "You have joined as an admin. Type '/sg join' to play.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.joinKickOther;
        value = "You have taken place of another player.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.joinKick;
        value = "&redYou have been kicked to make room for a VIP user since you were the last to join.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.joinKickFull;
        value = "&redThe server is full of VIP users.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.joinKickSoft;
        value = "&redYou are now a spectator to make room for a VIP user since you were the last to login.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.joinKickSoftFull;
        value = "&redThe game is full of VIP tributes.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.joinBanned;
        value = "&redYou are banned on the Survival Games servers.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.joinFull;
        value = "&redThe server is full, please check again later.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.joinServerDisabled;
        value = "&redThis server is currently disabled.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        props.newLine();
        props.comment("- Lobby -");
        
        key = LangKey.lobbyCountdown;
        value = "%time% until the games begin.";
        comment = "The countdown messages before the game starts.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.nextArena;
        value = "Up next: %arena%";
        comment = "Displays the next map to be played.";
        addLanguage(props, key, value, comment);
        
        props.newLine();
        props.comment("- Voting -");
        
        key = LangKey.votedForArena;
        value = "%tdisplay% has voted for %arena%! [/sg vote %arenanum%]";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.alreadyVoted;
        value = "&redYou have already chosen %arena%.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.arenaJustPlayed;
        value = "&redThe %arena% arena was just played. Please choose another.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.voteInLobby;
        value = "&redYou can only vote in the lobby.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.arenaVotes;
        value = "/sg vote %arenanum% >> %arenaname% [ %arenavotes% vote(s) ]";
        comment = "Displays the name and votes for an arena.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.voteInfo;
        value   = "-- Voting --\n"
                + "Next map: %nextarena%\n"
                + "%arenavotes%\n"
                + "-- ------ --";
        comment = "Displays the vote info for each arena. /sg vote";
        addLanguage(props, key, value, comment);
        
        props.newLine();
        props.comment("- Scoreboard -");
        
        key = LangKey.scoreLobbyTitle;
        value   = "The Lobby";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.scoreLobbyTime;
        value   = "Time left";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.scoreVoteArena;
        value   = "%arenaname%";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.scoreVoteMost;
        value   = "Up next";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.scoreGameTitle;
        value   = "The Survival Games";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.scoreGameTime;
        value   = "Time left";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.scoreGameTributes;
        value   = "Tributes";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.scoreGameSpectators;
        value   = "Watching";
        comment = "";
        addLanguage(props, key, value, comment);
        
        props.newLine();
        props.comment("- Game -");
        
        key = LangKey.gameCountdown;
        value = "%time% until the games begin.";
        comment = "Displayed when the game is counting down.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.gameHasBegun;
        value = "The games have begun!";
        comment = "Displayed when the game has begun.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.graceBegin;
        value = "You have a %time%s grace period!";
        comment = "Displayed when grace period begins.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.graceEnd;
        value = "Grace period is over!";
        comment = "Displayed when grace period is over.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.chestsRefilled;
        value = "The sponsors have refilled the chests!";
        comment = "Displayed when chests are refilled.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.timeLimitOver;
        value = "Time limit has been reached!";
        comment = "Displayed at the end of the survival round.";
        addLanguage(props, key, value, comment);
        
        props.newLine();
        props.comment("- Player -");
        
        key = LangKey.tributeRevived;
        value = "Tribute - %tdisplay% has been revived!";
        comment = "Message broadcasted when a tribute gets revived.";
        
        key = LangKey.tributeFallen;
        value = "Tribute - %tdisplay% has fallen.";
        comment = "Message broadcasted when a tribute dies.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.tributesRemaining;
        value = "%amount% tribute(s) remain.";
        comment = "Message displayed to dying player.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.youRevived;
        value = "You were revived!";
        comment = "Message displayed to revived player.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.youKilled;
        value = "You eliminated %tdisplay%!";
        comment = "Message displayed When a player kills another.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.youDied;
        value = "You were eliminated from the games by %tdisplay%!";
        comment = "Message displayed to dying player.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.nowSpec;
        value = "You are now a spectator. Spectators are invisible and can not be heard by tributes.\n"
               // + "To spectate tributes, swing your arm to teleport to the next tribute.";
                 + "To spectate tributes, use the compass in your inventory and click on whom you would like to watch.";
        comment = "Message displayed when a player becomes a spectator.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.cannon;
        value = "A cannon could be heard in the distance.";
        comment = "Message displayed when a cannon is fired.";
        addLanguage(props, key, value, comment);
        
        props.newLine();
        props.comment("- Deathmatch -");
        
        key = LangKey.minTributesRemain;
        value = "%amount% or less tributes remain!";
        comment = "Displayed when the amount of tributes remaining are less then the minimum.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.dmTeleport;
        value = "You are running too far away! Please return to the deathmatch area!";
        comment = "Displayed when a tribute runs too far away from the deathmatch center.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.dmCountdown;
        value = "Deathmatch will begin in %time%!";
        comment = "Message displayed for deathmatch countdown.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.dmStart;
        value = "We will now begin the final deathmatch!";
        comment = "Message displayed when deathmatch starts.";
        addLanguage(props, key, value, comment);
        
        props.newLine();
        props.comment("- End Game -");
        
        key = LangKey.tributeWon;
        value = "Tribute - %tdisplay% has won the Survival Games!";
        comment = "Displays who won the game.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.gameOver;
        value = "The Survival Games are over and will restart soon...";
        comment = "Message displayed on game over.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.gameRestarting;
        value = "The Survival Games are restarting...";
        comment = "Message displayed on game restart.";
        addLanguage(props, key, value, comment);
        
        props.newLine();
        props.comment("-- Commands --");
        
        props.newLine();
        props.comment("- Tribute -");
        
        key = LangKey.listTributes;
        value = "Tributes: (%amount%) %tributes%";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.listSpectators;
        value = "Spectators: (%amount%) %spectators%";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.listWaiting;
        value = "Players Waiting: (%amount%) %waiting%";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.youWereAdded;
        value = "You were added to the game.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.youChangeRank;
        value = "You are now a %rank%.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.youNotTribute;
        value = "&redYou are not a tribute.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.youNotSpectator;
        value = "&redYou are not a spectator.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.youNotIngame;
        value = "&redYou are not part of the game.";
        comment = "Message sent if player is neither tribute nor spectator.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.playerStats;
        value   = "-- %tdisplay%'s Stats (total/streak/high) --\n"
                + "Kills: %kills% / %killstreak% / %bestkillstreak%\n"
                + "Deaths: %deaths% / %deathstreak% / %worstdeathstreak%\n"
                + "Animals Killed: %animalskilled%\n"
                + "Mobs Killed: %mobskilled%\n"
                + "Wins: %wins% / %winstreak% / %bestwinstreak%\n"
                + "Ties: %ties%\n"
                + "Losses: %losses% / %losestreak% / %worstlosestreak%\n"
                + "Games Played: %gamesplayed%\n"
                + "Time Played: %timeplayed%\n"
                + "Last Played: %lastplayed%\n"
                + "Chests Looted: %looted%\n"
                + "Points: %points%";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.bountyTitle;
        value = "&dark_greenBounty";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.timeLeftLobby;
        value = "Time remaining: %time%s";
        comment = "Displays the time left before a game starts.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.timeLeftStarting;
        value = "Starting in %time%.";
        comment = "Displays the time left before a game starts.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.timeLeftPreDM;
        value = "Deathmatch starts in %time%.";
        comment = "Displays the time left before a deathmatch starts.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.timeLeftGame;
        value = "%time% until deathmatch.";
        comment = "Displays the time left before deathmatch begins.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.timeLeftDm;
        value = "Time remaining: %time%s";
        comment = "Displays the time left before a game ends.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.topList;
        value   = "-- Top 5 Rank (Page %page%) --\n"
                + "%topranks%";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.topItem;
        value = "%rank%] %target% > %points% points / %wins% wins / %kills% kills";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.bountySet;
        value = "%tdisplay%'s has a bounty of %bounty%! Kill him/her for that many points!";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.bountyCantAfford;
        value = "You cannot afford to put %bounty% bounty on %tdisplay%. You have %points% points.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.bountyGained;
        value = "You have gained bounty. If you win the game you will win %bounty% points!";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.wonPoints;
        value = "You have won %points% points!";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.lostPoints;
        value = "&redYou have lost %points% points.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.gameLimit;
        value = "You have been kicked to make room for new players.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        props.newLine();
        props.comment("- Spectator -");
        
        key = LangKey.changeSpectate;
        value = "Now spectating: %tdisplay%";
        comment = "Message displayed when spectating new player.";
        addLanguage(props, key, value, comment);
        
        props.newLine();
        props.comment("- Admin -");
        
        key = LangKey.adminJoinFirst;
        value = "You must join the games first! /sg join";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminGameStarted;
        value = "You started the survival games in %arena%!";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminNoTributes;
        value = "&redThere are no tributes to play.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminDeathmatchStart;
        value = "You started the deathmatch.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminAlreadyStarted;
        value = "&redThe game has already started.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminGameStopped;
        value = "You stopped the survival games.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminGameNotStarted;
        value = "&redThe game has not been started yet.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminRefillChests;
        value = "You have refilled the chests.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminChangeRank;
        value = "You changed %tdisplay% to a %rank%";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminViewInv;
        value = "You are viewing %tdisplay%'s inventory.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminPlayerAdded;
        value = "You have added %tdisplay% to the game.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminAddedSelf;
        value = "You have added yourself to the game.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminPlayerRemoved;
        value = "You have removed %tdisplay% from the game.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminRemovedSelf;
        value = "You have removed yourself from the game.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminEditBegun;
        value   = "You are now editing '%arenaname%'.\n"
                + "/sg save - Saves your changes.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminSaving;
        value = "Edits to '%arenaname%' are being saved...";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminSave;
        value = "Edits to '%arenaname%' have been saved.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminCancelling;
        value = "Edits to '%arenaname%' are being discarded...";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminCancel;
        value = "Edits to '%arenaname%' have been discarded.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminNotEditing;
        value = "You are not editing this arena. /sg edit";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminAlreadyEditing;
        value = "You are already editing this arena. /sg save";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminSpecSpawnSet;
        value = "Spectator spawn has been set to %location%.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminNotSpawn;
        value = "&redThere is no spawn with number %number%.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminNoSpawns;
        value = "&redNo spawns have been set here yet.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminTpSpawn;
        value = "You have tped to spawn %number% at %location%.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminSpawnSet;
        value = "Spawn number %number% has been set at %location%.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminSpawnRemove;
        value = "Spawn number %number% has been removed.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminSpawnList;
        value   = "-------------------------------------------\n"
                + "%spawns%\n"
                + "-------------------------------------------";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminSpawnListItem;
        value = "%number% - %location%";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminDMCenterSet;
        value = "Deathmatch center set to %location%.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminDMSpecSpawnSet;
        value = "Deathmatch spectator spawn has been set to %location%.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminNotDMSpawn;
        value = "&redThere is no deathmatch spawn with number %number%.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminNoDMSpawns;
        value = "&redNo deathmatch spawns have been set here yet.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminTpDMSpawn;
        value = "You have tped to deathmatch spawn %number% at %location%.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminDMSpawnSet;
        value = "Deathmatch spawn number %number% has been set at %location%.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminDMSpawnRemove;
        value = "Deathmatch spawn number %number% has been removed.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminDMSpawnList;
        value   = "-------------------------------------------\n"
                + "%spawns%\n"
                + "-------------------------------------------";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminDMSpawnListItem;
        value = "%number% - %location%";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminLobbySpawnSet;
        value = "Lobby spawn set to %location%.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminEditList;
        value   = "-------------------------------------------\n"
                + "%arenas%\n"
                + "-------------------------------------------";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.adminEditListItem;
        value = "%worldname% - %arenaname% (%enabled%)";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.arenaAlreadyEnabled;
        value = "%arenaname% is already enabled.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.arenaAlreadyDisabled;
        value = "%arenaname% is already disabled.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.arenaNoSpawns;
        value = "&redNo spawns have been set in %arenaname%, it is disabled.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.arenaAlreadyExists;
        value = "&redAn arena already exists in the world '%worldname%'.";
        comment = "Message appears when trying to add an already existing arena.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.arenaAdding;
        value = "Adding arena '%worldname%'...";
        comment = "Message appears when adding a new arena.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.arenaAdded;
        value   = "Arena '%arenaname%' has been added!\n"
                + "Loaded %spawns% spawns.\n"
                + "Loaded %dmspawns% deathmatch spawns.\n"
                + "Loaded %containers% containers for rewards.\n"
                + "Loaded %placelist% block place exceptions.\n"
                + "Loaded %placelist% block break exceptions.";
        comment = "Message appears when an arena was added.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.arenaNotAdded;
        value   = "&redArena has not been added. Could not find world '%worldname%'.";
        comment = "Message appears when trying to add a non-existing arena.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.arenaRemoved;
        value   = "Arena '%worldname%' has been removed.";
        comment = "Message appears when an arena has been removed.";
        addLanguage(props, key, value, comment);
        
        props.newLine();
        props.comment("- World -");
        
        key = LangKey.arenaLoaded;
        value = "%arenaname% is now available!";
        comment = "Displayed when a arena is enabled.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.arenaUnloaded;
        value = "%arenaname% is no longer available.";
        comment = "Displayed when a arena is disabled.";
        addLanguage(props, key, value, comment);
        
        props.newLine();
        props.comment("- Global -");
        
        key = LangKey.serverShutdown;
        value = "&redServer is restarting...";
        comment = "The name of the inventory for crates.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.pluginPrefix;
        value = "[SG] ";
        comment = "The prefix for the plugin.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.pluginSuffix;
        value = "";
        comment = "The suffix for the plugin.";
        addLanguage(props, key, value, comment);

        key = LangKey.helpTitle;
        value = "[&green%plugin%&white] Command Help (Page %page%)"; 
        comment = "The title shown for the help.";
        addLanguage(props, key, value, comment);

        key = LangKey.helpItem;
        value = "&f&l/%label%&r&gray - %desc%"; 
        comment = "The title shown for the help.";
        addLanguage(props, key, value, comment);

        key = LangKey.noHelp;
        value = "No help available."; 
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.stateMessageGame;
        value = "-------------------------------------------\n"
              + "%tributesleft% tributes remain.\n"
              + "%timetilldm%s until deathmatch.\n"
              + "%spectators% spectators watching the games.\n"
              + "-------------------------------------------";
        comment = "A periodic message that provides info about the game.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.stateMessageLobby;
        value = "-------------------------------------------\n"
              + "Next map: %nextarena%\n"
              + "%arenavotes%\n"
              + "%waitingplayers%/%maxplayers% tributes waiting to play.\n"
              + "%timetillgame% left until the games begin.\n"
              + "-------------------------------------------";
        comment = "A periodic message that provides info about the game.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.stateMessageLobbyWaiting;
        value = "-------------------------------------------\n"
              + "Next map: %nextarena%\n"
              + "%arenavotes%\n"
              + "%waitingplayers%/%maxplayers% tributes waiting to play.\n"
              + "Waiting for at least %playersneeded% more players.\n"
              + "-------------------------------------------";
        comment = "A periodic message that provides info about the game.";
        addLanguage(props, key, value, comment);
        
        props.newLine();
        props.comment("- Config -");
        
        key = LangKey.configSet;
        value = "%key% has been set to %value% for %file%.";
        comment = "Message shows when a config property has been set.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.configRemoved;
        value = "%key% has been removed from %file%.";
        comment = "Message shows when a config property has been removed.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.configNotFile;
        value = "The file '%file%' does not exist. Don't forget the path.";
        comment = "Message shows when a config file does not exist.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.configNotKey;
        value = "The property %key% was not found in the %file% file.";
        comment = "Message shows when a config property does not exist.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.configGetList;
        value   = "-- %file% config: --\n"
                + "%configs%";
        comment = "Message shows when a config property does not exist.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.configGet;
        value = "%key% = '%value%'   %comment%";
        comment = "Message shows when a config property does not exist.";
        addLanguage(props, key, value, comment);
        
        props.newLine();
        props.comment("- Errors -");
        
        key = LangKey.noLoadedArenas;
        value = "&redNo arenas have been loaded. Therefore you cannot play.";
        comment = "Message shows when no arenas have been loaded.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.arenaNotFound;
        value = "&redArena '%worldname%' not found.";
        comment = "Message displayed when a user enters an invalid arena. /sg worlds";
        addLanguage(props, key, value, comment);
        
        key = LangKey.outOfRange;
        value = "&redPlease choose a number between %min% and %max%.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.notNumber;
        value = "&red%number% is not a number.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.playerNotFound;
        value = "%notfound% was not found on the server.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.playerInGame;
        value = "%tdisplay% is playing the game.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.playerIsGM;
        value = "%tdisplay% is a game maker.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.noSpecInv;
        value = "&redYou can not view a spectator's inventory!";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.noLobbyInv;
        value = "&redYou can not view a player's inventory in the lobby!";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.notInLobby;
        value = "&redYou are not in the lobby!";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.noPermission;
        value = "You do not have permission to do this.";
        comment = "Displayed when a user does not have permission.";
        addLanguage(props, key, value, comment);
        
        key = LangKey.notPlayer;
        value = "This is only available in-game.";
        comment = "";
        addLanguage(props, key, value, comment);
        
        key = LangKey.notTribute;
        value = "%tdisplay% is not a tribute.";
        comment = "";
        addLanguage(props, key, value, comment);

        key = LangKey.unknownCommand;
        value = "Unknown %plugin% command: %command%";
        comment = "The message displayed when an unknown command is entered.";
        addLanguage(props, key, value, comment);

        key = LangKey.teleportSuccessful;
        value = "Teleported to %tdisplay%!";
        comment = "The message displayed when said user was teleported to a player using compass spectating.";
        addLanguage(props, key, value, comment);
        
        props.newLine();
        props.comment(" -- Standard Variables -- (Warning do not uncomment this area)");
        props.comment("%vars% = Shows a list of all available variables for a language.");
        props.comment("%user% = The player who is receiving the message.");
        props.comment("%display% = The display name for the player who is receiving the message.");
        props.comment("%target% = The player targeted by the event or command.");
        props.comment("%tdisplay% = The display name for the player targeted by the event or command.");
        props.comment("%plugin% = The name of this plugin.");
        props.comment("%version% = The version of this plugin.");
        props.newLine();
        props.newLine();
        props.comment(" -- Colors -- (Char Codes can also be used: &c)");
        props.comment("&BLACK, &DARK_BLUE, &DARK_GREEN, &DARK_AQUA, &DARK_RED, &DARK_PURPLE, ");
        props.comment("&GOLD, &GRAY, &DARK_GRAY, &BLUE, &GREEN, &AQUA, &RED, &LIGHT_PURPLE, &YELLOW, ");
        props.comment("&WHITE, &MAGIC");
        props.newLine();
    }
    
    public static void addLanguage(PropertyList props, LangKey key, String value, String comment){
        languages.put(key, props.getString(key.toString(), value, comment));
    }

    public static String getLanguage(LangKey key)
    {
        return getLanguage(key, true);
    }
    
    public static String getLanguage(LangKey key, boolean reset) {
        String message = languages.get(key);
        if(message != null) {
            message = insertVariables(Tools.parseColors(message));
            if(reset)
                resetVariables();
            if(message == null)
                message = "";
        } else {
            Logger.error("Language '" + key.name() + " does not exist.");
        }
        return message;
    }
    
    public static void broadcastLanguage(LangKey key) {
        broadcastLanguage(key, true, true);
    }
    
    public static void broadcastLanguage(LangKey key, boolean usePrefix) {
        broadcastLanguage(key, usePrefix, true);
    }
    
    public static void broadcastLanguage(LangKey key, boolean usePrefix, boolean log) {
        String prefix = getLanguage(LangKey.pluginPrefix, false);
        String suffix = getLanguage(LangKey.pluginSuffix, false);
        String lang = getLanguage(key, false);
        if(!lang.equals("")){
            for(Player player : plugin.getServer().getOnlinePlayers()) {
                setUser(player);
                lang = getLanguage(key, false);
                if(usePrefix) {
                    if(lang.contains("\n"))
                        for(String split : lang.split("\\n")) {
                            player.sendMessage(prefix + split + suffix);
                        }
                    else
                        player.sendMessage(prefix + lang + suffix);
                } else
                    player.sendMessage(lang);
            }
            if(log) {
                if(usePrefix) {
                    if(lang.contains("\n")) {
                        for(String split : lang.split("\\n")) {
                            Logger.info(prefix + split + suffix);
                        }
                    } else {
                        Logger.info(prefix + lang + suffix);
                    }
                } else {
                    if(lang.contains("\n")) {
                        for(String split : lang.split("\\n")) {
                            Logger.info(split);
                        }
                    } else {
                        Logger.info(prefix + lang + suffix);
                    }
                }
            }
            resetVariables();
        }
    }
    
    public static void broadcastAndBlockLanguage(LangKey key, int expire, boolean usePrefix) {
        String prefix = getLanguage(LangKey.pluginPrefix, false);
        String suffix = getLanguage(LangKey.pluginSuffix, false);
        String lang = getLanguage(key, false);
        if(!lang.equals("")){
            for(Player player : plugin.getServer().getOnlinePlayers()) {
                setUser(player);
                lang = getLanguage(key, false);
                if(usePrefix) {
                    if(lang.contains("\n"))
                        for(String split : lang.split("\\n")) {
                            ((SurvivalGames)plugin).getAntiSpammer().sendMessage(player, prefix + split + suffix, expire);
                        }
                    else
                        ((SurvivalGames)plugin).getAntiSpammer().sendMessage(player, prefix + lang + suffix, expire);
                } else
                    ((SurvivalGames)plugin).getAntiSpammer().sendMessage(player, lang, expire);
            }
        if(usePrefix) {
            if(lang.contains("\n")) {
                for(String split : lang.split("\\n")) {
                    Logger.info(prefix + split + suffix);
                }
            } else {
                Logger.info(prefix + lang + suffix);
            }
        } else {
            if(lang.contains("\n")) {
                for(String split : lang.split("\\n")) {
                    Logger.info(split);
                }
            } else {
                Logger.info(prefix + lang + suffix);
            }
        }
            resetVariables();
        }
    }
    
    public static void broadcastCustomLanguage(String language, boolean usePrefix) {
        broadcastCustomLanguage(null, language, usePrefix);
    }
    
    public static void broadcastCustomLanguage(World world, String language, boolean usePrefix) {
        String prefix = getLanguage(LangKey.pluginPrefix, false);
        String suffix = getLanguage(LangKey.pluginSuffix, false);
        String lang = "";
        
        List<Player> players = (List<Player>)(world == null 
                ? Arrays.asList(plugin.getServer().getOnlinePlayers())
                : world.getPlayers());
        for(Player player : players) {
            setUser(player);
            lang = getCustomLanguage(language, false);
            if(usePrefix) {
                if(lang.contains("\n"))
                    for(String split : lang.split("\\n")) {
                        player.sendMessage(prefix + split + suffix);
                    }
                else
                    player.sendMessage(prefix + lang + suffix);
            } else
                player.sendMessage(lang);
        }
        if(usePrefix) {
            if(lang.contains("\n")) {
                for(String split : lang.split("\\n")) {
                    Logger.info(prefix + split + suffix);
                }
            } else {
                Logger.info(prefix + lang + suffix);
            }
        } else {
            if(lang.contains("\n")) {
                for(String split : lang.split("\\n")) {
                    Logger.info(split);
                }
            } else {
                Logger.info(prefix + lang + suffix);
            }
        }
        resetVariables();
    }
    
    public static void sendLanguage(Player player, LangKey key) {
        sendLanguage(player, key, true);
    }
    
    public static void sendLanguage(Player player, LangKey key, boolean reset) {
        String prefix = getLanguage(LangKey.pluginPrefix, false);
        String suffix = getLanguage(LangKey.pluginSuffix, false);
        String lang = getLanguage(key, reset);
        if(!lang.equals("")){
            if(lang.contains("\n"))
                for(String split : lang.split("\\n")) {
                    player.sendMessage(prefix + split + suffix);
                }
            else
                player.sendMessage(prefix + lang + suffix);
        }
    }
    
    public static void sendAndBlockLanguage(Player player, LangKey key, int expire) {
        String prefix = getLanguage(LangKey.pluginPrefix, false);
        String suffix = getLanguage(LangKey.pluginSuffix, false);
        String lang = getLanguage(key, true);
        if(!lang.equals("")){
            if(lang.contains("\n"))
                for(String split : lang.split("\\n")) {
                    ((SurvivalGames)plugin).getAntiSpammer().sendMessage(player, prefix + split + suffix, expire);
                }
            else
                ((SurvivalGames)plugin).getAntiSpammer().sendMessage(player, prefix + lang + suffix, expire);
        }
    }
    
    public static String getCustomLanguage(String language) {
        return getCustomLanguage(language, true);
    }
    
    public static String getCustomLanguage(String language, boolean reset) {
        String message = insertVariables(Tools.parseColors(language));
        if(reset)
            resetVariables();
        return message;
    }
    
    public static void sendCustomLanguage(Player player, String lang) {
        sendCustomLanguage(player, lang, true);
    }
    
    public static void sendCustomLanguage(Player player, String lang, boolean reset) {
        if(!lang.equals("")){
            if(lang.contains("\n"))
                for(String split : lang.split("\\n")) {
                    player.sendMessage(insertVariables(Tools.parseColors(split)));
                }
            else
                player.sendMessage(insertVariables(Tools.parseColors(lang)));
        }
        if(reset)
            resetVariables();
    }
    
    public static void sendAndBlockCustomLanguage(Player player, String lang, int expire) {
        sendAndBlockCustomLanguage(player, lang, true, expire);
    }
    
    public static void sendAndBlockCustomLanguage(Player player, String lang, boolean reset, int expire) {
        if(!lang.equals("")){
            ((SurvivalGames)plugin).getAntiSpammer().sendMessage(player, insertVariables(Tools.parseColors(lang)), expire);
        }
        if(reset)
            resetVariables();
    }

    public static String insertVariables(String message) {
        String allVars = "{";
        for (String var : vars.keySet()) {
            message = message.replaceAll(var, vars.get(var));
            allVars += var + ", ";
        }
        if(!allVars.equals("{")) {
            allVars = allVars.substring(0, allVars.length() - 2) + "}";
            message = message.replaceAll("%vars%", allVars);
        }
        return message;
    }
    
    public static void setUser(Player player){
        setVar("user", player.getName());
        setVar("display", player.getDisplayName());
    }
    
    public static void setTarget(Player player){
        setVar("target", player.getName());
        setVar("tdisplay", player.getDisplayName());
    }

    public static void setVar(String name, String value) {
        String key = "%" + name.toLowerCase().trim() + "%";
        if (vars.containsKey(key)) {
            vars.remove(key);
        }
        vars.put(key, value);
    }
    
    public static void resetVar(String name) {
        vars.remove(name);
    }

    public static void resetVariables() {
        vars.clear();
        vars.put("%plugin%", plugin.getName());
        vars.put("%version%", plugin.getDescription().getVersion());
        vars.put("%user%", "You");
        vars.put("%display%", "You");
        vars.put("%target%", "Unknown");
        vars.put("%tdisplay%", "Unknown");
    }
}
