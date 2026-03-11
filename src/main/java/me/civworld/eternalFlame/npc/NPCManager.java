package me.civworld.eternalFlame.npc;

import me.civworld.eternalFlame.config.Config;
import me.civworld.eternalFlame.event.TitanEvent;
import me.civworld.eternalFlame.trait.WaypointTrait;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.HologramTrait;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tadpole;
import org.bukkit.plugin.Plugin;
import ru.civworld.darkAPI.DarkAPI;

import java.util.Iterator;

public class NPCManager {
    private final Plugin plugin;
    private final Config config;

    public NPCManager(Plugin plugin, Config config){
        this.plugin = plugin;
        this.config = config;
    }

    public void startNpcTitan(TitanEvent titanEvent){
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "test");

        World world = plugin.getServer().getWorld("world");
        SkinTrait trait = npc.getOrAddTrait(SkinTrait.class);

        trait.setSkinName("Fakebobby");
        trait.setShouldUpdateSkins(true);
        npc.spawn(new Location(world, 102.295, 64, -93.455, -90.2f, -0.3f));

        LookClose lookClose = new LookClose();
        lookClose.setRange(20);
        lookClose.setPerPlayer(true);
        lookClose.lookClose(true);
        npc.addTrait(lookClose);

        HologramTrait hologramTrait = new HologramTrait();
        npc.addTrait(hologramTrait);

        npc.setName("&#3446ebТитан");
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            hologramTrait.addLine("&7Хах, кто-то посмел бросить мне вызов?");

            for(Player player : titanEvent.playersBlindness){
                player.sendMessage(DarkAPI.parse("<#3446eb>Титан <gray>» <white>Хах, кто-то посмел бросить мне вызов?"));
            }
        }, 50L);
        Bukkit.getScheduler().runTaskLater(plugin, hologramTrait::clear, 130L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            hologramTrait.addLine("&7Как такие жалкие существа, посмели меня потревожить?");

            for(Player player : titanEvent.playersBlindness){
                player.sendMessage(DarkAPI.parse("<#3446eb>Титан <gray>» <white>Как такие жалкие существа, посмели меня потревожить?"));
            }
        }, 160L);
        Bukkit.getScheduler().runTaskLater(plugin, hologramTrait::clear, 240L);
        
//        npc.addTrait(hologramTrait);

//        List<Location> points = new ArrayList<>();
//        points.add(new Location(world, 97.5, 69, -59.5, 90, 0));
//        points.add(new Location(world, 96.5, 69, -59.5, 90, 0));
//        points.add(new Location(world, 94.5, 69, -59.5, 90, 0));
//        points.add(new Location(world, 82.5, 69, -59.5, 90, 0));
//
//        npc.addTrait(new WaypointTrait(points));
//
//
//        Bukkit.getScheduler().runTaskTimer(this, () -> {
//            for (Trait trait : npc.getTraits()) {
//                trait.run();
//            }
//        }, 1L, 1L);
    }
}