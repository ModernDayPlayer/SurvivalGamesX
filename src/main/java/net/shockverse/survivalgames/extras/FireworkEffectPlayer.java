package net.shockverse.survivalgames.extras;

import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.lang.reflect.Constructor;

/**
 * @author Vexil
 */
public class FireworkEffectPlayer {

    private FireworkEffectPlayer() {
    }

    private static Constructor<?> PACKET_PLAY_OUT_ENTITY_STATUS;

    static {
        try {
            PACKET_PLAY_OUT_ENTITY_STATUS = ReflectionUtil.getNMSClass("PacketPlayOutEntityStatus").getConstructor(ReflectionUtil.getNMSClass("Entity"), byte.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Play the fireworkEffect packet for a single player
     *
     * @param player         Player to play fireworkEffect for
     * @param location       Location to play firework effect at
     * @param fireworkEffect FireworkEffect to play
     */
    public static void playToPlayer(Player player, Location location, FireworkEffect fireworkEffect) {
        ReflectionUtil.sendPacket(player, makePacket(location, fireworkEffect));
    }

    /**
     * Play a firework effect at a location
     *
     * @param location       Location to play firework effect at
     * @param fireworkEffect FireworkEffect to play
     */
    public static void playToLocation(Location location, FireworkEffect fireworkEffect) {
        for (Entity entity : location.getWorld().getEntities()) {
            if (entity instanceof Player) {
                if (entity.getLocation().distanceSquared(location) <= 60 * 60) {
                    ReflectionUtil.sendPacket((Player) entity, makePacket(location, fireworkEffect));
                }
            }
        }
    }

    /**
     * Make a packet object
     *
     * @param location       Location to play firework effect at
     * @param fireworkEffect FireworkEffect to play
     * @return Packet constructed by the parameters
     */
    private static Object makePacket(Location location, FireworkEffect fireworkEffect) {
        try {
            Firework firework = location.getWorld().spawn(location, Firework.class);
            FireworkMeta data = firework.getFireworkMeta();
            data.clearEffects();
            data.setPower(1);
            data.addEffect(fireworkEffect);
            firework.setFireworkMeta(data);
            Object nmsFirework = ReflectionUtil.getHandle(firework);
            firework.remove();
            return PACKET_PLAY_OUT_ENTITY_STATUS.newInstance(nmsFirework, (byte) 17);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
