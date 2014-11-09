package net.shockverse.survivalgames.listeners;

import net.shockverse.survivalgames.SurvivalGames;
import net.shockverse.survivalgames.core.Language;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;


/**
 * @description Handles compass spectating & other items. Needs fixing.
 * @author LegitModern, Tagette
 */
public class ItemListener implements Listener {
/* OLD CODE, let's fix soon!
    private final SurvivalGames plugin;

    public ItemListener(final SurvivalGames plugin) {
        this.plugin = plugin;
    }

    public void disable() {
        InventoryClickEvent.getHandlerList().unregister(plugin);
    }

    @EventHandler
    public void spectateMenuClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (e.getInventory().getTitle().contains("Spectating Menu")) {
            e.setCancelled(true);
            if (e.getCurrentItem().hasItemMeta()) {
                Player target = Bukkit.getPlayer(ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName()));
                player.teleport(target);

                Language.setTarget(player);
                Language.setVar("tdisplay", target.getDisplayName());
                sendLanguage(player, Language.LangKey.teleportSuccessful);
            }
        }
    }

    private boolean sendLanguage(Player player, Language.LangKey key) {
        boolean sent = false;
        Language.setUser(player);
        String message = Language.getLanguage(key, false);
        if (message != null && !message.equals("")) {
            Language.sendLanguage(player, key);
            sent = true;
        }
        return sent;
    }
         */
}
