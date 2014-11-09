package net.shockverse.survivalgames.extras;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Basic various utilities for handling reflection.
 * @author LegitModern
 */
public class ReflectionUtil {

    private ReflectionUtil() { }

    private static String version;

    static {
        String name = Bukkit.getServer().getClass().getPackage().getName(), mcVersion = name.substring(name.lastIndexOf('.') + 1);
        version = mcVersion + ".";
    }

    /**
     * Get a class in NMS
     *
     * @param target Name of class
     * @return Class in NMS
     */
    public static Class<?> getNMSClass(String target) {
        String className = "net.minecraft.server." + version + target;
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return clazz;
    }

    /**
     * Get a class in CraftBukkit via reflection
     *
     * @param target Name of class
     * @return Class in CraftBukkit
     */
    public static Class<?> getCraftClass(String target) {
        String className = "org.bukkit.craftbukkit." + version + target;
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return clazz;
    }

    /**
     * Get a method with explicit parameters
     *
     * @param clazz  Class to get method from
     * @param method Name of method to get
     * @param params Parameters for method
     * @return Method from class
     */
    public static Method getMethod(Class<?> clazz, String method, Class<?>... params) {
        try {
            return clazz.getMethod(method, params);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get a declared method with explicit parameters
     *
     * @param clazz  Class to get method from
     * @param method Name of method to get
     * @param params Parameters for method
     * @return Declared method from class
     */
    public static Method getDeclaredMethod(Class<?> clazz, String method, Class<?>... params) {
        try {
            return clazz.getDeclaredMethod(method, params);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get a field from the class
     *
     * @param clazz     Class to get method from
     * @param fieldName Name of field to get
     * @return Field from class with name
     */
    public static Field getField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getField(fieldName);
        } catch (SecurityException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get a declared field from the class
     *
     * @param clazz     Class to get method from
     * @param fieldName Name of field to get
     * @return Field from class with name
     */
    public static Field getDeclaredField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (SecurityException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Set the value of a field in a class
     *
     * @param instance  Instance of object to set
     * @param fieldName Name of field to set value for
     * @param value     Value to set on field
     */
    public static void setValue(Object instance, String fieldName, Object value) {
        try {
            Field field = getDeclaredField(instance.getClass(), fieldName);
            field.setAccessible(true);
            field.set(instance, value);
            field.setAccessible(!field.isAccessible());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a packet via reflection
     *
     * @param player Player to send packet to
     * @param packet Packet to send
     */
    public static void sendPacket(Player player, Object packet) {
        try {
            Object nmsPlayer = getHandle(player);
            Field conField = getField(nmsPlayer.getClass(), "playerConnection");
            Object connection = conField.get(nmsPlayer);
            Method packetMethod = getMethod(connection.getClass(), "sendPacket", getNMSClass("Packet"));
            packetMethod.invoke(connection, packet);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the NMS handle of an entity
     *
     * @param entity Entity to get handle from
     * @return Entity's NMS handle
     */
    public static Object getHandle(Object entity) {
        Object nmsEntity = null;
        Method entityGetHandle = getMethod(entity.getClass(), "getHandle");
        try {
            nmsEntity = entityGetHandle.invoke(entity);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return nmsEntity;
    }
}
