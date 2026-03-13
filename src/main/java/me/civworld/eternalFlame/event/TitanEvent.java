package me.civworld.eternalFlame.event;

import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.civworld.eternalFlame.config.Config;
import me.civworld.eternalFlame.meteor.Meteor;
import me.civworld.eternalFlame.manager.NPCManager;
import me.civworld.eternalFlame.type.EventStatus;
import me.civworld.eternalFlame.type.ParkourDifficult;
import me.civworld.eternalFlame.utils.Utils;
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
    public EventStatus eventStatus = EventStatus.OFFLINE;

    public final HashMap<String, Long> lastGame = new HashMap<>();

    public final LinkedHashSet<Player> playersInCircle = new LinkedHashSet<>();
    public final LinkedHashSet<Player> playersInGame = new LinkedHashSet<>();
    public final LinkedHashSet<Player> playersInBlindness = new LinkedHashSet<>();

    public final LinkedHashSet<Player> playerParkourists = new LinkedHashSet<>();

    public final HashMap<Player, Integer> playerAttempts = new HashMap<>();
    public final HashMap<Player, Boolean> playerDone = new HashMap<>();

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
                if(playerParkourists.isEmpty()) return;

                World world = plugin.getServer().getWorld("world");
                if(world != null){
                    world.spawnParticle(Particle.VILLAGER_HAPPY, endLocation, 5);
                }

                for(Player player : playerParkourists){
                    if(player.getLocation().distance(endLocation) < 1 && !playerDone.getOrDefault(player, false)){
                        player.sendMessage(DarkAPI.parse("<red>❖ <white>Вы <green>прошли <white>этот <yellow>уровень<white>!"));
                        playerDone.put(player, true);
                        for(Player target : playerParkourists){
                            if(player != target){
                                player.showPlayer(plugin, target);
                            }
                        }
                    }
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
                    for (Player p1 : playersInGame) {
                        for (Player p2 : playersInGame) {
                            if (!p1.equals(p2)) {
                                p1.hidePlayer(plugin, p2);
                                p2.hidePlayer(plugin, p1);
                            }
                        }
                    }

                    for (Player player : new ArrayList<>(playersInGame)) {
                        blindness(player);
                    }
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

        for (Player player : new ArrayList<>(playersInCircle)) {
            playersInCircle.remove(player);
            player.setGameMode(GameMode.SPECTATOR);
            player.setSpectatorTarget(tadpole);
            playersInGame.add(player);
            player.sendActionBar(DarkAPI.parse(""));
            lastGame.put(player.getName(), System.currentTimeMillis());
        }

        Location start = config.get("titan-event.meteorite.pos1", Location.class).clone();
        Location end = config.get("titan-event.meteorite.pos2", Location.class).clone();

        Meteor meteor = new Meteor(plugin);
        meteor.spawnMeteor(start, end);

        setStatus(EventStatus.RUNNING);
    }

    private void blindness(Player player){
        playersInGame.remove(player);
        playersInBlindness.add(player);
        player.setSpectatorTarget(null);
        player.setGameMode(GameMode.ADVENTURE);
        Location newLoc = player.getLocation();
        newLoc.setYaw(89.9f);
        newLoc.setPitch(-0.1f);
        newLoc.add(0, -1.5, 0);
        player.teleport(newLoc);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 255, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 255, false, false, false));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            playersInGame.add(player);
            playersInBlindness.remove(player);
        }, 40L);
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event){
        Player player = event.getPlayer();
        if(!playersInGame.contains(player) && !playersInBlindness.contains(player)) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event){
        Player player = event.getPlayer();
        if(!playersInGame.contains(player) && !playersInBlindness.contains(player)) return;

        if(event.getFrom().distance(event.getTo()) == 0) return;

        if(playerParkourists.contains(player)) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onChangeGameMode(PlayerGameModeChangeEvent event){
        Player player = event.getPlayer();
        if(!playersInGame.contains(player)) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event){
        Player player = event.getPlayer();
        if(!playersInGame.contains(player)) return;

        if(event.getCause() == PlayerTeleportEvent.TeleportCause.UNKNOWN) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onStop(PlayerStopSpectatingEntityEvent event){
        Player player = event.getPlayer();
        if(!playersInGame.contains(player)) return;

        event.setCancelled(true);
    }

    public void updateHologram(){
        if(hologram == null) createHologram();

        DHAPI.setHologramLine(hologram, 1, "Статус: " + statusToString(eventStatus));
        if(eventStatus == EventStatus.OFFLINE){
            DHAPI.setHologramLine(hologram, 3, "Для старта <#FF0000>необходимо");
            DHAPI.setHologramLine(hologram, 4, "иметь <#3446eb>x" + config.get("titan-event.need-amount-item", Integer.class) + " Осколок Титана");
        } else {
            DHAPI.setHologramLine(hologram, 3, "Попробуй <#00FF00>одолеть<#FFFFFF> самого <#3446eb>Титана");
            DHAPI.setHologramLine(hologram, 4, "и завладеть <#808080>его <#0000FF>рангом<#FFFFFF>!");
        }
    }

    private void createHologram(){
        if(DHAPI.getHologram("titan-event-holo") != null){
            DHAPI.removeHologram("titan-event-holo");
        }

        hologram = DHAPI.createHologram("titan-event-holo", config.get("titan-event.position", Location.class).add(0, 1.75, 0));
        DHAPI.addHologramLine(hologram, "<#3446eb>Падение Титана");
        DHAPI.addHologramLine(hologram, "Статус: " + statusToString(eventStatus));
        DHAPI.addHologramLine(hologram, "");
        if(eventStatus == EventStatus.OFFLINE){
            DHAPI.addHologramLine(hologram, "Для старта <#FF0000>необходимо");
            DHAPI.addHologramLine(hologram, "иметь <#3446eb>x" + config.get("titan-event.need-amount-item", Integer.class) + " Осколок Титана");
        } else {
            DHAPI.addHologramLine(hologram, "Попробуй <#00FF00>одолеть<#FFFFFF> самого <#3446eb>Титана");
            DHAPI.addHologramLine(hologram, "и завладеть <#808080>его <#0000FF>рангом<#FFFFFF>!");
        }

        hologram.showAll();
    }

    public void setStatus(EventStatus status){
        eventStatus = status;
        updateHologram();
    }

    public void updateAddPlayer(Player player){
        if(lastGame.containsKey(player.getName())){
            long lastGame = this.lastGame.get(player.getName());
            if(System.currentTimeMillis() - lastGame > 120000000){
                player.sendMessage(DarkAPI.parse("<prefix>Вы <gray>недавно <red>участвовали<white> в игре!"));
                player.sendMessage(DarkAPI.parse("<prefix>Подождите ещё: <yellow>" + Utils.formatMillis(System.currentTimeMillis() - lastGame)));
            }
        }
        if(!playersInCircle.contains(player)){
            player.sendActionBar(DarkAPI.parse("Вы <green>вступили<white> в <blue>ивент<white>!"));
            playersInCircle.add(player);

            if(playersInCircle.size() > 1){
                startGame();
            }

            updateHologram();
        }
    }

    private String statusToString(EventStatus status){
        return switch (status){
            case RUNNING -> "<#FF0000>Активен";
            case PAID -> "<#008000>Ожидание игроков (" + playersInCircle.size() + "/5)";
            case OFFLINE -> "<#808080>Не активен";
        };
    }

    public void removePlayer(Player player){
        player.sendActionBar(DarkAPI.parse("Вы <red>покинули<white> <white>ивент<white>!"));
        playersInCircle.remove(player);
        updateHologram();
    }
}