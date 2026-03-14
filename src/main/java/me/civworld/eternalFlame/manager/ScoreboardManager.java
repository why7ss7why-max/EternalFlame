package me.civworld.eternalFlame.manager;

import me.civworld.eternalFlame.config.Config;
import me.civworld.eternalFlame.event.TitanEvent;
import me.civworld.eternalFlame.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import ru.civworld.darkAPI.DarkAPI;

import java.util.ArrayList;
import java.util.List;

import static me.civworld.eternalFlame.utils.Utils.removePlayerScoreboard;

public class ScoreboardManager {
    private final Plugin plugin;
    private final Config config;

    public ScoreboardManager(Plugin plugin, Config config){
        this.plugin = plugin;
        this.config = config;
    }

    public void titanEvent(TitanEvent titanEvent, NPCManager npcManager){
        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        Objective objective = board.registerNewObjective(
                "titanEvent",
                "dummy",
                Component.text("§x§3§4§4§6§E§BПадение Титана")
        );
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<String> lines = new ArrayList<>();
        lines.add("");
        lines.add("§cЗадача§f: Победить титана");
        lines.add(" ");
        lines.add("§fСложность: §7" + Utils.difficultToString(titanEvent.difficult));
        lines.add("  ");
        lines.add("§fПопыток: §c" + repeat(titanEvent.attempts));
        lines.add("   ");
        lines.add("Время: §e01:00");

        for(int i = 0; i < lines.size(); i++){
            String line = lines.get(i);
            Score score = objective.getScore(line);
            score.setScore(lines.size() - i);
        }

        Location teleport = new Location(Bukkit.getWorld("world"), 220.5, 63.0, -128.5, -180.0f, 0.0f);
        titanEvent.player.setScoreboard(board);
        titanEvent.player.removePotionEffect(PotionEffectType.BLINDNESS);
        titanEvent.player.removePotionEffect(PotionEffectType.SLOW);
        titanEvent.allowTeleport = true;
        titanEvent.player.teleport(teleport);
        titanEvent.allowTeleport = false;

        new BukkitRunnable(){
            @Override
            public void run(){
                if(titanEvent.player == null){
                    cancel();
                    return;
                }

                Player player = titanEvent.player;

                player.setHealth(20);
                player.setFoodLevel(20);
                player.setSaturation(20);

                if(teleport.getY() - player.getLocation().getY() > 1){
                    titanEvent.attempts--;
                    int attempts = titanEvent.attempts;
                    if(attempts <= 0){
                        player.sendMessage(DarkAPI.parse("<red>❖ <white>Вы <red>выбыли<white>!"));

                        titanEvent.player = null;

                        player.teleport(config.get("titan-event.tp-on-leave", Location.class));
                        player.setGameMode(GameMode.SURVIVAL);
                        player.showTitle(Title.title(DarkAPI.parse("<red>ПРОВАЛ"), DarkAPI.parse("<white>Вы <red>выбыли<white>!")));
                        removePlayerScoreboard(player);
                        npcManager.forceShutdown();
                        return;
                    }
                    player.showTitle(Title.title(DarkAPI.parse("<red>ПАДЕНИЕ"), DarkAPI.parse("<white>Осталось попыток: <red>" + attempts)));
                    player.sendMessage(DarkAPI.parse("<red>❖ <white>Вы <red>упали<white>! Осталось попыток: <blue>" + attempts));
                    titanEvent.allowTeleport = true;
                    player.teleport(teleport);
                    titanEvent.allowTeleport = false;
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        titanEvent.npcManager.startParkour();

        int[] time = {60};
        new BukkitRunnable(){
            @Override
            public void run(){
                if(titanEvent.player == null){
                    this.cancel();
                    return;
                }
                updateTitanEvent(titanEvent, time, npcManager);
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private String repeat(int repeats){
        return "❤".repeat(Math.max(0, repeats));
    }

    public void updateTitanEvent(TitanEvent titanEvent, int[] time, NPCManager npcManager){
        if(time[0] < 0){
            Player player = titanEvent.player;

            player.sendMessage(DarkAPI.parse("<red>❖ <white>Время <yellow>вышло<white>! Вы <red>выбыли<white>!"));
            player.showTitle(Title.title(DarkAPI.parse("<red>ПРОВАЛ"), DarkAPI.parse("<white>Вы <red>не успели<white>!")));

            player.removePotionEffect(PotionEffectType.BLINDNESS);
            player.removePotionEffect(PotionEffectType.SLOW);

            removePlayerScoreboard(player);

            titanEvent.player = null;

            player.setGameMode(GameMode.SURVIVAL);

            Location spawnLocation = config.get("titan-event.tp-on-leave", Location.class);
            if(spawnLocation != null){
                player.teleport(spawnLocation);
            }

            npcManager.forceShutdown();
        } else {
            Player player = titanEvent.player;
            Scoreboard board = player.getScoreboard();
            Objective obj = board.getObjective("titanEvent");

            if (obj != null) {
                for (String entry : board.getEntries()) {
                    board.resetScores(entry);
                }

                List<String> lines = new ArrayList<>();
                lines.add("");
                lines.add("§cЗадача§f: Победить титана");
                lines.add(" ");
                lines.add("§fСложность: §7" + Utils.difficultToString(titanEvent.difficult));
                lines.add("  ");
                lines.add("§fПопыток: §c" + repeat(titanEvent.attempts));
                lines.add("   ");
                lines.add("Время: §e" + formatTime(time[0]));

                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    if (line.trim().isEmpty()) line = " ".repeat(i + 1);
                    Score score = obj.getScore(line);
                    score.setScore(lines.size() - i);
                }
            }
        }
        time[0]--;
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
}