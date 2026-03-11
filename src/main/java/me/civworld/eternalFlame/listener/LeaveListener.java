package me.civworld.eternalFlame.listener;

import me.civworld.eternalFlame.config.Config;
import me.civworld.eternalFlame.event.TitanEvent;
import me.civworld.eternalFlame.npc.NPCManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

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

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();

        if(titanEvent.playersInCircle.remove(player)) plugin.getLogger().info("Player " + player.getName() + " was in players in circle, removing him");
        if(titanEvent.playersInGame.remove(player)) plugin.getLogger().info("Player " + player.getName() + " was in players in game, removing him");
        if(titanEvent.playersBlindness.remove(player)) plugin.getLogger().info("Player " + player.getName() + " was in players in blindness, removing him");
    }
}