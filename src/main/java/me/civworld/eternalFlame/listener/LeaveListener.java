package me.civworld.eternalFlame.listener;

import me.civworld.eternalFlame.config.Config;
import me.civworld.eternalFlame.event.TitanEvent;
import me.civworld.eternalFlame.manager.NPCManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

public class LeaveListener implements Listener {
    private final Plugin plugin;
    private final Config config;
    private final TitanEvent titanEvent;
    private final NPCManager npcManager;

    public LeaveListener(Plugin plugin, Config config, TitanEvent titanEvent, NPCManager npcManager){
        this.plugin = plugin;
        this.config = config;
        this.titanEvent = titanEvent;
        this.npcManager = npcManager;
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();

        if(!(titanEvent.playersInCircle.remove(player)
                | titanEvent.playersInGame.remove(player))) {
            return;
        }
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.SLOW);

        Location spawnLocation = config.get("titan-event.tp-on-leave", Location.class);
        if(spawnLocation != null){
            player.teleport(spawnLocation);
        }

        if(titanEvent.playersInGame.isEmpty()){
            npcManager.forceShutdown();
        }
    }
}