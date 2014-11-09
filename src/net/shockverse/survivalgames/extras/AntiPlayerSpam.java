package net.shockverse.survivalgames.extras;

import java.util.ArrayList;
import java.util.List;

import net.shockverse.survivalgames.SurvivalGames;

import org.bukkit.entity.Player;

public class AntiPlayerSpam {

    public class PlayerMessager {

        public Player player;
        public List<PlayerMessage> messages;

        public PlayerMessager(Player p) {
            player = p;
            messages = new ArrayList<PlayerMessage>();
        }

        public PlayerMessage getMessage(String message) {
            for (int i = 0; i < messages.size(); i++) {
                PlayerMessage msg = messages.get(i);
                if (msg.message.equals(message)) {
                    return msg;
                }
            }
            return null;
        }

        public boolean hasMessage(String message) {
            for (int i = 0; i < messages.size(); i++) {
                PlayerMessage msg = messages.get(i);
                if (msg.message.equals(message)) {
                    if (msg.expire >= 0 && System.currentTimeMillis() - msg.time >= msg.expire) {
                        messages.remove(i);
                        return false;
                    }
                    return true;
                }
            }
            return false;
        }
    }

    public class PlayerMessage {

        public String message;
        public long time;
        public int expire;

        public PlayerMessage(String message, long time, int expire) {
            this.message = message;
            this.time = time;
            this.expire = expire;
        }
    }
    private SurvivalGames plugin;
    private List<PlayerMessager> playerMessagers;

    public AntiPlayerSpam(SurvivalGames instance) {
        plugin = instance;
        playerMessagers = new ArrayList<PlayerMessager>();
    }

    /**
     * Checks whether it is OK or not to send a message to the player again.
     * @param player The player to check.
     * @param message The message to check.
     * 
     * Note this will check the message exactly. This class is meant to only stop message output from the plugin.
     */
    public boolean canSendMessage(Player player, String message) {
        for (PlayerMessager msgs : playerMessagers) {
            if (msgs.player == player || msgs.player.equals(player)) {
                return !msgs.hasMessage(message);
            }
        }
        return true;
    }

    /**
     * Blocks a message from being sent to the player until it expires.
     * @param player The player to block incoming messages.
     * @param message The message to block.
     * @param expire The amount of time before the message blocking expires.
     * 
     * * Note that this will not block messages that are send without checking first.
     */
    public void blockMessage(Player player, String message, int expire) {
        boolean found = false;
        for (PlayerMessager msgs : playerMessagers) {
            if (msgs.player == player || msgs.player.equals(player)) {
                PlayerMessage msg = msgs.getMessage(message);
                if (msg != null) {
                    msg.time = System.currentTimeMillis();
                    msg.expire = expire;
                } else {
                    msgs.messages.add(new PlayerMessage(message, System.currentTimeMillis(), expire));
                }
                found = true;
            }
        }
        if (!found) {
            PlayerMessager messager = new PlayerMessager(player);
            messager.messages.add(new PlayerMessage(message, System.currentTimeMillis(), expire));
            playerMessagers.add(messager);
        }
    }

    /**
     * Sends a message to a player and blocks it at the same time.
     * @param player The player
     * @param message The message to block
     * @param expire The time in milliseconds
     */
    public void sendMessage(Player player, String message, int expire) {
        if (canSendMessage(player, message)) {
            player.sendMessage(message);
            blockMessage(player, message, expire);
        }
    }
    
    /**
     * Unblocks messages for a player.
     * @param player The player.
     */
    public void unblockMessages(Player player) {
        for (PlayerMessager msgs : playerMessagers) {
            if (msgs.player == player || msgs.player.equals(player)) {
                msgs.messages.clear();
            }
        }
    }
    
    /**
     * Unblocks a message for a player.
     * @param player The player.
     * @param message The message to unblock.
     */
    public void unblockMessage(Player player, String message) {
        for (PlayerMessager msgs : playerMessagers) {
            if (msgs.player == player || msgs.player.equals(player)) {
                msgs.getMessage(message).expire = 0;
            }
        }
    }
}
