package net.shockverse.survivalgames.extras;

import com.google.common.base.Preconditions;
import net.shockverse.survivalgames.core.Tools;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * @description Utilities for creating ItemStacks. Converting to ItemStackFactory in next update, this code is messy and ugly
 * @author LegitModern
 */
public class ItemUtils {

    /**
     * Create an item stack
     *
     * @param material Desired material for ItemStack
     * @return Created ItemStack
     */
    public static ItemStack createItemStack(Material material) {
        Preconditions.checkNotNull(material, "ItemStack material is null");

        return new ItemStack(material);
    }

    /**
     * Create an item stack
     *
     * @param material Desired material for ItemStack
     * @param amount   Amount of items in ItemStack
     * @return Created ItemStack
     */
    public static ItemStack createItemStack(Material material, int amount) {
        Preconditions.checkNotNull(material, "ItemStack material is null");
        Preconditions.checkNotNull(amount, "ItemStack amount is null");
        Preconditions.checkArgument(amount >= 0, "ItemStack amount must be greater than 0");

        ItemStack itemStack = createItemStack(material);
        itemStack.setAmount(amount);
        return itemStack;
    }

    /**
     * Create an item stack
     *
     * @param name     Desired name for ItemStack
     * @param material Desired material for ItemStack
     * @return Created ItemStack
     */
    public static ItemStack createItemStack(String name, Material material) {
        Preconditions.checkNotNull(name, "ItemStack name is null");
        Preconditions.checkNotNull(material, "ItemStack material is null");

        ItemStack itemStack = createItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(Tools.parseColors(name));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    /**
     * Create an item stack
     *
     * @param name     Desired name for ItemStack
     * @param material Desired material for ItemStack
     * @param amount   Amount of items in ItemStack
     * @return Created ItemStack
     */
    public static ItemStack createItemStack(String name, Material material, int amount) {
        Preconditions.checkNotNull(name, "ItemStack name is null");
        Preconditions.checkNotNull(material, "ItemStack material is null");
        Preconditions.checkNotNull(amount, "ItemStack amount is null");
        Preconditions.checkArgument(amount >= 0, "ItemStack amount must be greater than 0");

        ItemStack itemStack = createItemStack(name, material);
        itemStack.setAmount(amount);
        return itemStack;
    }

    /**
     * Create an item stack
     *
     * @param name     Desired name for ItemStack
     * @param lore     Desired lore for ItemStack
     * @param material Desired material for ItemStack
     * @return Created ItemStack
     */
    public static ItemStack createItemStack(String name, List<String> lore, Material material) {
        Preconditions.checkNotNull(name, "ItemStack name is null!");
        Preconditions.checkNotNull(material, "ItemStack material is null!");

        ItemStack itemStack = createItemStack(name, material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (lore != null) {
            List<String> newLore = new ArrayList<String>();
            for (String loreLine : lore)  {
                newLore.add(Tools.parseColors(loreLine));
            }
            itemMeta.setLore(newLore);
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    /**
     * Create an item stack
     *
     * @param name     Desired name for ItemStack
     * @param lore     Desired lore for ItemStack
     * @param material Desired material for ItemStack
     * @param amount   Amount of items in ItemStack
     * @return Created ItemStack
     */
    public static ItemStack createItemStack(String name, List<String> lore, Material material, int amount) {
        Preconditions.checkNotNull(name, "ItemStack name is null");
        Preconditions.checkNotNull(lore, "ItemStack lore is null");
        Preconditions.checkNotNull(material, "ItemStack material is null");
        Preconditions.checkNotNull(amount, "ItemStack amount is null");
        Preconditions.checkArgument(amount >= 0, "ItemStack amount must be greater than 0");

        ItemStack itemStack = createItemStack(name, lore, material);
        itemStack.setAmount(amount);
        return itemStack;
    }

    public static enum LeatherArmorType {HELMET, CHESTPLATE, LEGGINGS, BOOTS}

    /**
     * Create a leather item stack
     *
     * @param type  Desired leather armor type
     * @param color Desired leather armor color
     * @return Created ItemStack
     */
    public static ItemStack createLeatherItemStack(LeatherArmorType type, Color color) {
        Preconditions.checkNotNull(type, "ItemStack type is null");
        Preconditions.checkNotNull(color, "ItemStack color is null");

        ItemStack itemStack = createItemStack(type == LeatherArmorType.HELMET ? Material.LEATHER_HELMET : type == LeatherArmorType.CHESTPLATE ? Material.LEATHER_CHESTPLATE : type == LeatherArmorType.LEGGINGS ? Material.LEATHER_LEGGINGS : Material.LEATHER_BOOTS);
        LeatherArmorMeta meta = (LeatherArmorMeta) itemStack.getItemMeta();
        meta.setColor(color);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    /**
     * Create a leather item stack
     *
     * @param name  Desired name for ItemStack
     * @param type  Desired leather armor type
     * @param color Desired leather armor color
     * @return Created ItemStack
     */
    public static ItemStack createLeatherItemStack(String name, LeatherArmorType type, Color color) {
        Preconditions.checkNotNull(name, "ItemStack name is null");
        Preconditions.checkNotNull(type, "ItemStack type is null");
        Preconditions.checkNotNull(color, "ItemStack color is null");

        ItemStack itemStack = createLeatherItemStack(type, color);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(Tools.parseColors(name));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    /**
     * Create a leather item stack
     *
     * @param name  Desired name for ItemStack
     * @param lore  Desired lore for ItemStack
     * @param type  Desired leather armor type
     * @param color Desired leather armor color
     * @return Created ItemStack
     */
    public static ItemStack createLeatherItemStack(String name, List<String> lore, LeatherArmorType type, Color color) {
        Preconditions.checkNotNull(name, "ItemStack name is null");
        Preconditions.checkNotNull(lore, "ItemStack lore is null");
        Preconditions.checkNotNull(type, "ItemStack type is null");
        Preconditions.checkNotNull(color, "ItemStack color is null");

        ItemStack itemStack = createLeatherItemStack(name, type, color);
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> newLore = new ArrayList<String>();
        for (String loreLine : lore) {
            newLore.add(Tools.parseColors(loreLine));
        }

        itemMeta.setLore(newLore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
