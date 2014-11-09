package net.shockverse.survivalgames.core;

import java.util.Arrays;
import java.util.List;
import net.milkbowl.vault.permission.Permission;
import net.shockverse.survivalgames.SurvivalGames;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * @description Handles all plugin permissions
 * @author Duker02, LegitModern, Tagette
 */
public class Perms {
    
    public static Permission permission = null;
    private static SurvivalGames plugin;

    public static void initialize(SurvivalGames instance) {
    	plugin = instance;
    	RegisteredServiceProvider<Permission> permissionProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
            if(!plugin.getSettings().LowDetailMode)
                Logger.info("'" + permission.getName() + "' vault permissions hooked into.");
        }
    }

    public static void onOtherPluginEnable() {
        RegisteredServiceProvider<Permission> permissionProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            if(permission == null || !permission.getName().equals(permissionProvider.getProvider().getName())) {
                permission = permissionProvider.getProvider();
                if(!Perms.plugin.getSettings().LowDetailMode)
                    Logger.info("'" + permission.getName() + "' vault permissions hooked into.");
            }
        }
    }
    
    public static void disable() {
        permission = null;
    }
    
    public static Permission getHandler(){
        return permission;
    }
    
    public static boolean hasHandler()
    {
        return permission != null;
    }
    
    public static boolean has(Player player, String perm, boolean def){
        if(!hasHandler())
            return def;
        return permission.has(player, perm);
    }

    public static boolean hasAll(Player player) {
        return permission.has(player, plugin.name + ".lkjhsdafkjh");
    }
    
    public static boolean hasAllGroupsIn(Player player, List<String> groups) {
        boolean hasGroups = true;
        List<String> playerGroups = Arrays.asList(permission.getPlayerGroups(player));
        for(String group : groups) {
            if(!playerGroups.contains(group)) {
                hasGroups = false;
                break;
            }
        }
        return hasGroups;
    }
    
    public static boolean hasAGroupFrom(Player player, List<String> groups) {
        boolean hasGroups = false;
        List<String> playerGroups = Arrays.asList(permission.getPlayerGroups(player));
        for(String group : groups) {
            if(playerGroups.contains(group)) {
                hasGroups = true;
                break;
            }
        }
        return hasGroups;
    }
}
