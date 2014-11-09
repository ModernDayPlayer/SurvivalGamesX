/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.shockverse.survivalgames;

import java.io.File;
import java.io.FileWriter;
import net.shockverse.survivalgames.core.Logger;

/**
 *
 * @author LegitModern, Tagette
 */
public class ExampleCreator {

    private SurvivalGames plugin;
    
    private String shStart = "#!/bin/bash\nwhile true ; do\n\necho \"\"\necho \"            #########################################\"\necho \"            ##        Auto Restart Script          ##\"\necho \"            ##                                     ##\"\necho \"            ##    Press Ctrl + C ONLY when this    ##\"\necho \"            ##   message shows to stop the loop.   ##\"\necho \"            #########################################\"\necho \"Starting in 5 seconds...\"\nsleep 1\necho \"Starting in 4 seconds...\"\nsleep 1\necho \"Starting in 3 seconds...\"\nsleep 1\necho \"Starting in 2 seconds...\"\nsleep 1\necho \"Starting in 1 seconds...\"\nsleep 1\n\njava -Xms1024M -Xmx4096M -jar craftbukkit.jar --log-strip-color\n\ndone";
    private String batStart = "@ECHO OFF\n:begin\n\necho             #########################################\necho             ##        Auto Restart Script          ##\necho             ##                                     ##\necho             ##    Press Ctrl + C ONLY when this    ##\necho             ##   message shows to stop the loop.   ##\necho             #########################################\necho Starting in 5 seconds...\nping 192.0.2.2 -n 1 -w 1000 > nul\necho Starting in 4 seconds...\nping 192.0.2.2 -n 1 -w 1000 > nul\necho Starting in 3 seconds...\nping 192.0.2.2 -n 1 -w 1000 > nul\necho Starting in 2 seconds...\nping 192.0.2.2 -n 1 -w 1000 > nul\necho Starting in 1 seconds...\nping 192.0.2.2 -n 1 -w 1000 > nul\n\njava -Xmx1024M -jar craftbukkit.jar --log-strip-color\ngoto begin";
    private String arenaCfg  = "# -- Worlds --\n\nlobby {\n    worldFolder = 'lobby'\n    lobbyName = 'Lobby'\n    lobbyTime = '240'   # Lobby seconds.\n    stateMessageTime = '40'   # How often the state message appears.\n    adminChat = '&RED[ADMIN] %tdisplay%: %message%'   # The format of the chat for admins.\n    spectatorChat = '&GRAY[SPECTATOR] %tdisplay%: %message%'   # The format of the chat for spectators.\n    tributeChat = '&DARK_GREEN[%points%] %tdisplay%: %message%'   # The format of the chat for tributes.\n    lobbySpawn = '0,0,0'   # The spawn location for the lobby.\n}\nworlds {\n    arena01 {   # Your world folder name.\n        arenaName = 'Survival Games'\n        enabled = 'true'\n        graceTime = '30'   # Grace period seconds.\n        gameCountdown = '60'   # Countdown seconds before game begins.\n        gameTime = '20'   # Game minutes.\n        deathMatchCountdown = '60'   # Countdown before deathmatch.\n        deathMatchTime = '300'   # Death match seconds.\n        minStartTributes = '6'   # The minimum amount of tributes needed to start the game.\n        minDMTributes = '3'   # The amount of tributes that need to be left for the deathmatch to start.\n        winPoints = '100'\n        killPoints = '10'\n        killPercent = '0'   # This amount will be rewarded from a player you killed.\n        refillWorldTime = '17000'   # The minecraft world time that chests will be refilled. (In ticks)\n        killDMRun = 'false'   # Kills the tribute when they run away.\n        dmRange = '35.0'   # The distance in blocks before the player is teleported or killed in deathmatch.\n        adminChat = '&RED[ADMIN] %tdisplay%: %message%'   # The format of the chat for admins.\n        spectatorChat = '&GRAY[SPECTATOR] %tdisplay%: %message%'   # The format of the chat for spectators.\n        tributeChat = '&DARK_GREEN[%points%] %tdisplay%: %message%'   # The format of the chat for tributes.\n    }\n}\n";
    private String rewardCfg = "# -- Rewards --\n\nlighterUses = '3'   # The number of uses a lighter has left.\n\ncontainers {\n    chest {\n        enabled = 'true'\n        name = 'Chest'   # The title that appears for the inventory.\n        minChestRewards = '3'   # The min amount of rewards that can be in a chest.\n        maxChestRewards = '8'   # The max amount of rewards that can be in a chest.\n        # Item/Id = rarity x amount (or min, max)\n        rewards {\n            rawfish = '40'\n            leatherboots = '40'\n            leatherchestplate = '40'\n            leatherhelmet = '40'\n            leatherleggings = '40'\n            ironboots = '10'\n            ironchestplate = '10'\n            ironhelmet = '10'\n            ironleggings = '10'\n            woodsword = '95'\n            stonesword = '80'\n            ironsword = '10'\n            enderpearl = '20 x 1, 2'\n            diamond = '2 x 1'\n            stick = '75 x 1, 3'\n            arrow = '70 x 1, 5'\n            wheat = '80 x 1, 2'\n            apple = '65 x 1, 3'\n            goldenapple = '40'\n            cookedfish = '50 x 1, 2'\n            fishingrod = '40'\n            melon = '90 x 2, 4'\n            rawbeef = '65 x 1, 2'\n            cookedbeef = '60'\n            bread = '75 x 1, 2'\n            bow = '85'\n            string = '85 x 1, 3'\n            compass = '70'\n            carrotitem = '65 x 1, 2'\n            bakedpotato = '60'\n            cookie = '65 x 1, 2'\n            pumpkinpie = '50'\n            ironingot = '20'\n            goldingot = '20'\n            flintandsteel = '5'\n        }\n    }\n}\n";
    
    public ExampleCreator(SurvivalGames instance) {
        plugin = instance;
    }
    
    public void Create() {
        if(plugin.getSettings().createExamples) {
            File exampleFolder = new File(plugin.getDataFolder().getAbsolutePath() + "/Examples/");
            if(!exampleFolder.exists())
                exampleFolder.mkdirs();
            try {
                File shFile = new File(exampleFolder.getAbsolutePath() + "/SurvivalGames.sh");
                if(shFile.exists())
                    shFile.delete();
                shFile.createNewFile();
                FileWriter fw = new FileWriter(shFile);
                fw.write(shStart);
                fw.flush();
                fw.close();
                
                File batFile = new File(exampleFolder.getAbsolutePath() + "/SurvivalGames.bat");
                if(batFile.exists())
                    batFile.delete();
                batFile.createNewFile();
                fw = new FileWriter(batFile);
                fw.write(batStart);
                fw.flush();
                fw.close();
                
                File arenaFile = new File(exampleFolder.getAbsolutePath() + "/Ex-Arenas.cfg");
                if(arenaFile.exists())
                    arenaFile.delete();
                arenaFile.createNewFile();
                fw = new FileWriter(arenaFile);
                fw.write(arenaCfg);
                fw.flush();
                fw.close();
                
                File rewardFile = new File(exampleFolder.getAbsolutePath() + "/Ex-Rewards.cfg");
                if(rewardFile.exists())
                    rewardFile.delete();
                rewardFile.createNewFile();
                fw = new FileWriter(rewardFile);
                fw.write(rewardCfg);
                fw.flush();
                fw.close();
                
                Logger.info("Example files created.");
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
}
