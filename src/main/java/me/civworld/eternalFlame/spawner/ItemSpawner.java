package me.civworld.eternalFlame.spawner;

import me.civworld.eternalFlame.config.Config;
import me.civworld.eternalFlame.fabric.ItemFabric;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import static me.civworld.eternalFlame.utils.Utils.findSafeSpawnNearPlayer;
import static me.civworld.eternalFlame.utils.Utils.isLucky;

public class ItemSpawner {
    private final Plugin plugin;
    private final Config config;
    private BukkitRunnable runnable = null;

    public ItemSpawner(Plugin plugin, Config config){
        this.plugin = plugin;
        this.config = config;
    }

    public void updateSpawnings(){
        if(runnable != null) runnable.cancel();

        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (!isLucky(config.get("spawner.chance", Integer.class))) continue;

                    ItemStack item = ItemFabric.getTitanShard(config.get("titan-event.need-amount-item", Integer.class));

                    int attempts = config.get("spawner.attempts", Integer.class);
                    int radiusX = config.get("spawner.radiusX", Integer.class);
                    int radiusY = config.get("spawner.radiusY", Integer.class);
                    Location spawnLocation = findSafeSpawnNearPlayer(player, attempts, radiusX, radiusY);
                    if (spawnLocation != null && item != null) {
                        player.getWorld().dropItemNaturally(spawnLocation, item);
                    }
                }
            }
        };

        int cooldown = config.get("cooldown-in-ticks", Integer.class);
        runnable.runTaskTimer(plugin, cooldown, cooldown);
    }
}