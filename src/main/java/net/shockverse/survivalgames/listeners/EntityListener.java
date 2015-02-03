package net.shockverse.survivalgames.listeners;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.shockverse.survivalgames.GameManager.SGGameState;
import net.shockverse.survivalgames.PlayerDamage;
import net.shockverse.survivalgames.SurvivalGames;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @decsription Handles all block related events
 *
 * @author Duker02, LegitModern, Tagette
 */
public class EntityListener implements Listener {

    private final SurvivalGames plugin;

    public EntityListener(final SurvivalGames plugin) {
        this.plugin = plugin;
    }

    public void disable() {
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCreatureSpawn(CreatureSpawnEvent ev) {
        if ((ev.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL)
                && (ev.getEntityType() == EntityType.SLIME)
                && (ev.getLocation().getWorld().getWorldType() == WorldType.FLAT)) {
            ev.setCancelled(true);
        }
        if (((ev.getEntity() instanceof Monster))
                && ((plugin.getGameManager().getState() == SGGameState.LOBBY)
                || (plugin.getGameManager().getState() == SGGameState.STARTING)
                || (plugin.getGameManager().getState() == SGGameState.PRE_DEATHMATCH)
                || (plugin.getGameManager().getState() == SGGameState.RESETTING))) {
            ev.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent ev) {
        Entity entity1 = ev.getTarget();

        if ((entity1 instanceof Player)) {
            Player target = (Player) entity1;

            if ((plugin.getGameManager().isSpectator(target))
                    || (plugin.getGameManager().getState() == SGGameState.LOBBY)
                    || (plugin.getGameManager().getState() == SGGameState.STARTING)
                    || (plugin.getGameManager().getState() == SGGameState.PRE_DEATHMATCH)
                    || (plugin.getGameManager().getState() == SGGameState.RESETTING)) {
                ev.setCancelled(true);
            }
        }
    }

    @SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent ev) {
        Entity entity1 = ev.getEntity();
        Entity entity2 = ev.getDamager();
        World world = ev.getDamager().getWorld();

        if (entity1 instanceof Player) {
            Player defender = (Player) entity1;

            if ((entity2 instanceof Player)) {
                Player attacker = (Player) entity2;
                if (plugin.getGameManager().isSpectator(attacker)
                		|| plugin.getGameManager().isAdmin(attacker)
                        || plugin.getGameManager().getState() == SGGameState.LOBBY
                        || plugin.getGameManager().getState() == SGGameState.STARTING
                        || plugin.getGameManager().getState() == SGGameState.PRE_DEATHMATCH
                        || plugin.getGameManager().getState() == SGGameState.RESETTING
                        || !plugin.getGameManager().grace.isEmpty()) {
                    ev.setCancelled(true);
                } else {
                    PlayerDamage damage = new PlayerDamage();
                    damage.attackerName = attacker.getName();
                    damage.timeAttacked = System.currentTimeMillis();
                    plugin.getGameManager().damagecause.put(defender.getName(), damage);
                    // Send particle effect to all players in world.
                    if(plugin.getSettings().bloodIntensity > 0) {
                        Location hitLoc = new Location(defender.getWorld(),
                                defender.getLocation().getX(),
                                defender.getLocation().getY() + 0.5,
                                defender.getLocation().getZ());
                        for(int i = 0; i < plugin.getSettings().bloodIntensity; i++) {
                            world.playEffect(hitLoc, Effect.STEP_SOUND, Material.REDSTONE_WIRE.getId(), 20);
                        }
                    }
                }
            } else if (plugin.getGameManager().damagecause.containsKey(defender.getName())) {
                plugin.getGameManager().damagecause.remove(defender.getName());
            }
        }
    }
    
    @EventHandler(priority =  EventPriority.NORMAL)
    public void onEntityRegainHealth(EntityRegainHealthEvent ev) {
        Entity entity = ev.getEntity();
        World world = entity.getWorld();

        if (entity instanceof Player) {
            Player player = (Player) entity;
            if(player.getHealthScale() < 10) {
                Location hitLoc = new Location(player.getWorld(),
                        player.getLocation().getX(),
                        player.getLocation().getY() + 0.5,
                        player.getLocation().getZ());
                for(int i = 0; i < plugin.getSettings().bloodIntensity; i++) {
                    world.playEffect(hitLoc, Effect.STEP_SOUND, Material.REDSTONE_WIRE.getId());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageEvent ev) {
        if ((ev.getEntityType() == EntityType.PLAYER)
                && (ev.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
            Player defender = (Player) ev.getEntity();

            if (plugin.getGameManager().damagecause.containsKey(defender.getName())) {
                plugin.getGameManager().damagecause.remove(defender.getName());
            }
            if ((plugin.getGameManager().getState() == SGGameState.LOBBY)
                    || (plugin.getGameManager().getState() == SGGameState.STARTING)
                    || (plugin.getGameManager().getState() == SGGameState.PRE_DEATHMATCH)
                    || (plugin.getGameManager().getState() == SGGameState.RESETTING)
                    || !plugin.getGameManager().grace.isEmpty()) {
                ev.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(final PlayerDeathEvent ev) {
        ev.setDeathMessage(null);
        new BukkitRunnable() {
			public void run() {
				try {
					Object nmsPlayer = ev.getEntity().getClass().getMethod("getHandle").invoke(ev.getEntity());
					Object con = nmsPlayer.getClass().getDeclaredField("playerConnection").get(nmsPlayer);

					Class<?> EntityPlayer = Class.forName(nmsPlayer.getClass().getPackage().getName() + ".EntityPlayer");

					Field minecraftServer = con.getClass().getDeclaredField("minecraftServer");
					minecraftServer.setAccessible(true);
					Object mcserver = minecraftServer.get(con);

					Object playerlist = mcserver.getClass().getDeclaredMethod("getPlayerList").invoke(mcserver);
					Method moveToWorld = playerlist.getClass().getMethod("moveToWorld", EntityPlayer, int.class , boolean.class);
					moveToWorld.invoke(playerlist, nmsPlayer, 0, false);
					plugin.getGameManager().killPlayer(ev.getEntity(), ev.getEntity().getLocation());
				} catch (Exception ex) {
					ex.printStackTrace();
					plugin.getGameManager().killPlayer(ev.getEntity(), ev.getEntity().getLocation());
				}
			}
		}.runTaskLater(plugin , 2);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityExplode(EntityExplodeEvent ev) {
        ev.blockList().clear();
    }
    
    @EventHandler
    public void onFoodLevelChangeEvent(FoodLevelChangeEvent ev) {
        if(plugin.getGameManager().getState() == SGGameState.LOBBY) {
            if(ev.getEntity() instanceof Player) {
                ((Player) ev.getEntity()).setFoodLevel(20);
            }
            ev.setCancelled(true);
        }
        if (plugin.getGameManager().getState() != SGGameState.LOBBY) {
        	if (ev.getEntity() instanceof Player) {
        		if (plugin.getGameManager().isSpectator((Player) ev.getEntity())) {
        			((Player) ev.getEntity()).setFoodLevel(20);
        			ev.setCancelled(true);
        		}
        	}
        }
    }
    /*
     * == Entity ==
     *
     * CreatureSpawnEvent CreeperPowerEvent EntityBreakDoorEvent
     * EntityChangeBlockEvent EntityCombustByBlockEvent
     * EntityCombustByEntityEvent EntityCombustEvent EntityCreatePortalEvent
     * EntityDamageByBlockEvent EntityDamageByEntityEvent EntityDamageEvent
     * EntityDeathEvent EntityEvent EntityExplodeEvent EntityInteractEvent
     * EntityPortalEnterEvent EntityPortalEvent EntityPortalExitEvent
     * EntityRegainHealthEvent EntityShootBowEvent EntityTameEvent
     * EntityTargetEvent EntityTargetLivingEntityEvent EntityTeleportEvent
     * ExpBottleEvent ExplosionPrimeEvent FoodLevelChangeEvent ItemDespawnEvent
     * ItemSpawnEvent PigZapEvent PlayerDeathEvent PotionSplashEvent
     * ProjectileHitEvent ProjectileLaunchEvent SheepDyeWoolEvent
     * SheepRegrowWoolEvent SlimeSplitEvent
     *
     * == Vehicle ==
     *
     * VehicleBlockCollisionEvent VehicleCollisionEvent VehicleCreateEvent
     * VehicleDamageEvent VehicleDestroyEvent VehicleEnterEvent
     * VehicleEntityCollisionEvent VehicleEvent VehicleExitEvent
     * VehicleMoveEvent VehicleUpdateEvent
     */
}
