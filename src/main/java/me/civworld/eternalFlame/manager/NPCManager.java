package me.civworld.eternalFlame.manager;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.civworld.eternalFlame.action.ActionManager;
import me.civworld.eternalFlame.action.PlayerAction;
import me.civworld.eternalFlame.config.Config;
import me.civworld.eternalFlame.event.TitanEvent;
import me.civworld.eternalFlame.type.EventStatus;
import me.civworld.eternalFlame.type.ParkourDifficult;
import me.civworld.eternalFlame.utils.Utils;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.HologramTrait;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import ru.civworld.darkAPI.DarkAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class NPCManager {
    private final Plugin plugin;
    private final Config config;
    private final ScoreboardManager scoreboardManager;
    private final ActionManager actionManager;
    private NPC npc;
    private TitanEvent titanEvent;
    private Hologram playerHologram;

    public NPCManager(Plugin plugin, Config config, ScoreboardManager scoreboardManager, ActionManager actionManager){
        this.plugin = plugin;
        this.config = config;
        this.scoreboardManager = scoreboardManager;
        this.actionManager = actionManager;
    }

    public void startNpcTitan(TitanEvent titanEvent){
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "test");
        this.npc = npc;
        this.titanEvent = titanEvent;

        World world = plugin.getServer().getWorld("world");
        SkinTrait trait = npc.getOrAddTrait(SkinTrait.class);

        trait.setSkinName("Fakebobby");
        trait.setShouldUpdateSkins(true);
        npc.spawn(new Location(world, 102.295, 64, -93.455, -90.2f, -0.3f));

        HologramTrait hologramTrait = new HologramTrait();
        npc.addTrait(hologramTrait);

        npc.setName("&#3446ebТитан");

        HashMap<String, String> lines = getStringStringHashMap();

        List<String> keys = new ArrayList<>(lines.keySet());
        String firstLine = keys.get(new Random().nextInt(keys.size()));
        String secondLine = lines.get(firstLine);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            hologramTrait.addLine(firstLine);

            for(Player player : titanEvent.playersInGame){
                player.sendMessage(DarkAPI.parse("<red>❖ <#3446eb>Титан <gray>» <white>" + firstLine));
            }
        }, 50L);
        Bukkit.getScheduler().runTaskLater(plugin, hologramTrait::clear, 130L);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            hologramTrait.addLine(secondLine);

            for(Player player : titanEvent.playersInGame){
                player.sendMessage(DarkAPI.parse("<red>❖ <#3446eb>Титан <gray>» <white>" + secondLine));
            }
        }, 160L);
        Bukkit.getScheduler().runTaskLater(plugin, hologramTrait::clear, 240L);


        List<String> playerLines = new ArrayList<>();
        playerLines.add("Я пришёл остановить тебя, Титан!");
        playerLines.add("Сегодня твоя сила не спасёт тебя!");
        playerLines.add("Твои угрозы меня не пугают!");
        playerLines.add("Я сражусь и выйду победителем!");
        playerLines.add("Приготовься, сейчас ты умрёшь!");
        String msg = playerLines.get(new Random().nextInt(playerLines.size()));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Hologram hologram = DHAPI.getHologram("player-hologram-titan-event");
            if (hologram != null) {
                DHAPI.removeHologram("player-hologram-titan-event");
            }
            playerHologram = DHAPI.createHologram("player-hologram-titan-event", config.get("titan-event.position-cut", Location.class).clone().add(0, 1.4, 0), false);
            DHAPI.addHologramLine(playerHologram, "&f" + msg);

            for(Player player : titanEvent.playersInGame){
                player.sendMessage(DarkAPI.parse("<red>❖ <green>" + player.getName() + " <gray>» <white>" + msg));
            }
        }, 260L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> DHAPI.removeHologram(playerHologram.getName()), 320L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for(Player player : titanEvent.playersInGame){
                player.showTitle(Title.title(DarkAPI.parse("<red>Задача"), DarkAPI.parse("<white>Одолеть Титана")));
            }

            scoreboardManager.titanEvent(titanEvent, this);
        }, 340L);
    }

    private @NotNull HashMap<String, String> getStringStringHashMap() {
        HashMap<String, String> lines = new HashMap<>();
        lines.put("Хах, кто-то посмел бросить мне вызов?", "Ну-ну, посмотрим, кто осмелился встать против меня!");
        lines.put("Как такие жалкие существа, посмели меня потревожить?", "Серьёзно? Думаете, вы меня остановите?");
        lines.put("Ах, ты смеешь бросить мне вызов?", "Смешно… Я раздавлю тебя за мгновение!");
        lines.put("Ох, кто осмелился против меня восстать?", "Глупцы! Вы даже не представляете, с кем связались!");
        lines.put("Ха, ты действительно думаешь, что сможешь со мной справиться?", "Я уничтожу всё на своём пути, включая тебя!");
        lines.put("Ты смеешь противостоять мне?", "Слишком смело… Ты узнаешь силу Титана!");
        lines.put("Серьёзно думаешь, что сможешь меня напугать?", "Ха! Твоё ничтожество смешно в сравнении со мной!");
        lines.put("Кто осмелился нарушить мой покой?", "Глупцы! Я раздавлю вас без жалости!");
        lines.put("Ты надеешься победить меня?", "Мечты, пустые мечты… Я сокрушу всё вокруг!");
        return lines;
    }

    public void startParkour(){
        npc.removeTrait(LookClose.class);

        Location npcLocation = new Location(Bukkit.getWorld("world"), 210.5, 63.0, -127.5, 0.0f, -180.0f);
        npc.teleport(npcLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);

        npc.setName("&9Титан");

        List<PlayerAction> actions = actionManager.titanActions.get(ParkourDifficult.SUPER_EASY);
        if(actions == null || actions.isEmpty()) return;

        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (index >= actions.size()) {
                    LookClose lookClose = npc.getOrAddTrait(LookClose.class);
                    lookClose.lookClose(true);
                    lookClose.setRange(40);
                    lookClose.run();
                    cancel();
                    return;
                }

                PlayerAction action = actions.get(index);
                npc.teleport(action.location, PlayerTeleportEvent.TeleportCause.PLUGIN);
                index++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void forceShutdown(){
        if(npc != null){
            npc.despawn();
            npc.destroy();
            CitizensAPI.getNPCRegistry().deregister(npc);
        }
        if(titanEvent != null){
            titanEvent.setStatus(EventStatus.OFFLINE);
            for(Player player : titanEvent.playersInGame){
                player.setGameMode(GameMode.SURVIVAL);
                Utils.removePlayerScoreboard(player);
                player.removePotionEffect(PotionEffectType.BLINDNESS);
                player.removePotionEffect(PotionEffectType.SLOW);
            }
            for (Player p1 : titanEvent.playersInGame) {
                for (Player p2 : titanEvent.playersInGame) {
                    if (!p1.equals(p2)) {
                        p1.showPlayer(plugin, p2);
                        p2.showPlayer(plugin, p1);
                    }
                }
            }
            for (Player p1 : titanEvent.playersInBlindness) {
                for (Player p2 : titanEvent.playersInBlindness) {
                    if (!p1.equals(p2)) {
                        p1.showPlayer(plugin, p2);
                        p2.showPlayer(plugin, p1);
                    }
                }
            }
            for (Player player : titanEvent.playersInCircle) {
                Scoreboard board = player.getScoreboard();
                Objective obj = board.getObjective("titanEvent");
                if (obj != null) {
                    obj.unregister();
                }
            }
        }
        if(playerHologram != null){
            DHAPI.removeHologram(playerHologram.getName());
        }
    }
}