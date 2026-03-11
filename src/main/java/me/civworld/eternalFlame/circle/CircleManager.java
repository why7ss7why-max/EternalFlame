package me.civworld.eternalFlame.circle;

import me.civworld.eternalFlame.EternalFlame;
import me.civworld.eternalFlame.config.Config;
import me.civworld.eternalFlame.event.TitanEvent;
import me.civworld.eternalFlame.type.EventStatus;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.civworld.darkAPI.DarkAPI;

import java.util.ArrayList;

public class CircleManager {
    private final Plugin plugin;
    private final Config config;
    private final TitanEvent titanEvent;
    private BukkitRunnable runnable = null;

    public CircleManager(Plugin plugin, Config config, TitanEvent titanEvent){
        this.plugin = plugin;
        this.config = config;
        this.titanEvent = titanEvent;
    }

    public void updateCircleRound(){
        if(runnable != null) runnable.cancel();

        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                Location centerCircle = config.get("titan-event.position", Location.class);
                if(centerCircle == null) return;

                int radius = config.get("titan-event.circle-radius", Integer.class);
                if(radius == 0) return;

                for(Player player : new ArrayList<>(titanEvent.playersInCircle)){
                    if(player.getLocation().distanceSquared(centerCircle) > radius){
                        titanEvent.removePlayer(player);
                    }
                }

                for(Player player : centerCircle.getNearbyPlayers(radius)){
                    if(player.getLocation().distanceSquared(centerCircle) > radius) continue;
                    if(titanEvent.eventStatus == EventStatus.RUNNING){
                        player.sendActionBar(DarkAPI.parse("Ивент <gray>уже <red>активен<white>!"));
                        continue;
                    }
                    titanEvent.updateAddPlayer(player);

                    for(int i = 0; i < player.getInventory().getSize(); i++){
                        ItemStack item = player.getInventory().getItem(i);
                        if(item == null) continue;

                        if(item.getType() != Material.DISC_FRAGMENT_5) continue;

                        NamespacedKey namespacedKey = new NamespacedKey(EternalFlame.getPlugin(EternalFlame.class), "eternalflame");
                        String value = item.getItemMeta().getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
                        if(value == null) continue;
                        if(!value.equals("titan_shard")) continue;

                        if(item.getAmount() >= config.get("titan-event.need-amount-item", Integer.class)){
                            if(titanEvent.eventStatus == EventStatus.OFFLINE){
                                item.setAmount(item.getAmount() - config.get("titan-event.need-amount-item", Integer.class));
                                player.getInventory().setItem(i, item);
                                titanEvent.setStatus(EventStatus.PAID);
                                player.sendMessage(DarkAPI.parse("<prefix>Вы <green>оплатили <white>старт <yellow>игры<white>!"));
                            }
                        }
                    }
                }
            }
        };

        runnable.runTaskTimer(plugin, 5L, 5L);
    }
}