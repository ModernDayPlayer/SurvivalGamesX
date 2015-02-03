package net.shockverse.survivalgames.data;

import org.bukkit.Material;

/*
 * Stores data about a reward to be used by survival games.
 *   @author Duker02
 */
public class RewardData
{
  public Material item = Material.DIRT;
  public Byte data = 0;
  public int min = 1;
  public int max = 1;
  public int rarity = 100;
}