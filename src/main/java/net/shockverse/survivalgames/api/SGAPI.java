package net.shockverse.survivalgames.api;

import net.shockverse.survivalgames.GameManager.SGGameState;
import net.shockverse.survivalgames.*;
import net.shockverse.survivalgames.core.Language;
import net.shockverse.survivalgames.core.Language.LangKey;
import net.shockverse.survivalgames.data.ArenaData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

/**
 * Provides access to the Survival Games API.
 * @author LegitModern, Tagette
 * @license http://creativecommons.org/licenses/by-sa/3.0/deed.en_US
 */
public class SGAPI {
    
    private static SurvivalGames plugin;
    
    public static void Intialize(SurvivalGames instance) {
        plugin = instance;
        
    }
    
    /**
     * Gets the game manager.
     * @return Returns null if SurvivalGames is not available.
     */
    public static GameManager getGameManager() {
        if(plugin == null)
            return null;
        return plugin.getGameManager();
    }
    
    /**
     * Gets the arena manager.
     * @return Returns null if SurvivalGames is not available.
     */
    public static ArenaManager getArenaManager() {
        if(plugin == null)
            return null;
        return plugin.getArenaManager();
    }
    
    /**
     * Gets the player statistic manager.
     * @return Returns null if SurvivalGames is not available.
     */
    public static StatManager getStatManager() {
        if(plugin == null)
            return null;
        return plugin.getStatManager();
    }
    
    /**
     * Gets the vote manager.
     * @return Returns null if SurvivalGames is not available.
     */
    public static VoteManager getVoteManager() {
        if(plugin == null)
            return null;
        return getArenaManager().getVoteManager();
    }
    
    /**
     * Gets the current state of the game.
     * @return The game state.
     */
    public static SGGameState getGameState() {
        return getGameManager().getState();
    }
    
    /**
     * Get the winner if not null.
     * @return The game winner.
     */
    public static Player getWinner() {
    	if (getGameManager().winner == null)
    		return null;
    	return getGameManager().winner;
    }
    
    /**
     * Gets the time (in millis) left before the next state change.
     * @return Time in milliseconds.
     */
    public static long getTimeLeft() {
        GameManager gameMan = getGameManager();
        long timeLeft = 0;
        
        if (gameMan.getState() == SGGameState.LOBBY) {
            timeLeft = gameMan.nextGame - System.currentTimeMillis();
        } else if (gameMan.getState() == SGGameState.STARTING) {
            timeLeft = gameMan.nextGameStart - System.currentTimeMillis();
        } else if (gameMan.getState() == SGGameState.GAME) {
            timeLeft = gameMan.nextDeathmatch - System.currentTimeMillis();
        } else if (gameMan.getState() == SGGameState.PRE_DEATHMATCH) {
            timeLeft = gameMan.nextDeathmatch - System.currentTimeMillis();
        } else if (gameMan.getState() == SGGameState.DEATHMATCH) {
            timeLeft = gameMan.nextEndGame - System.currentTimeMillis();
        }
        
        return timeLeft;
    }
    
    /**
     * Starts the next arena immediately.
     * @return Returns the results of the API call.
     */
    public static SGAPIResult startGame() {
        return startGame("", -1);
    }
    
    /**
     * Starts a game in a specific arena immediately.
     * @param arena The world name for the arena to start the game in.
     * @return Returns the results of the API call.
     */
    public static SGAPIResult startGame(String arena) {
        return startGame(arena, -1);
    }
    
    /**
     * Starts the next arena with a countdown.
     * @param seconds The countdown seconds before the game starts.
     * @return Returns the results of the API call.
     */
    public static SGAPIResult startGame(int seconds) {
        return startGame("", seconds);
    }
    
    /**
     * Starts a game in a specific arena with a countdown.
     * @param arena The world name for the arena to start the game in.
     * @param seconds The countdown seconds before the game starts.
     * @return Returns the results of the API call.
     */
    public static SGAPIResult startGame(String arena, int seconds) {
        SGAPIResult result = new SGAPIResult();
        GameManager gameMan = getGameManager();
        ArenaManager arenaMan = getArenaManager();
        
        if(!gameMan.getTributeNames().isEmpty()) {
            if (gameMan.getState() == SGGameState.LOBBY) {
                if(arenaMan.arenaOrder.size() > 0) {
                    if(arena.equals("") || arenaMan.arenaOrder.contains(arena)) {
                        gameMan.cancelTasks();
                        if(!arena.equals(""))
                            arenaMan.setNextArena(arena);
                        gameMan.startGame(seconds);
                        Language.setVar("arena", arenaMan.getNextArena().name);
                        result.message = Language.getLanguage(LangKey.adminGameStarted);
                        result.success = true;
                    } else {
                        Language.setVar("worldname", arena);
                        result.message = Language.getLanguage(LangKey.arenaNotFound);
                    }
                } else {
                    result.message = Language.getLanguage(LangKey.noLoadedArenas);
                }
            } else {
                result.message = Language.getLanguage(LangKey.adminAlreadyStarted);
            }
        } else {
            result.message = Language.getLanguage(LangKey.adminNoTributes);
        }
        
        return result;
    }
    
    /**
     * Starts the death match if the game is started.
     * @return Returns the results of the API call.
     */
    public static SGAPIResult startDeathmatch() {
        SGAPIResult result = new SGAPIResult();
        GameManager gameMan = getGameManager();
        
        if (gameMan.getState() == SGGameState.GAME) {
            gameMan.cancelTasks();
            gameMan.startDeathmatch();
            result.message = Language.getLanguage(LangKey.adminDeathmatchStart);
            result.success = true;
        } else {
            result.message = Language.getLanguage(LangKey.adminGameNotStarted);
        }
        
        return result;
    }
    
    /**
     * Stops the current playing game.
     * @return Returns the results of the API call.
     */
    public static SGAPIResult stopGame() {
        SGAPIResult result = new SGAPIResult();
        GameManager gameMan = getGameManager();
        
        if (gameMan.getState() != SGGameState.LOBBY) {
            gameMan.cancelTasks();
            gameMan.endGame();
            result.message = Language.getLanguage(LangKey.adminGameStopped);
            result.success = true;
        } else {
            result.message = Language.getLanguage(LangKey.adminGameNotStarted);
        }
        
        return result;
    }
    
    /**
     * Refills the chests all over the map.
     * @return Returns the results of the API call.
     */
    public static SGAPIResult refillChests() {
        SGAPIResult result = new SGAPIResult();
        ArenaManager arenaMan = getArenaManager();
        
        arenaMan.refillContainers();
        result.message = Language.getLanguage(LangKey.adminRefillChests);
        result.success = true;
        
        return result;
    }
    
    /**
     * Adds a player to the game as a tribute.
     * @param target The player to add to the game.
     * @return Returns the results of the API call.
     */
    public static SGAPIResult addPlayerToGame(Player target) {
        SGAPIResult result = new SGAPIResult();
        GameManager gameMan = getGameManager();
        ArenaManager arenaMan = getArenaManager();
        
        if (gameMan.isSpectator(target) || gameMan.isAdmin(target)) {
            gameMan.setTribute(target);
            target.setGameMode(GameMode.SURVIVAL);
            target.setFlying(false);
            target.setAllowFlight(false);
            gameMan.setVanished(target, false);
            gameMan.resetPlayer(target);
            target.teleport(arenaMan.getCurrentArena().spectatorSpawn);

            Language.setTarget(target);
            result.message = Language.getLanguage(LangKey.adminPlayerAdded);
            result.success = true;
        } else {
            Language.setTarget(target);
            result.message = Language.getLanguage(LangKey.playerInGame);
        }
        
        return result;
    }
    
    /**
     * Removes a player from a game and makes them a spectator.
     * @param target The player to remove from the game.
     * @return Returns the results of the API call.
     */
    public static SGAPIResult removePlayerFromGame(Player target) {
        SGAPIResult result = new SGAPIResult();
        GameManager gameMan = getGameManager();
        ArenaManager arenaMan = getArenaManager();
        
        if(gameMan.getState() != SGGameState.LOBBY) {
            target.setGameMode(GameMode.ADVENTURE);
            target.setAllowFlight(true);
            target.setFlying(true);
            gameMan.setVanished(target, true);
            gameMan.resetPlayer(target);
        } else {
            target.setGameMode(GameMode.SURVIVAL);
            target.setFlying(false);
            target.setAllowFlight(false);
            gameMan.setVanished(target, false);
            gameMan.resetPlayer(target);
            arenaMan.getVoteManager().removeVotes(target.getName());
        }
        gameMan.setSpectator(target);
        ArenaData cData = arenaMan.getCurrentArena();
        target.teleport(cData.spectatorSpawn);

        Language.setTarget(target);
        result.message = Language.getLanguage(LangKey.adminPlayerRemoved);
        result.success = true;
        
        return result;
    }
    
}
