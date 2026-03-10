package me.civworld.eternalFlame;

import me.civworld.eternalFlame.circle.CircleManager;
import me.civworld.eternalFlame.command.EternalCommand;
import me.civworld.eternalFlame.config.Config;
import me.civworld.eternalFlame.event.TitanEvent;
import me.civworld.eternalFlame.spawner.ItemSpawner;
import me.civworld.eternalFlame.trait.WaypointTrait;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import ru.civworld.darkAPI.DarkAPI;

import java.util.ArrayList;
import java.util.List;

public final class EternalFlame extends JavaPlugin {
    private final List<NPC> npcs = new ArrayList<>();

    @Override
    public void onEnable() {
        Config config = new Config(this);
        config.loadConfig();

        DarkAPI.registerPlugin(this, config.get("plugin-prefix", String.class));

        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "test");
        npcs.add(npc);

        World world = getServer().getWorld("world");

        Location spawnLoc = new Location(world, 98.5, 69, -59.5, 0, 0);
        npc.spawn(spawnLoc);

        List<Location> points = new ArrayList<>();
        points.add(new Location(world, 97.5, 69, -59.5, 90, 0));
        points.add(new Location(world, 96.5, 69, -59.5, 90, 0));
        points.add(new Location(world, 94.5, 69, -59.5, 90, 0));
        points.add(new Location(world, 82.5, 69, -59.5, 90, 0));

        ItemSpawner itemSpawner = new ItemSpawner(this, config);
        itemSpawner.updateSpawnings();

        npc.addTrait(new WaypointTrait(points));
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Trait trait : npc.getTraits()) {
                trait.run();
            }
        }, 1L, 1L);

        TitanEvent titanEvent = new TitanEvent(this, config);
        titanEvent.updateHologram();
        CircleManager circleManager = new CircleManager(this, config, titanEvent);
        circleManager.updateCircleRound();

        getServer().getPluginManager().registerEvents(titanEvent, this);

        DarkAPI.setCommand("eternal", new EternalCommand(this, config, itemSpawner, circleManager, titanEvent));

        getLogger().info("Plugin is enabled.");
    }

    @Override
    public void onDisable() {
        for(NPC npc : npcs){
            CitizensAPI.getNPCRegistry().deregister(npc);
            npc.despawn();
            npc.destroy();
        }
    }
}
