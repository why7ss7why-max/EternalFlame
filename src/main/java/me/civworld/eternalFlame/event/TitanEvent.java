package me.civworld.eternalFlame.event;

import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.civworld.eternalFlame.config.Config;
import me.civworld.eternalFlame.meteor.Meteor;
import me.civworld.eternalFlame.manager.NPCManager;
import me.civworld.eternalFlame.type.ParkourDifficult;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import ru.civworld.darkAPI.DarkAPI;

import java.util.*;

public class TitanEvent implements Listener {
    private final Plugin plugin;
    private final Config config;
    public final NPCManager npcManager;

    public final HashMap<String, Long> lastGame = new HashMap<>();

    public Player player = null;

    public boolean allowGamemode = false;
    public boolean allowTeleport = false;
    public boolean allowStopspectate = false;
    public boolean allowMove = false;

    public int attempts = 3;

    public ParkourDifficult difficult = ParkourDifficult.SUPER_EASY;

    public final Location endLocation = new Location(Bukkit.getWorld("world"), 220.5, 65.5, -151.5);

    public Hologram hologram = null;

    public TitanEvent(Plugin plugin, Config config, NPCManager npcManager){
        this.plugin = plugin;
        this.config = config;
        this.npcManager = npcManager;
    }

    public void startGame(){
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if(player == null) return;

                World world = plugin.getServer().getWorld("world");
                if(world != null){
                    world.spawnParticle(Particle.VILLAGER_HAPPY, endLocation, 5);
                }

                Location loc = player.getLocation();

                if (!loc.getWorld().equals(endLocation.getWorld())) return;

                if (loc.distanceSquared(endLocation) < 1) {
                    player.sendMessage(DarkAPI.parse("<red>❖ <white>Вы <green>прошли <white>этот <yellow>уровень<white>!"));
                }
            }
        };

        runnable.runTaskTimer(plugin, 200L, 5L);

        Location cutLocation = config.get("titan-event.position-cut", Location.class).clone();
        cutLocation.setYaw(36);
        cutLocation.setPitch(-40);

        Tadpole tadpole = (Tadpole) cutLocation.getWorld().spawnEntity(cutLocation, EntityType.TADPOLE);
        tadpole.setGravity(false);
        tadpole.setAI(false);
        tadpole.setAgeLock(true);
        tadpole.setInvulnerable(true);
        tadpole.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, true, false));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            npcManager.startNpcTitan(this);
            tadpole.setPersistent(false);
            tadpole.remove();
        }, 130L);

        float startYaw = 36f;
        float endYaw = 80f;

        float startPitch = -40f;
        float endPitch = -10f;

        int totalTicks = 60;
        float yawStep = (endYaw - startYaw) / totalTicks;
        float pitchStep = (endPitch - startPitch) / totalTicks;

        float[] currentYaw = {startYaw};
        float[] currentPitch = {startPitch};

        new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                if (tick >= totalTicks) {
                    blindness(player);
                    this.cancel();
                    return;
                }

                currentYaw[0] += yawStep;
                currentPitch[0] += pitchStep;

                cutLocation.setYaw(currentYaw[0]);
                cutLocation.setPitch(currentPitch[0]);

                tadpole.teleport(cutLocation);

                tick++;
            }
        }.runTaskTimer(plugin, 60L, 1L);

        allowGamemode = true;
        allowStopspectate = true;
        allowTeleport = true;
        player.setGameMode(GameMode.SPECTATOR);
        player.setSpectatorTarget(tadpole);
        allowGamemode = false;
        allowStopspectate = false;
        allowTeleport = false;
        player.sendActionBar(DarkAPI.parse(""));
        lastGame.put(player.getName(), System.currentTimeMillis());

        Location start = config.get("titan-event.meteorite.pos1", Location.class).clone();
        Location end = config.get("titan-event.meteorite.pos2", Location.class).clone();

        Meteor meteor = new Meteor(plugin);
        meteor.spawnMeteor(start, end);
    }

    private void blindness(Player player){
        allowGamemode = true;
        allowStopspectate = true;
        allowTeleport = true;
        player.setSpectatorTarget(null);
        player.setGameMode(GameMode.ADVENTURE);
        allowGamemode = false;
        allowStopspectate = false;
        allowTeleport = false;
        Location newLoc = player.getLocation();
        newLoc.setYaw(89.9f);
        newLoc.setPitch(-0.1f);
        newLoc.add(0, -1.5, 0);
        allowTeleport = true;
        player.teleport(newLoc);
        allowTeleport = false;
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 255, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 255, false, false, false));
        Bukkit.getScheduler().runTaskLater(plugin, () -> player.removePotionEffect(PotionEffectType.BLINDNESS), 40L);
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event){
        Player player = event.getPlayer();
        if(this.player == null) return;
        if(player != this.player) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event){
        Player player = event.getPlayer();
        if(this.player == null) return;
        if(player != this.player) return;

        if(event.getFrom().distance(event.getTo()) == 0) return;

        if(allowMove) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onChangeGameMode(PlayerGameModeChangeEvent event){
        Player player = event.getPlayer();
        if(this.player == null) return;
        if(player != this.player) return;

        if(allowGamemode) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event){
        Player player = event.getPlayer();
        if(this.player == null) return;
        if(player != this.player) return;

        if(allowTeleport) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onStop(PlayerStopSpectatingEntityEvent event){
        Player player = event.getPlayer();
        if(this.player == null) return;
        if(player != this.player) return;

        if(allowStopspectate) return;

        event.setCancelled(true);
    }

    public void updateHologram(){
        if(hologram == null) createHologram();

        DHAPI.setHologramLine(hologram, 1, "Статус: " + statusToString());
        DHAPI.setHologramLine(hologram, 3, "Для старта <#FF0000>необходимо");
        DHAPI.setHologramLine(hologram, 4, "иметь <#3446eb>x" + config.get("titan-event.need-amount-item", Integer.class) + " Осколок Титана");
    }

    private void createHologram(){
        if(DHAPI.getHologram("titan-event-holo") != null){
            DHAPI.removeHologram("titan-event-holo");
        }

        hologram = DHAPI.createHologram("titan-event-holo", config.get("titan-event.position", Location.class).add(0, 1.75, 0));
        DHAPI.addHologramLine(hologram, "<#3446eb>Падение Титана");
        DHAPI.addHologramLine(hologram, "Статус: " + statusToString());
        DHAPI.addHologramLine(hologram, "");
        DHAPI.addHologramLine(hologram, "Для старта <#FF0000>необходимо");
        DHAPI.addHologramLine(hologram, "иметь <#3446eb>x" + config.get("titan-event.need-amount-item", Integer.class) + " Осколок Титана");

        hologram.showAll();
    }

    public void setPlayer(Player player){
        player.sendMessage(DarkAPI.parse("<prefix>Вы <green>вступили<white> в <blue>ивент<white>!"));
        this.player = player;

        startGame();

        updateHologram();
    }

    private String statusToString(){
        if(player == null) return "<#808080>Не активен";
        return "<#FF0000>Активен";
    }
}