package net.shockverse.survivalgames.core;

import net.milkbowl.vault.economy.Economy;
import net.shockverse.survivalgames.SurvivalGames;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * @description Deals with economy setup.
 * @author Tagette, LegitModern
 */
public class Treasury {

    private SurvivalGames plugin;
    private Economy economy;
    
    public Treasury(SurvivalGames instance) {
        plugin = instance;
        RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
            //if(!plugin.getSettings().LowDetailMode)
                Logger.info("'" + economy.getName() + "' vault economy hooked into.");
        }
    }
    
    public void onOtherPluginEnable() {
        RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            if(economy == null || !economy.getName().equals(economyProvider.getProvider().getName())) {
                economy = economyProvider.getProvider();
                //if(!plugin.getSettings().LowDetailMode)
                    Logger.info("'" + economy.getName() + "' vault economy hooked into.");
            }
        }
    }
    
    public boolean hasEconomy() {
        return economy != null;
    }
    
    public Economy getEconomy() {
        return economy;
    }
    
}
