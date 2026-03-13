package me.civworld.eternalFlame.manager;

import me.civworld.eternalFlame.config.Config;
import me.civworld.eternalFlame.event.TitanEvent;
import me.civworld.eternalFlame.type.EventStatus;
import me.civworld.eternalFlame.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import ru.civworld.darkAPI.DarkAPI;

import java.util.ArrayList;
import java.util.Iterator;
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
        lines.add("§fИгроки:");
        for(Player player : titanEvent.playersInGame){
            lines.add("§7 - §f" + player.getName() + " §c" + repeat(titanEvent.playerAttempts.getOrDefault(player, 3)));
        }
        lines.add("   ");
        lines.add("Время: §e01:00");

        for(int i = 0; i < lines.size(); i++){
            String line = lines.get(i);
            Score score = objective.getScore(line);
            score.setScore(lines.size() - i);
        }

        Location teleport = new Location(Bukkit.getWorld("world"), 220.5, 63.0, -128.5, -180.0f, 0.0f);
        for (Player player : titanEvent.playersInGame) {
            player.setScoreboard(board);
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            player.removePotionEffect(PotionEffectType.SLOW);
            titanEvent.playerParkourists.add(player);
            player.teleport(teleport, PlayerTeleportEvent.TeleportCause.UNKNOWN);
        }

        new BukkitRunnable(){
            @Override
            public void run(){
                if(titanEvent.playersInGame.isEmpty() && titanEvent.playerParkourists.isEmpty() && titanEvent.playersInBlindness.isEmpty()){
                    cancel();
                    return;
                }

                if(titanEvent.playerParkourists.isEmpty()) return;

                Iterator<Player> iterator = titanEvent.playerParkourists.iterator();
                while(iterator.hasNext()){
                    Player player = iterator.next();

                    player.setHealth(20);
                    player.setFoodLevel(20);
                    player.setSaturation(20);

                    if(teleport.getY() - player.getLocation().getY() > 1){
                        titanEvent.playerAttempts.put(player, titanEvent.playerAttempts.getOrDefault(player, 3) - 1);
                        int attempts = titanEvent.playerAttempts.get(player);
                        if(attempts <= 0){
                            player.sendMessage(DarkAPI.parse("<red>❖ <white>Вы <red>выбыли<white>!"));

                            iterator.remove();
                            titanEvent.playersInGame.remove(player);
                            titanEvent.playersInBlindness.remove(player);
                            titanEvent.playerAttempts.remove(player);

                            player.teleport(config.get("titan-event.tp-on-leave", Location.class));
                            player.setGameMode(GameMode.SURVIVAL);
                            player.showTitle(Title.title(DarkAPI.parse("<red>ПРОВАЛ"), DarkAPI.parse("<white>Вы <red>выбыли<white>!")));
                            removePlayerScoreboard(player);
                            if(titanEvent.playersInGame.isEmpty() && titanEvent.playersInBlindness.isEmpty() && titanEvent.playerParkourists.isEmpty()){
                                npcManager.forceShutdown();
                            }
                            continue;
                        }
                        player.showTitle(Title.title(DarkAPI.parse("<red>ПАДЕНИЕ"), DarkAPI.parse("<white>Осталось попыток: <red>" + attempts)));
                        player.sendMessage(DarkAPI.parse("<red>❖ <white>Вы <red>упали<white>! Осталось попыток: <blue>" + attempts));
                        player.teleport(teleport, PlayerTeleportEvent.TeleportCause.UNKNOWN);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        titanEvent.npcManager.startParkour();

        int[] time = {60};
        Bukkit.getScheduler().runTaskTimer(plugin, () -> updateTitanEvent(titanEvent, time, npcManager), 20L, 20L);
    }

    private String repeat(int repeats){
        return "❤".repeat(Math.max(0, repeats));
    }

    public void updateTitanEvent(TitanEvent titanEvent, int[] time, NPCManager npcManager){
        if(time[0] < 0){
            for(Player player : titanEvent.playerParkourists){
                if(titanEvent.playerDone.containsKey(player)) continue;

                player.sendMessage(DarkAPI.parse("<red>❖ <white>Время <yellow>вышло<white>! Вы <red>выбыли<white>!"));
                player.showTitle(Title.title(DarkAPI.parse("<red>ПРОВАЛ"), DarkAPI.parse("<white>Вы <red>не успели<white>!")));

                titanEvent.playersInCircle.remove(player);
                titanEvent.playersInGame.remove(player);
                titanEvent.playerParkourists.remove(player);
                titanEvent.playersInBlindness.remove(player);
                titanEvent.playerAttempts.remove(player);

                player.removePotionEffect(PotionEffectType.BLINDNESS);
                player.removePotionEffect(PotionEffectType.SLOW);

                player.setGameMode(GameMode.SURVIVAL);

                removePlayerScoreboard(player);

                Location spawnLocation = config.get("titan-event.tp-on-leave", Location.class);
                if(spawnLocation != null){
                    player.teleport(spawnLocation);
                }

                if(titanEvent.playersInGame.isEmpty() && titanEvent.playersInBlindness.isEmpty() && titanEvent.playerParkourists.isEmpty()){
                    npcManager.forceShutdown();
                }
            }
        } else for (Player player : titanEvent.playersInGame) {
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
                lines.add("§fИгроки:");
                for (Player p : titanEvent.playersInGame) {
                    String done = titanEvent.playerDone.getOrDefault(player, false) ? " §a✔" : "";
                    lines.add("§7 - §f" + p.getName() + " §c" + repeat(titanEvent.playerAttempts.getOrDefault(p, 3)) + done);
                }
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