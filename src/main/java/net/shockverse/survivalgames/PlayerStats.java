package net.shockverse.survivalgames;

import java.sql.ResultSet;
import java.sql.SQLException;
import net.shockverse.survivalgames.core.DataAccess;
import net.shockverse.survivalgames.extras.DataManager;

/**
 * A class used to store statistics for players.
 * @author LegitModern, Tagette
 */
public class PlayerStats {
    
    private String player;
    
    // Kills/Death
    private int kills;
    private int killStreak;
    private int bestKillStreak;
    private int deaths;
    private int deathStreak;
    private int worstDeathStreak;
    
    private int points;
    
    // Win/lose
    private int wins;
    private int winStreak;
    private int bestWinStreak;
    private int ties;
    private int losses;
    private int loseStreak;
    private int worstLoseStreak;
    private int gamesPlayed;
    
    // Misc
    private int timePlayed;
    private long lastPlayed;
    private int containersLooted;
    private int animalsKilled;
    private int mobsKilled;
    
    public PlayerStats(String pName) {
        this.player = pName;
        kills = 0;
        killStreak = 0;
        bestKillStreak = 0;
        deaths = 0;
        deathStreak = 0;
        worstDeathStreak = 0;
        points = 0;
        wins = 0;
        winStreak = 0;
        bestWinStreak = 0;
        ties = 0;
        losses = 0;
        loseStreak = 0;
        worstLoseStreak = 0;
        gamesPlayed = 0;
        timePlayed = 0;
        lastPlayed = System.currentTimeMillis();
        containersLooted = 0;
        animalsKilled = 0;
        mobsKilled = 0;
    }
    
    // Static
    
    public static void RemoveFromDB(String pName) {
        DataManager dbm = DataAccess.getManager();
        String query = "DELETE FROM players WHERE player = '" + pName + "'";
        dbm.execute(query);
    }
    
    public static PlayerStats LoadFromDB(String pName) {
        PlayerStats loadStats = null;
        DataManager dbm = DataAccess.getManager();
        String query = "SELECT * FROM players WHERE player = '" + pName + "'";
        ResultSet rs = dbm.query(query);
        try {
            if(rs.next()) {
                loadStats = new PlayerStats(pName);
                loadStats.setKills(rs.getInt("kills"));
                loadStats.setKillStreak(rs.getInt("killstreak"));
                loadStats.setBestKillStreak(rs.getInt("bestkillstreak"));
                loadStats.setDeaths(rs.getInt("deaths"));
                loadStats.setDeathStreak(rs.getInt("deathstreak"));
                loadStats.setWorstDeathStreak(rs.getInt("worstdeathstreak"));
                loadStats.setPoints(rs.getInt("points"));
                loadStats.setWins(rs.getInt("wins"));
                loadStats.setWinStreak(rs.getInt("winstreak"));
                loadStats.setBestWinStreak(rs.getInt("bestwinstreak"));
                loadStats.setTies(rs.getInt("ties"));
                loadStats.setLosses(rs.getInt("losses"));
                loadStats.setLoseStreak(rs.getInt("losestreak"));
                loadStats.setWorstLoseStreak(rs.getInt("worstlosestreak"));
                loadStats.setGamesPlayed(rs.getInt("games"));
                loadStats.setTimePlayed(rs.getInt("timeplayed"));
                loadStats.setLastPlayed(rs.getLong("lastplayed"));
                loadStats.setContainersLooted(rs.getInt("containerslooted"));
                loadStats.setAnimalsKilled(rs.getInt("animalskilled"));
                loadStats.setMobsKilled(rs.getInt("mobskilled"));
            }
        } catch(SQLException se) {}
        return loadStats;
    }
    
    // Instantial
    
    /**
     * Gets the player's name.
     * @return The player's name.
     */
    public String getPlayer() {
        return player;
    }
    
    /**
     * Saves these stats to the database.
     */
    public void save() {
        DataManager dbm = DataAccess.getManager();
        String query = "DELETE FROM players WHERE player = '" + player + "'";
        dbm.execute(query);
        
        query = "INSERT INTO players ("
                + "player, "
                + "kills, "
                + "killstreak, "
                + "bestkillstreak, "
                + "deaths, "
                + "deathstreak, "
                + "worstdeathstreak, "
                + "points, "
                + "wins, "
                + "winstreak, "
                + "bestwinstreak, "
                + "ties, "
                + "losses, "
                + "losestreak, "
                + "worstlosestreak, "
                + "timeplayed, "
                + "games, "
                + "lastplayed, "
                + "containerslooted, "
                + "animalskilled, "
                + "mobskilled) VALUES ('"
                + player + "', '"
                + getKills() + "', '"
                + getKillStreak() + "', '"
                + getBestKillStreak() + "', '"
                + getDeaths() + "', '"
                + getDeathStreak() + "', '"
                + getWorstDeathStreak() + "', '"
                + getPoints() + "', '"
                + getWins() + "', '"
                + getWinStreak() + "', '"
                + getBestWinStreak() + "', '"
                + getTies() + "', '"
                + getLosses() + "', '"
                + getLoseStreak() + "', '"
                + getWorstLoseStreak() + "', '"
                + getTimePlayed() + "', '"
                + getGamesPlayed() + "', '"
                + getLastPlayed() + "', '"
                + getContainersLooted() + "', '"
                + getAnimalsKilled() + "', '"
                + getMobsKilled() + "')";
        dbm.execute(query);
    }

    /**
     * Gets the amount of kills for this player.
     * @return the kills
     */
    public int getKills() {
        return kills;
    }

    /**
     * Sets the amount of kills for this player.
     * @param kills the kills to set
     */
    public void setKills(int kills) {
        this.kills = kills;
    }

    /**
     * Sets the amount of kills for this player.
     * @param kills the kills to set
     * @param update updates the value in the database
     */
    public void setKills(int kills, boolean update) {
        this.kills = kills;
        if(update) {
            String query = "UPDATE players SET kills = '" + kills + "' WHERE player = '" + player + "'";
            DataManager dbm = DataAccess.getManager();
            dbm.execute(query);
        }
    }

    /**
     * Gets the current kill streak for this player.
     * @return the killStreak
     */
    public int getKillStreak() {
        return killStreak;
    }

    /**
     * Sets the current kill streak for this player.
     * @param killStreak the killStreak to set
     */
    public void setKillStreak(int killStreak) {
        this.killStreak = killStreak;
    }

    /**
     * Sets the current kill streak for this player.
     * @param killStreak the killStreak to set
     * @param update updates the value in the database
     */
    public void setKillStreak(int killStreak, boolean update) {
        this.killStreak = killStreak;
        if(update) {
            String query = "UPDATE players SET killstreak = '" + killStreak + "' WHERE player = '" + player + "'";
            DataManager dbm = DataAccess.getManager();
            dbm.execute(query);
        }
    }

    /**
     * Gets the best kill streak for this player.
     * @return the bestKillStreak
     */
    public int getBestKillStreak() {
        return bestKillStreak;
    }

    /**
     * Sets the best kill streak for this player.
     * @param bestKillStreak the bestKillStreak to set
     */
    public void setBestKillStreak(int bestKillStreak) {
        this.bestKillStreak = bestKillStreak;
    }

    /**
     * Sets the best kill streak for this player.
     * @param bestKillStreak the bestKillStreak to set
     * @param update updates the value in the database
     */
    public void setBestKillStreak(int bestKillStreak, boolean update) {
        this.bestKillStreak = bestKillStreak;
        if(update) {
            String query = "UPDATE players SET bestkillstreak = '" + bestKillStreak + "' WHERE player = '" + player + "'";
            DataManager dbm = DataAccess.getManager();
            dbm.execute(query);
        }
    }

    /**
     * Gets the amount of deaths for this player.
     * @return the deaths
     */
    public int getDeaths() {
        return deaths;
    }

    /**
     * Sets the amount of deaths for this player.
     * @param deaths the deaths to set
     */
    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    /**
     * Sets the amount of deaths for this player.
     * @param deaths the deaths to set
     * @param update updates the value in the database
     */
    public void setDeaths(int deaths, boolean update) {
        this.deaths = deaths;
        if(update) {
            String query = "UPDATE players SET deaths = '" + deaths + "' WHERE player = '" + player + "'";
            DataManager dbm = DataAccess.getManager();
            dbm.execute(query);
        }
    }

    /**
     * Gets the current death streak for this player.
     * @return the deathStreak
     */
    public int getDeathStreak() {
        return deathStreak;
    }

    /**
     * Sets the current death streak for this player.
     * @param deathStreak the deathStreak to set
     */
    public void setDeathStreak(int deathStreak) {
        this.deathStreak = deathStreak;
    }

    /**
     * Sets the current death streak for this player.
     * @param deathStreak the deathStreak to set
     * @param update updates the value in the database
     */
    public void setDeathStreak(int deathStreak, boolean update) {
        this.deathStreak = deathStreak;
        if(update) {
            String query = "UPDATE players SET deathstreak = '" + deathStreak + "' WHERE player = '" + player + "'";
            DataManager dbm = DataAccess.getManager();
            dbm.execute(query);
        }
    }

    /**
     * Gets the worst death streak for this player.
     * @return the worstDeathStreak
     */
    public int getWorstDeathStreak() {
        return worstDeathStreak;
    }

    /**
     * Sets the worst death streak for this player.
     * @param worstDeathStreak the worstDeathStreak to set
     */
    public void setWorstDeathStreak(int worstDeathStreak) {
        this.worstDeathStreak = worstDeathStreak;
    }

    /**
     * Sets the worst death streak for this player.
     * @param worstDeathStreak the worstDeathStreak to set
     * @param update updates the value in the database
     */
    public void setWorstDeathStreak(int worstDeathStreak, boolean update) {
        this.worstDeathStreak = worstDeathStreak;
        if(update) {
            String query = "UPDATE players SET worstdeathstreak = '" + worstDeathStreak + "' WHERE player = '" + player + "'";
            DataManager dbm = DataAccess.getManager();
            dbm.execute(query);
        }
    }

    /**
     * Gets the total points for this player.
     * @return the points
     */
    public int getPoints() {
        return points;
    }

    /**
     * Sets the total points for this player.
     * @param points the points to set
     */
    public void setPoints(int points) {
        this.points = points;
    }

    /**
     * Sets the total points for this player.
     * @param points the points to set
     * @param update updates the value in the database
     */
    public void setPoints(int points, boolean update) {
        this.points = points;
        if(update) {
            String query = "UPDATE players SET points = '" + points + "' WHERE player = '" + player + "'";
            DataManager dbm = DataAccess.getManager();
            dbm.execute(query);
        }
    }

    /**
     * Gets the total wins for this player.
     * @return the wins
     */
    public int getWins() {
        return wins;
    }

    /**
     * Sets the total wins for this player.
     * @param wins the wins to set
     */
    public void setWins(int wins) {
        this.wins = wins;
    }

    /**
     * Sets the total wins for this player.
     * @param wins the wins to set
     * @param update updates the value in the database
     */
    public void setWins(int wins, boolean update) {
        this.wins = wins;
        if(update) {
            String query = "UPDATE players SET wins = '" + wins + "' WHERE player = '" + player + "'";
            DataManager dbm = DataAccess.getManager();
            dbm.execute(query);
        }
    }

    /**
     * Gets the current win streak for this player.
     * @return the winStreak
     */
    public int getWinStreak() {
        return winStreak;
    }

    /**
     * Sets the current win streak for this player.
     * @param winStreak the winStreak to set
     */
    public void setWinStreak(int winStreak) {
        this.winStreak = winStreak;
    }

    /**
     * Sets the current win streak for this player.
     * @param winStreak the winStreak to set
     * @param update updates the value in the database
     */
    public void setWinStreak(int winStreak, boolean update) {
        this.winStreak = winStreak;
        if(update) {
            String query = "UPDATE players SET winstreak = '" + winStreak + "' WHERE player = '" + player + "'";
            DataManager dbm = DataAccess.getManager();
            dbm.execute(query);
        }
    }

    /**
     * Gets the best win streak for this player.
     * @return the bestWinStreak
     */
    public int getBestWinStreak() {
        return bestWinStreak;
    }

    /**
     * Sets the best win streak for this player.
     * @param bestWinStreak the bestWinStreak to set
     */
    public void setBestWinStreak(int bestWinStreak) {
        this.bestWinStreak = bestWinStreak;
    }

    /**
     * Sets the best win streak for this player.
     * @param bestWinStreak the bestWinStreak to set
     * @param update updates the value in the database
     */
    public void setBestWinStreak(int bestWinStreak, boolean update) {
        this.bestWinStreak = bestWinStreak;
        if(update) {
            String query = "UPDATE players SET bestwinstreak = '" + bestWinStreak + "' WHERE player = '" + player + "'";
            DataManager dbm = DataAccess.getManager();
            dbm.execute(query);
        }
    }

    /**
     * Gets the total ties for this player.
     * @return the ties
     */
    public int getTies() {
        return ties;
    }

    /**
     * Sets the total ties for this player.
     * @param ties the ties to set
     */
    public void setTies(int ties) {
        this.ties = ties;
    }

    /**
     * Sets the total ties for this player.
     * @param ties the ties to set
     * @param update updates the value in the database
     */
    public void setTies(int ties, boolean update) {
        this.ties = ties;
        if(update) {
            String query = "UPDATE players SET ties = '" + ties + "' WHERE player = '" + player + "'";
            DataManager dbm = DataAccess.getManager();
            dbm.execute(query);
        }
    }

    /**
     * Gets the total losses for this player.
     * @return the losses
     */
    public int getLosses() {
        return losses;
    }

    /**
     * Sets the total losses for this player.
     * @param losses the losses to set
     */
    public void setLosses(int losses) {
        this.losses = losses;
    }

    /**
     * Sets the total losses for this player.
     * @param losses the losses to set
     * @param update updates the value in the database
     */
    public void setLosses(int losses, boolean update) {
        this.losses = losses;
        if(update) {
            String query = "UPDATE players SET losses = '" + losses + "' WHERE player = '" + player + "'";
            DataManager dbm = DataAccess.getManager();
            dbm.execute(query);
        }
    }

    /**
     * Gets the current lose streak for this player.
     * @return the loseStreak
     */
    public int getLoseStreak() {
        return loseStreak;
    }

    /**
     * Sets the current lose streak for this player.
     * @param loseStreak the loseStreak to set
     */
    public void setLoseStreak(int loseStreak) {
        this.loseStreak = loseStreak;
    }

    /**
     * Sets the current lose streak for this player.
     * @param loseStreak the loseStreak to set
     * @param update updates the value in the database
     */
    public void setLoseStreak(int loseStreak, boolean update) {
        this.loseStreak = loseStreak;
        if(update) {
            String query = "UPDATE players SET losestreak = '" + loseStreak + "' WHERE player = '" + player + "'";
            DataManager dbm = DataAccess.getManager();
            dbm.execute(query);
        }
    }

    /**
     * Gets the worst lose streak for this player.
     * @return the worstLoseStreak
     */
    public int getWorstLoseStreak() {
        return worstLoseStreak;
    }

    /**
     * Sets the worst lose streak for this player.
     * @param worstLoseStreak the worstLoseStreak to set
     */
    public void setWorstLoseStreak(int worstLoseStreak) {
        this.worstLoseStreak = worstLoseStreak;
    }

    /**
     * Sets the worst lose streak for this player.
     * @param worstLoseStreak the worstLoseStreak to set
     * @param update updates the value in the database
     */
    public void setWorstLoseStreak(int worstLoseStreak, boolean update) {
        this.worstLoseStreak = worstLoseStreak;
        if(update) {
            String query = "UPDATE players SET worstlosestreak = '" + worstLoseStreak + "' WHERE player = '" + player + "'";
            DataManager dbm = DataAccess.getManager();
            dbm.execute(query);
        }
    }

    /**
     * Gets the total games played for this player.
     * @return the gamesPlayed
     */
    public int getGamesPlayed() {
        return gamesPlayed;
    }

    /**
     * Sets the total games played for this player.
     * @param gamesPlayed the gamesPlayed to set
     */
    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    /**
     * Sets the total games played for this player.
     * @param gamesPlayed the gamesPlayed to set
     * @param update updates the value in the database
     */
    public void setGamesPlayed(int gamesPlayed, boolean update) {
        this.gamesPlayed = gamesPlayed;
        if(update) {
            String query = "UPDATE players SET gamesplayed = '" + gamesPlayed + "' WHERE player = '" + player + "'";
            DataManager dbm = DataAccess.getManager();
            dbm.execute(query);
        }
    }

    /**
     * Gets the total time played in milliseconds.
     * @return the timePlayed
     */
    public int getTimePlayed() {
        return timePlayed;
    }

    /**
     * Sets the total time played in milliseconds.
     * @param timePlayed the timePlayed to set
     */
    public void setTimePlayed(int timePlayed) {
        this.timePlayed = timePlayed;
    }

    /**
     * Sets the total time played in milliseconds.
     * @param timePlayed the timePlayed to set
     * @param update updates the value in the database
     */
    public void setTimePlayed(int timePlayed, boolean update) {
        this.timePlayed = timePlayed;
        if(update) {
            String query = "UPDATE players SET timeplayed = '" + timePlayed + "' WHERE player = '" + player + "'";
            DataManager dbm = DataAccess.getManager();
            dbm.execute(query);
        }
    }

    /**
     * Gets the last time played in UTC milliseconds.
     * @return the lastPlayed
     */
    public long getLastPlayed() {
        return lastPlayed;
    }

    /**
     * Sets the last time played in UTC milliseconds.
     * @param lastPlayed the lastPlayed to set
     */
    public void setLastPlayed(long lastPlayed) {
        this.lastPlayed = lastPlayed;
    }

    /**
     * Sets the last time played in UTC milliseconds.
     * @param lastPlayed the lastPlayed to set
     * @param update updates the value in the database
     */
    public void setLastPlayed(long lastPlayed, boolean update) {
        this.lastPlayed = lastPlayed;
        if(update) {
            String query = "UPDATE players SET lastplayed = '" + lastPlayed + "' WHERE player = '" + player + "'";
            DataManager dbm = DataAccess.getManager();
            dbm.execute(query);
        }
    }

    /**
     * Gets the total amount of reward containers looted by this player.
     * @return the containersLooted
     */
    public int getContainersLooted() {
        return containersLooted;
    }

    /**
     * Sets the total amount of reward containers looted by this player.
     * @param containersLooted the containersLooted to set
     */
    public void setContainersLooted(int containersLooted) {
        this.containersLooted = containersLooted;
    }

    /**
     * Sets the total amount of reward containers looted by this player.
     * @param containersLooted the containersLooted to set
     * @param update updates the value in the database
     */
    public void setContainersLooted(int containersLooted, boolean update) {
        this.containersLooted = containersLooted;
        if(update) {
            String query = "UPDATE players SET containerslooted = '" + containersLooted + "' WHERE player = '" + player + "'";
            DataManager dbm = DataAccess.getManager();
            dbm.execute(query);
        }
    }

    /**
     * Gets the total amount of animals killed by this player.
     * @return the animalsKilled
     */
    public int getAnimalsKilled() {
        return animalsKilled;
    }

    /**
     * Sets the total amount of animals killed by this player.
     * @param animalsKilled the animalsKilled to set
     */
    public void setAnimalsKilled(int animalsKilled) {
        this.animalsKilled = animalsKilled;
    }

    /**
     * Sets the total amount of animals killed by this player.
     * @param animalsKilled the animalsKilled to set
     * @param update updates the value in the database
     */
    public void setAnimalsKilled(int animalsKilled, boolean update) {
        this.animalsKilled = animalsKilled;
        if(update) {
            String query = "UPDATE players SET animalskilled = '" + animalsKilled + "' WHERE player = '" + player + "'";
            DataManager dbm = DataAccess.getManager();
            dbm.execute(query);
        }
    }

    /**
     * Gets the total amount of mobs killed by this player.
     * @return the mobsKilled
     */
    public int getMobsKilled() {
        return mobsKilled;
    }

    /**
     * Sets the total amount of mobs killed by this player.
     * @param mobsKilled the mobsKilled to set
     */
    public void setMobsKilled(int mobsKilled) {
        this.mobsKilled = mobsKilled;
    }

    /**
     * Sets the total amount of mobs killed by this player.
     * @param mobsKilled the mobsKilled to set
     * @param update updates the value in the database
     */
    public void setMobsKilled(int mobsKilled, boolean update) {
        this.mobsKilled = mobsKilled;
        if(update) {
            String query = "UPDATE players SET mobskilled = '" + mobsKilled + "' WHERE player = '" + player + "'";
            DataManager dbm = DataAccess.getManager();
            dbm.execute(query);
        }
    }
    
}
