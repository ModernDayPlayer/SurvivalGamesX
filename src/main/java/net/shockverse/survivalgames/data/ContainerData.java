package net.shockverse.survivalgames.data;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

/**
 *
 * @author Tagette
 */
public class ContainerData {
    
    public boolean enabled = true;
    public String title = "Chest";
    public Material material = null;
    public Byte data = 0;
    public List<RewardData> rewards = new ArrayList<RewardData>();   
    public int minChestRewards = 0;
    public int maxChestRewards = 15;
}
