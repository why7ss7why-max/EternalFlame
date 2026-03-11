package me.civworld.eternalFlame.event;

import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockTypes;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.civworld.eternalFlame.config.Config;
import me.civworld.eternalFlame.type.EventStatus;
import me.civworld.eternalFlame.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import ru.civworld.darkAPI.DarkAPI;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class TitanEvent implements Listener {
    private final Plugin plugin;
    private final Config config;
    public EventStatus eventStatus = EventStatus.OFFLINE;

    public final HashMap<String, Long> lastGame = new HashMap<>();

    public final LinkedHashSet<Player> playersInCircle = new LinkedHashSet<>();
    public final LinkedHashSet<Player> playersInGame = new LinkedHashSet<>();

    public Hologram hologram = null;

    public TitanEvent(Plugin plugin, Config config){
        this.plugin = plugin;
        this.config = config;
    }

    public void startGame(){
        Iterator<Player> iterator = playersInCircle.iterator();
        while(iterator.hasNext()){
            Player player = iterator.next();
            iterator.remove();

            Location cutLocation = config.get("titan-event.position-cut", Location.class).clone();
            cutLocation.setYaw(0);
            cutLocation.setPitch(-45);

            Tadpole tadpole = (Tadpole) cutLocation.getWorld().spawnEntity(cutLocation, EntityType.TADPOLE);
            tadpole.setGravity(false);
            tadpole.setAI(false);
            tadpole.setAgeLock(true);
            tadpole.setInvulnerable(true);
            tadpole.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, true, false));

            player.setGameMode(GameMode.SPECTATOR);
            player.setSpectatorTarget(tadpole);

            float[] times = {-40};
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    if(times[0] > -10) {
                        this.cancel();
                        return;
                    }

                    cutLocation.setPitch(times[0]);
                    tadpole.teleport(cutLocation);
                    player.setGameMode(GameMode.SPECTATOR);
                    player.setSpectatorTarget(null);
                    player.setSpectatorTarget(tadpole);

                    times[0]+=0.5f;
                }
            };

            runnable.runTaskTimer(plugin, 60L, 1L);

            playersInGame.add(player);
            lastGame.put(player.getName(), System.currentTimeMillis());
        }

        File file = new File(plugin.getDataFolder(), "meteorite.schem");

        ClipboardFormat format = ClipboardFormats.findByFile(file);
        if(format != null){
            Clipboard clipboard;

            try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
                clipboard = reader.read();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Location start = config.get("titan-event.meteorite.pos1", Location.class).clone();
            Location end = config.get("titan-event.meteorite.pos2", Location.class).clone();

            int steps = 7;

            double dx = (end.getX() - start.getX()) / steps;
            double dy = (end.getY() - start.getY()) / steps;
            double dz = (end.getZ() - start.getZ()) / steps;

            new BukkitRunnable() {
                int step = 0;

                @Override
                public void run() {
                    if (step > steps) {
                        cancel();
                        return;
                    }

                    double x = start.getX() + dx * step;
                    double y = start.getY() + dy * step;
                    double z = start.getZ() + dz * step;

                    World world = BukkitAdapter.adapt(start.getWorld());

                    try (EditSession session = WorldEdit.getInstance().newEditSession(world)) {
                        BlockVector3 min = BlockVector3.at(
                                Math.min(start.getX(), end.getX()),
                                Math.min(start.getY(), end.getY()),
                                Math.min(start.getZ(), end.getZ())
                        );
                        BlockVector3 max = BlockVector3.at(
                                Math.max(start.getX(), end.getX()) + clipboard.getRegion().getWidth(),
                                Math.max(start.getY(), end.getY()) + clipboard.getRegion().getHeight(),
                                Math.max(start.getZ(), end.getZ()) + clipboard.getRegion().getLength()
                        );

                        for (int xx = min.getX(); xx <= max.getX(); xx++) {
                            for (int yy = min.getY(); yy <= max.getY(); yy++) {
                                for (int zz = min.getZ(); zz <= max.getZ(); zz++) {
                                    session.setBlock(BlockVector3.at(xx, yy, zz), BlockTypes.AIR.getDefaultState());
                                }
                            }
                        }

                        Region clipRegion = clipboard.getRegion();

                        int width = clipRegion.getWidth();
                        int height = clipRegion.getHeight();
                        int length = clipRegion.getLength();

                        double minX = Math.min(start.getX(), end.getX());
                        double minY = Math.min(start.getY(), end.getY());
                        double minZ = Math.min(start.getZ(), end.getZ());

                        double maxX = Math.max(start.getX(), end.getX());
                        double maxY = Math.max(start.getY(), end.getY());
                        double maxZ = Math.max(start.getZ(), end.getZ());

                        if (x + width > maxX) {
                            x = maxX - width;
                        }
                        if (x < minX) {
                            x = minX;
                        }

                        if (y + height > maxY) {
                            y = maxY - height;
                        }
                        if (y < minY) {
                            y = minY;
                        }

                        if (z + length > maxZ) {
                            z = maxZ - length;
                        }
                        if (z < minZ) {
                            z = minZ;
                        }

                        Operation op = new ClipboardHolder(clipboard)
                                .createPaste(session)
                                .to(BlockVector3.at(x, y, z))
                                .ignoreAirBlocks(false)
                                .build();

                        Operations.complete(op);
                        session.flushQueue();
                    } catch (Exception e) {
                        plugin.getLogger().warning("Error: " + e.getMessage());
                    }

                    step++;
                }

            }.runTaskTimer(plugin, 0, 20);
        }

        setStatus(EventStatus.RUNNING);
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