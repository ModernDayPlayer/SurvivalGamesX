package net.shockverse.survivalgames.listeners;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

import net.shockverse.survivalgames.GameManager.SGGameState;
import net.shockverse.survivalgames.ArenaManager;
import net.shockverse.survivalgames.GameManager;
import net.shockverse.survivalgames.PlayerStats;
import net.shockverse.survivalgames.SurvivalGames;
import net.shockverse.survivalgames.core.*;
import net.shockverse.survivalgames.core.Language.LangKey;
import net.shockverse.survivalgames.data.ArenaData;
import net.shockverse.survivalgames.data.ContainerData;
import net.shockverse.survivalgames.extras.GameTask;

import net.shockverse.survivalgames.extras.InventoryMenu;
import net.shockverse.survivalgames.extras.ItemUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

/**
 * @description Handles all player related events
 *
 * @author Duker02, LegitModern, Tagette
 */
public class PlayerListener implements Listener {

    private final SurvivalGames plugin;

    public PlayerListener(SurvivalGames instance) {
        plugin = instance;
    }

    public void disable() {
        // Unregister your events on plugin disable.
        // PlayerJoinEvent.getHandlerList().unregister(plugin);
        PlayerBucketFillEvent.getHandlerList().unregister(this);
        PlayerBucketEmptyEvent.getHandlerList().unregister(this);
        ProjectileLaunchEvent.getHandlerList().unregister(this);
        PlayerMoveEvent.getHandlerList().unregister(this);
        EntityDamageEvent.getHandlerList().unregister(this);
        PlayerJoinEvent.getHandlerList().unregister(this);
        PlayerLoginEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
        PlayerKickEvent.getHandlerList().unregister(this);
        PlayerChangedWorldEvent.getHandlerList().unregister(this);
        PlayerPickupItemEvent.getHandlerList().unregister(this);
        PlayerDropItemEvent.getHandlerList().unregister(this);
        InventoryClickEvent.getHandlerList().unregister(this);
        PlayerInteractEvent.getHandlerList().unregister(this);
        PlayerAnimationEvent.getHandlerList().unregister(this);
        PlayerRespawnEvent.getHandlerList().unregister(this);
        AsyncPlayerChatEvent.getHandlerList().unregister(this);
                                
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerBucketFill(PlayerBucketFillEvent ev) {
        Player p = ev.getPlayer();
        if (!plugin.getGameManager().isAdmin(p)
                && !plugin.getArenaManager().isEditor(p.getName())) {
            ev.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent ev) {
        Player p = ev.getPlayer();

        if (!plugin.getGameManager().isAdmin(p)
                && !plugin.getArenaManager().isEditor(p.getName())) {
            ev.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onProjectileLaunch(ProjectileLaunchEvent ev) {
        LivingEntity shooter = (LivingEntity) ev.getEntity().getShooter();

        if ((shooter instanceof Player)) {
            Player p = (Player) shooter;

            if ((plugin.getGameManager().isSpectator(p)
                    || plugin.getGameManager().getState() == SGGameState.LOBBY 
                    || plugin.getGameManager().getState() == SGGameState.STARTING 
                    || plugin.getGameManager().getState() == SGGameState.PRE_DEATHMATCH)
                    && !plugin.getGameManager().isAdmin(p)) {
                ev.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent ev) {
        Player p = ev.getPlayer();
        Location from = ev.getFrom();
        Location to = ev.getTo();

        if ((plugin.getGameManager().getState() == SGGameState.STARTING
                || plugin.getGameManager().getState() == SGGameState.PRE_DEATHMATCH)
                && (plugin.getGameManager().isTribute(p))
                && ((from.getX() != to.getX()) || (from.getZ() != to.getZ()))) {
            p.teleport(from);
        }

        if ((plugin.getGameManager().getState() == SGGameState.DEATHMATCH)
                && (plugin.getGameManager().isTribute(p))) {
            ArenaData aData = plugin.getArenaManager().getCurrentArena();
            Location dmCenter = aData.dmCenter;
            if (new Vector(dmCenter.getBlockX(), 0, dmCenter.getBlockZ()).distance(new Vector(to.getBlockX(), 0, to.getBlockZ())) >= aData.dmRange) {
                if(aData.killDMRun) {
                    p.setHealth(0);
                } else {
                    p.teleport(from);
                }
                Language.setUser(p);
                Language.sendAndBlockLanguage(p, LangKey.dmTeleport, 2000);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onGPHit(EntityDamageEvent e) {
        if (plugin.getGameManager().getState() != SGGameState.LOBBY) {
            if (e.getEntity() instanceof Player
                    && (plugin.getGameManager().grace.contains(((Player) e.getEntity()).getName()))) {
                e.setCancelled(true);
            } else if (e.getEntity() instanceof Player
                    && plugin.getGameManager().isSpectator((Player) e.getEntity())) {
                e.setCancelled(true);
            } else {
                e.setCancelled(false);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent ev) {
        final Player p = ev.getPlayer();
        ArenaManager arenaMan = plugin.getArenaManager();
        GameManager gameMan = plugin.getGameManager();
        ArenaData aData = arenaMan.getCurrentArena();
        ArenaData nData = arenaMan.getNextArena();
        int joinLevel = gameMan.getKickJoinLevel(p);
        boolean adminLogin = Perms.has(p, "survivalgames.admin.login", p.isOp());
        boolean inGame = gameMan.getState() != SGGameState.LOBBY;
        Location spawn = aData.spectatorSpawn;
        plugin.getDebug().normal(p.getName() + "'s kick join is " + joinLevel + ".");
        
        ev.setJoinMessage(null);
        p.getInventory().clear();
        
        if(plugin.getArenaManager().arenaOrder.isEmpty())
            Language.sendLanguage(p, LangKey.noLoadedArenas);
        
        // Add the player then set last played.
        plugin.getStatManager().addPlayer(p.getName()).setLastPlayed(System.currentTimeMillis());
        
        int tributeSlots = !inGame 
                ? (nData != null ? nData.spawns.size() : aData.spawns.size()) 
                : aData.spawns.size();
        if(adminLogin) {
            gameMan.setVanished(p, inGame); // If in game then vanish.
            Language.sendLanguage(p, LangKey.joinAdmin);
        } else { // Always join as a spectator and let the update move the player to tribute.
            gameMan.setSpectator(p);
            gameMan.setVanished(p, true); // Spectators are always not visible.

            ItemStack compass = ItemUtils.createItemStack(
                    "&a&lSpectate Players &7(Right Click)",
                    Arrays.asList(
                            "&7Right click to open the spectating menu!"
                    ),
                    Material.COMPASS
            );

            p.getInventory().setItem(0, compass);
            //Language.sendLanguage(p, LangKey.joinSpec);
        }
        for(PotionEffect effect : p.getActivePotionEffects()) {
            p.removePotionEffect(effect.getType());
        }
        p.setGameMode(GameMode.ADVENTURE);
        p.setAllowFlight(inGame);
        p.setFlying(inGame);
        gameMan.resetPlayer(p);
        p.teleport(spawn);
        
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(PlayerLoginEvent ev) {
        Player p = ev.getPlayer();
        GameManager gameMan = plugin.getGameManager();
        int kickJoinLevel = gameMan.getKickJoinLevel(p);
        plugin.getDebug().normal(p.getName() + " login with join level " + kickJoinLevel + ".");
        boolean adminLogin = Perms.has(p, "survivalgames.admin.login", p.isOp());
        int nonAdminsOnline = gameMan.getTributeNames().size() + gameMan.getSpectatorNames().size();
        
        if(adminLogin) {
            ev.setResult(Result.ALLOWED);
        } else if(ev.getResult() == Result.KICK_BANNED) {
            ev.setKickMessage(Language.getLanguage(LangKey.joinBanned));
        } else if(ev.getResult() == Result.KICK_WHITELIST) {
            ev.setKickMessage(Language.getLanguage(LangKey.joinServerDisabled));
        } else if(nonAdminsOnline < plugin.getSettings().playerLimit) {
            ev.setResult(Result.ALLOWED);
        } else if(kickJoinLevel > 0) {
            // Need to kick someone to allow.
            HashMap<Integer, List<String>> tributeLevels = new HashMap<Integer, List<String>>();
            HashMap<Integer, List<String>> spectatorLevels = new HashMap<Integer, List<String>>();
            
            // Sort tributes into map.
            if(gameMan.getState() == SGGameState.LOBBY) {
                for(Player tribute : gameMan.getTributes()) {
                    int joinLevel = gameMan.getKickJoinLevel(tribute);
                    if(!tributeLevels.containsKey(joinLevel))
                        tributeLevels.put(joinLevel, new ArrayList<String>());
                    tributeLevels.get(joinLevel).add(tribute.getName());
                }
            }
            // Sort spectators into map.
            for(Player spectator : gameMan.getSpectators()) {
                int joinLevel = gameMan.getKickJoinLevel(spectator);
                if(!spectatorLevels.containsKey(joinLevel))
                    spectatorLevels.put(joinLevel, new ArrayList<String>());
                spectatorLevels.get(joinLevel).add(spectator.getName());
            }
            
            String kickName = null;
            
            // Check tributes. This will add player to game as tribute.
            for(int i = 0; i < kickJoinLevel; i++) {
                if(tributeLevels.containsKey(i)) {
                    int size = tributeLevels.get(i).size();
                    kickName = tributeLevels.get(i).get(size - 1);
                    break;
                }
            }
            if(kickName == null) {
                // Check spectators.
                for(int i = 0; i < kickJoinLevel; i++) {
                    if(spectatorLevels.containsKey(i)) {
                        int size = spectatorLevels.get(i).size();
                        kickName = spectatorLevels.get(i).get(size - 1);
                        break;
                    }
                }
            }
            plugin.getDebug().normal("Kick?");
            if(kickName != null) {
                plugin.getDebug().normal("Trying to kick " + kickName + ".");
                Player toKick = plugin.getServer().getPlayer(kickName);
                if(toKick != null) {
                    gameMan.removePlayer(toKick);
                    if(plugin.getSettings().useBungee 
                            && !Tools.isNullEmptyWhite(plugin.getSettings().bungeeServer)) {
                        try {
                            Language.sendLanguage(p, LangKey.joinKick);
                            ByteArrayOutputStream  b = new ByteArrayOutputStream();
                            DataOutputStream out = new DataOutputStream(b);
                            out.writeUTF("Connect");
                            out.writeUTF(plugin.getSettings().bungeeServer);
                            toKick.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
                        } catch(Exception ex) {
                            Logger.warning("Unable to send player to bungee server.");
                            toKick.kickPlayer(Language.getLanguage(LangKey.joinKick));
                        }
                    } else {
                        toKick.kickPlayer(Language.getLanguage(LangKey.joinKick));
                    }
                } else {
                    plugin.getDebug().normal("Was unable to kick " + kickName + ".");
                }
                Language.sendLanguage(p, LangKey.joinKickOther);
                ev.setResult(Result.ALLOWED);
            } else {
                ev.setKickMessage(Language.getLanguage(LangKey.joinKickFull));
                ev.setResult(Result.KICK_FULL);
            }
            
        } else {
            ev.setKickMessage(Language.getLanguage(LangKey.joinFull));
            ev.setResult(Result.KICK_FULL);
        }
        
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent ev) {
        Player p = ev.getPlayer();

        if (plugin.getGameManager().isSpectator(p)) {
            ev.setQuitMessage(null);
        }
        
        // Remove the players vote when they leave.
        plugin.getArenaManager().getVoteManager().removeVotes(p.getName());
        
        plugin.getGameManager().removePlayer(p);
        
        ArenaManager arenaMan = plugin.getArenaManager();
        World world = p.getWorld();
        ArenaData aFromData = arenaMan.get(world.getName());
        
        if(arenaMan.isEditing(p.getName(), world.getName())
                && aFromData != arenaMan.getLobby()) {
            arenaMan.finishEditting(p.getName());
//            arenaMan.createWorldZips(aFromData.worldName, true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerKick(PlayerKickEvent ev) {
        Player p = ev.getPlayer();

        if (plugin.getGameManager().isSpectator(p)) {
            ev.setLeaveMessage(null);
        }
        
        // Remove the players vote when they leave.
        plugin.getArenaManager().getVoteManager().removeVotes(p.getName());
        plugin.getGameManager().removePlayer(p);
        
        ArenaManager arenaMan = plugin.getArenaManager();
        World wFrom = p.getWorld();
        ArenaData aFromData = arenaMan.get(wFrom.getName());
        
        // Save arena if this player was editing.
        if(arenaMan.isEditing(p.getName(), wFrom.getName())
                && aFromData != arenaMan.getLobby()) {
            arenaMan.finishEditting(p.getName());
//            arenaMan.createWorldZips(aFromData.worldName, true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent ev) {
        Player p = ev.getPlayer();
        ArenaManager arenaMan = plugin.getArenaManager();
        World wFrom = ev.getFrom();
        ArenaData aFromData = arenaMan.get(wFrom.getName());
        
        if (plugin.getGameManager().isSpectator(p)) {
        	p.setGameMode(GameMode.ADVENTURE);
        	p.setAllowFlight(true);
        	p.setFlying(true);
        }
        // Tagette
        // Remove the player's vote if they are not in the lobby.
        if (!p.getWorld().getName().equals(plugin.getArenaManager().getLobby().worldName) 
                && plugin.getGameManager().getState() == SGGameState.LOBBY) // Remove the players vote when they leave.
        {
            plugin.getArenaManager().getVoteManager().removeVotes(p.getName());
        }
        
        if(arenaMan.isEditing(p.getName(), wFrom.getName())) {
            arenaMan.finishEditting(p.getName());
//            if(arenaMan.getEditors(aFromData.worldName).isEmpty()) {
//                Language.setVar("worldname", aFromData.worldName);
//                Language.setVar("arenaname", aFromData.name);
//                Language.sendLanguage(p, LangKey.adminSaving);
//                arenaMan.createWorldZips(aFromData.worldName, true);
//                Language.setVar("worldname", aFromData.worldName);
//                Language.setVar("arenaname", aFromData.name);
//                Language.sendLanguage(p, LangKey.adminSave);
//            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerPickupItem(PlayerPickupItemEvent ev) {
        Player p = ev.getPlayer();

        if (plugin.getGameManager().isSpectator(p)) {
            ev.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDropItem(PlayerDropItemEvent ev) {
        Player p = ev.getPlayer();

        if (plugin.getGameManager().isSpectator(p) && !p.isDead()) {
            ev.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInventoryClick(InventoryClickEvent ev) {
        Player player = (Player) ev.getWhoClicked();

        if (ev.getInventory().getTitle().contains("Spectating Menu")) {
            ev.setCancelled(true);
            if (ev.getCurrentItem().hasItemMeta()) {
                Player target = Bukkit.getPlayer(ChatColor.stripColor(ev.getCurrentItem().getItemMeta().getDisplayName()));
                player.teleport(target);

                Language.setTarget(player);
                Language.setVar("tdisplay", target.getDisplayName());
                sendLanguage(player, Language.LangKey.teleportSuccessful);
            }
        }

        if (plugin.getGameManager().isSpectator(player) && !player.isOp() && !Perms.hasAll(player)) {
            ev.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();

        if (plugin.getGameManager().getState() == SGGameState.LOBBY
                && p.getGameMode() == GameMode.CREATIVE
                && !plugin.getGameManager().isAdmin(p)
                && !Perms.has(p, "survivalgames.admin", p.isOp())) {
            p.setGameMode(GameMode.SURVIVAL);
            p.setFlying(false);
            p.setAllowFlight(false);
        }

        //TODO: Toggle compass spectating?
        /*
        if (plugin.getGameManager().isSpectator(p)) {
        	if (event.getAction().equals(Action.LEFT_CLICK_AIR))
        		plugin.getGameManager().switchSpectate(p);
        	if (event.getAction().equals(Action.LEFT_CLICK_BLOCK))
        		plugin.getGameManager().switchSpectate(p);
            event.setCancelled(true);
        }
        */

        try {
            if (plugin.getGameManager().isSpectator(p)) {
                if (event.getAction().equals(Action.LEFT_CLICK_AIR)) {
                    if (event.getItem().getType().equals(Material.COMPASS) && event.getItem().getItemMeta().getDisplayName().contains("Spectate Players")) {
                        InventoryMenu menu = new InventoryMenu("Spectating Menu", 3);
                        for(int x = 0; x < plugin.getGameManager().tributes.size(); x++) {
                            if (Bukkit.getPlayer(plugin.getGameManager().tributes.get(x)) != p) {
                                PlayerStats stats = plugin.getStatManager().getPlayer(Bukkit.getPlayer(plugin.getGameManager().tributes.get(x)).getName());

                                menu.addItem(
                                        ItemUtils.createItemStack(
                                                "&b&l" + Bukkit.getPlayer(plugin.getGameManager().tributes.get(x)).getName(),
                                                Arrays.asList(
                                                        "&7Click to spectate this player!", "&8&m------------------------",
                                                        "&eHealth &l: &c" + (float) getPlayerHealth(Bukkit.getPlayer(plugin.getGameManager().tributes.get(x))) + " ❤",
                                                        "&eHunger Level &l: &c" + Bukkit.getPlayer(plugin.getGameManager().tributes.get(x)).getFoodLevel(),
                                                        "&eSaturation Level &l: &c" + Bukkit.getPlayer(plugin.getGameManager().tributes.get(x)).getSaturation(), "&8&m------------------------",
                                                        "&eKills &l: &c" + stats.getKills(),
                                                        "&eDeaths &l: &c" + stats.getDeaths(),
                                                        "&eWins &l: &c" + stats.getWins(),
                                                        "&eTies &l: &c" + stats.getTies(),
                                                        "&eLosses &l: &c" + stats.getLosses(),
                                                        "&ePoints &l: &c" + stats.getPoints(),
                                                        "&eTime Played &l: &c" + Tools.getTime(stats.getTimePlayed()),
                                                        "&eLast Played &l: &c" + Tools.getTime(System.currentTimeMillis() - stats.getLastPlayed()),
                                                        "&eChests Looted &l: &c" + stats.getContainersLooted()),
                                                Material.SKULL_ITEM));


                                menu.getInventory().getItem(x).setDurability((short) 3);
                            }
                        }

                        menu.open(p);
                    }
                }

                if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
                    if (event.getItem().getType().equals(Material.COMPASS) && event.getItem().getItemMeta().getDisplayName().contains("Spectate Players")) {
                        InventoryMenu menu = new InventoryMenu("Spectating Menu", 3);
                        for(int x = 0; x < plugin.getGameManager().tributes.size(); x++) {
                            if (Bukkit.getPlayer(plugin.getGameManager().tributes.get(x)) != p) {
                                PlayerStats stats = plugin.getStatManager().getPlayer(Bukkit.getPlayer(plugin.getGameManager().tributes.get(x)).getName());

                                menu.addItem(
                                        ItemUtils.createItemStack(
                                                "&b&l" + Bukkit.getPlayer(plugin.getGameManager().tributes.get(x)).getName(),
                                                Arrays.asList(
                                                        "&7Click to spectate this player!", "&8&m------------------------",
                                                        "&eHealth &l: &c" + (float) getPlayerHealth(Bukkit.getPlayer(plugin.getGameManager().tributes.get(x))) + " ❤",
                                                        "&eHunger Level &l: &c" + Bukkit.getPlayer(plugin.getGameManager().tributes.get(x)).getFoodLevel(),
                                                        "&eSaturation Level &l: &c" + Bukkit.getPlayer(plugin.getGameManager().tributes.get(x)).getSaturation(), "&8&m------------------------",
                                                        "&eKills &l: &c" + stats.getKills(),
                                                        "&eDeaths &l: &c" + stats.getDeaths(),
                                                        "&eWins &l: &c" + stats.getWins(),
                                                        "&eTies &l: &c" + stats.getTies(),
                                                        "&eLosses &l: &c" + stats.getLosses(),
                                                        "&ePoints &l: &c" + stats.getPoints(),
                                                        "&eTime Played &l: &c" + Tools.getTime(stats.getTimePlayed()),
                                                        "&eLast Played &l: &c" + Tools.getTime(System.currentTimeMillis() - stats.getLastPlayed()),
                                                        "&eChests Looted &l: &c" + stats.getContainersLooted()),
                                                Material.SKULL_ITEM));


                                menu.getInventory().getItem(x).setDurability((short) 3);
                            }
                        }

                        menu.open(p);
                    }
                }
                event.setCancelled(true);
            }
        } catch (NullPointerException ignored) { }

        if(plugin.getGameManager().getState() != SGGameState.LOBBY && !plugin.getGameManager().isSpectator(p)
                && !event.isCancelled() && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            Material type = clickedBlock.getType();
            ArenaManager arenaMan = plugin.getArenaManager();
            ArenaData aData = arenaMan.get(p.getWorld().getName());
            if(aData != null) {
                if(aData.containers.containsKey(type)) {
                    ContainerData cData = aData.containers.get(type);
                    if(cData.enabled) {
                        if(clickedBlock.getType() == Material.ENDER_CHEST) {
                            event.setCancelled(true);
                            openBlock(clickedBlock, p, cData.title);
                        } else if(clickedBlock.getState() instanceof InventoryHolder) {
                            openContainer(clickedBlock, p);
                        } else if(clickedBlock.getData() == cData.data) {
                            event.setCancelled(true);
                            openBlock(clickedBlock, p, cData.title);
                        }
                    }
                } else if(clickedBlock.getState() instanceof InventoryHolder) {
                    event.setCancelled(true);
                }
            }
        }

        if(plugin.getGameManager().getState() != SGGameState.LOBBY && plugin.getGameManager().isSpectator(p)
                && !event.isCancelled() && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            // We don't want spectators to right click stuff lol.
        }
    }
    
    private void openContainer(Block clickedBlock, Player player) {
        plugin.getDebug().normal("Container clicked at " + clickedBlock.getLocation() + ".");
        Inventory inventory = ((InventoryHolder) clickedBlock.getState()).getInventory();
        if(plugin.getArenaManager().canFillContainer(clickedBlock)) {
            plugin.getArenaManager().clearContainer(clickedBlock);
            plugin.getArenaManager().fillContainer(clickedBlock, inventory);
            plugin.getDebug().normal("    Container filled at " + clickedBlock.getLocation() + ".");
            PlayerStats stats = plugin.getStatManager().getPlayer(player.getName());
            stats.setContainersLooted(stats.getContainersLooted() + 1, true);
        }
    }
    
    private void openBlock(Block clickedBlock, Player player, String title) {
        plugin.getDebug().normal(clickedBlock.getType() + " clicked at " + clickedBlock.getLocation() + ".");
        Inventory inventory;
        if(plugin.getArenaManager().lootedContainers.containsKey(clickedBlock))
            inventory = plugin.getArenaManager().lootedContainers.get(clickedBlock);
        else
            inventory = plugin.getServer().createInventory(null, 27, title);
        if(plugin.getArenaManager().canFillContainer(clickedBlock)) {
            plugin.getArenaManager().clearContainer(clickedBlock);
            plugin.getArenaManager().fillContainer(clickedBlock, inventory);
            plugin.getDebug().normal("    Container filled at " + clickedBlock.getLocation() + ".");
            PlayerStats stats = plugin.getStatManager().getPlayer(player.getName());
            stats.setContainersLooted(stats.getContainersLooted() + 1, true);
        }
        player.openInventory(inventory);
    }

    //@EventHandler(priority = EventPriority.HIGH) // Changed to high because of multiverse. :|
    public void onPlayerRespawn(PlayerRespawnEvent ev) {
        Player p = ev.getPlayer();
        if (plugin.getGameManager().getState() != SGGameState.LOBBY) {
            if(plugin.getGameManager().isSpectator(p)) {
                plugin.getGameManager().setVanished(p, true);
                p.setGameMode(GameMode.ADVENTURE);
                p.setAllowFlight(true);
                p.setFlying(true);
                if(plugin.getGameManager().getState() == SGGameState.GAME) {
                    ev.setRespawnLocation(plugin.getArenaManager().getCurrentArena().spectatorSpawn);
                } else {
                    ev.setRespawnLocation(plugin.getArenaManager().getCurrentArena().dmSpectatorSpawn);
                }
            } else {
                plugin.getGameManager().setVanished(p, false);
                p.setGameMode(GameMode.SURVIVAL);
                p.setFlying(false);
                p.setAllowFlight(false);
                ev.setRespawnLocation(p.getLocation());
            }
        } else {
            p.setGameMode(GameMode.SURVIVAL);
            p.setFlying(false);
            p.setAllowFlight(false);
            ev.setRespawnLocation(Bukkit.getWorld(plugin.getArenaManager().getLobby().worldName).getSpawnLocation());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent ev) {
        Player p = ev.getPlayer();
        final Treasury treasury = new Treasury(plugin);
        final String senderName = p.getName();
        final String message = Matcher.quoteReplacement(ev.getMessage());
        
        if(plugin.getSettings().useCustomChat) {
            final int points = plugin.getStatManager().getPlayer(p.getName()).getPoints();
            final double balance = treasury.getEconomy().getBalance(Bukkit.getServer().getOfflinePlayer(p.getName()));
            if (plugin.getGameManager().isTribute(p)){
                GameManager gameMan = plugin.getGameManager();
                for(Player player : p.getWorld().getPlayers()) {
                    if(gameMan.isTribute(player) 
                            || gameMan.isSpectator(player) 
                            || Perms.has(player, "survivalgames.admin.chat", player.isOp())) {
                        final String pName = player.getName();
                        new GameTask(plugin) {
                            @Override
                            public void run() {
                                Player sender = plugin.getServer().getPlayer(senderName);
                                Player p = plugin.getServer().getPlayer(pName);
                                Language.setTarget(sender);
                                Language.setVar("points", points + "");
                                Language.setVar("money", balance + "");
                                Language.setVar("message", message);
                                Language.sendCustomLanguage(p, plugin.getArenaManager().getCurrentArena().tributeChat, false);
                                Logger.info(Language.getCustomLanguage(plugin.getArenaManager().getCurrentArena().tributeChat, true));
                            }
                        };
                    }
                }
                ev.setCancelled(true);
            }

            else if (Perms.has(p, "survivalgames.admin.chat", p.isOp()) || plugin.getGameManager().isAdmin(p)) {
                for(Player player : p.getWorld().getPlayers()) {
                    final String pName = player.getName();
                    new GameTask(plugin) {
                        @Override
                        public void run() {
                            Player sender = plugin.getServer().getPlayer(senderName);
                            Player p = plugin.getServer().getPlayer(pName);
                            Language.setTarget(sender);
                            Language.setVar("points", points + "");
                            Language.setVar("money", balance + "");
                            Language.setVar("message", message);
                            Language.sendCustomLanguage(p, plugin.getArenaManager().getCurrentArena().adminChat, false);
                            Logger.info(Language.getCustomLanguage(plugin.getArenaManager().getCurrentArena().adminChat, true));
                        }
                    };
                }
                ev.setCancelled(true);
            } 

            else if(plugin.getGameManager().isSpectator(p)) {
                GameManager gameMan = plugin.getGameManager();
                for(Player player : p.getWorld().getPlayers()) {
                    if(gameMan.isSpectator(player)
                            || Perms.has(player, "survivalgames.admin.chat", player.isOp())) {
                        final String pName = player.getName();
                        new GameTask(plugin) {
                            @Override
                            public void run() {
                                Player sender = plugin.getServer().getPlayer(senderName);
                                Player p = plugin.getServer().getPlayer(pName);
                                Language.setTarget(sender);
                                Language.setVar("points", points + "");
                                Language.setVar("money", balance + "");
                                Language.setVar("message", message);
                                Language.sendCustomLanguage(p, plugin.getArenaManager().getCurrentArena().specChat, false);
                                Logger.info(Language.getCustomLanguage(plugin.getArenaManager().getCurrentArena().specChat, true));
                            }
                        };
                    }
                }
                ev.setCancelled(true);
            }
        } else {
            if(plugin.getGameManager().isSpectator(p)) {
                ev.getRecipients().removeAll(plugin.getGameManager().getTributeNames());
            }
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerCraft(CraftItemEvent event) {
//        for(int i = 0; i < event.getWhoClicked().getInventory().getSize(); i++) {
//            ItemStack item = event.getWhoClicked().getInventory().getItem(i);
//            if(item != null) {
//                if(item.getType() == Material.FLINT_AND_STEEL && item.getDurability() > 3) {
//                    item.setDurability((short) 3);
//                }
//            }
//        }
        if (event.getCurrentItem().getType() ==  Material.FLINT_AND_STEEL) {
            ArenaData aData = plugin.getArenaManager().getCurrentArena();
            event.getCurrentItem().setDurability((short) (Material.FLINT_AND_STEEL.getMaxDurability() - aData.lighterUses));
        }
    }

    // Horrible way of doing this. :P
    public double getPlayerHealth(Player p) {
        double h = p.getHealth();

        if (h % 2.0D == 0.0D) {
            return h / 2.0D;
        }
        if (h == 19.0D) {
            return 9.5D;
        }
        if (h == 17.0D) {
            return 8.5D;
        }
        if (h == 15.0D) {
            return 7.5D;
        }
        if (h == 13.0D) {
            return 6.5D;
        }
        if (h == 11.0D) {
            return 5.5D;
        }
        if (h == 9.0D) {
            return 4.5D;
        }
        if (h == 7.0D) {
            return 3.5D;
        }
        if (h == 5.0D) {
            return 2.5D;
        }
        if (h == 3.0D) {
            return 1.5D;
        }
        if (h == 1.0D) {
            return 0.5D;
        }
        return h;
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

    /*
     * == Enchant ==
     *
     * EnchantItemEvent PrepareItemEnchantEvent
     *
     * == Hanging ==
     *
     * HangingBreakByEntityEvent HangingBreakEvent HangingEvent
     * HangingPlaceEvent
     *
     * == Inventory ==
     *
     * BrewEvent CraftItemEvent FurnaceBurnEvent FurnaceExtractEvent
     * FurnaceSmeltEvent InventoryClickEvent InventoryCloseEvent
     * InventoryCreativeEvent InventoryDragEvent InventoryEvent
     * InventoryInteractEvent InventoryMoveItemEvent InventoryOpenEvent
     * InventoryPickupItemEvent PrepareItemCraftEvent
     *
     * == Player ==
     *
     * AsyncPlayerChatEvent AsyncPlayerPreLoginEvent PlayerAnimationEvent
     * PlayerBedEnterEvent PlayerBedLeaveEvent PlayerBucketEmptyEvent
     * PlayerBucketEvent PlayerBucketFillEvent PlayerChangedWorldEvent
     * PlayerChannelEvent PlayerChatEvent PlayerChatTabCompleteEvent
     * PlayerCommandPreprocessEvent PlayerDropItemEvent PlayerEditBookEvent
     * PlayerEggThrowEvent PlayerEvent PlayerExpChangeEvent PlayerFishEvent
     * PlayerGameModeChangeEvent PlayerInteractEntityEvent PlayerInteractEvent
     * PlayerInventoryEvent PlayerItemBreakEvent PlayerItemConsumeEvent
     * PlayerItemHeldEvent PlayerJoinEvent PlayerKickEvent
     * PlayerLevelChangeEvent PlayerLoginEvent PlayerMoveEvent
     * PlayerPickupItemEvent PlayerPortalEvent PlayerPreLoginEvent
     * PlayerQuitEvent PlayerRegisterChannelEvent PlayerRespawnEvent
     * PlayerShearEntityEvent PlayerTeleportEvent PlayerToggleFlightEvent
     * PlayerToggleSneakEvent PlayerToggleSprintEvent
     * PlayerUnregisterChannelEvent PlayerVelocityEvent
     */
    // Simplifies and shortens the if statements for commands.
    private boolean is(String entered, String label) {
        return entered.equalsIgnoreCase(label);
    }
}
