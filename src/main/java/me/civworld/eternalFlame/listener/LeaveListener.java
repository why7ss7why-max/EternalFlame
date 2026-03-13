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
import org.bukkit.potion.PotionEffectType;

import static me.civworld.eternalFlame.utils.Utils.removePlayerScoreboard;

public class LeaveListener implements Listener {
    private final Config config;
    private final TitanEvent titanEvent;
    private final NPCManager npcManager;

    public LeaveListener(Config config, TitanEvent titanEvent, NPCManager npcManager){
        this.config = config;
        this.titanEvent = titanEvent;
        this.npcManager = npcManager;
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();

        if(!(titanEvent.playersInCircle.remove(player)
                | titanEvent.playersInGame.remove(player)
                | titanEvent.playerParkourists.remove(player)
                | titanEvent.playersInBlindness.remove(player))) {
            return;
        }
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.SLOW);

        removePlayerScoreboard(player);

        Location spawnLocation = config.get("titan-event.tp-on-leave", Location.class);
        if(spawnLocation != null){
            player.teleport(spawnLocation);
        }

        if(titanEvent.playersInGame.isEmpty() && titanEvent.playersInBlindness.isEmpty() && titanEvent.playerParkourists.isEmpty()){
            npcManager.forceShutdown();
        }
    }
}