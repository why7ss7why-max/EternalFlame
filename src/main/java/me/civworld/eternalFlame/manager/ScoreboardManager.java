package me.civworld.eternalFlame.manager;

import me.civworld.eternalFlame.event.TitanEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;

import static me.civworld.eternalFlame.utils.Utils.formatTime;

public class ScoreboardManager {
    private final Plugin plugin;

    public ScoreboardManager(Plugin plugin){
        this.plugin = plugin;
    }

    public void titanEvent(TitanEvent titanEvent){
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
        lines.add("§fИгроки:");
        for(Player player : titanEvent.playersInGame){
            lines.add("§7 - §f" + player.getName() + " §c" + repeat(titanEvent.playerAttempts.getOrDefault(player, 3)));
        }
        lines.add("  ");
        lines.add("Время: §e10:00");

        for(int i = 0; i < lines.size(); i++){
            String line = lines.get(i);
            Score score = objective.getScore(line);
            score.setScore(lines.size() - i);
        }

        for (Player player : titanEvent.playersInGame) {
            player.setScoreboard(board);
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            player.removePotionEffect(PotionEffectType.SLOW);
            titanEvent.playerParkourists.add(player);
            player.teleport(new Location(Bukkit.getWorld("world"), 220.5, 63.0, -128.5, -180.0f, 0.0f), PlayerTeleportEvent.TeleportCause.UNKNOWN);
        }

        titanEvent.npcManager.startParkour();

        int[] time = {600};
        Bukkit.getScheduler().runTaskTimer(plugin, () -> updateTitanEvent(titanEvent, time), 20L, 20L);
    }

    private String repeat(int repeats){
        return "❤".repeat(Math.max(0, repeats));
    }

    public void updateTitanEvent(TitanEvent titanEvent, int[] time){
        for (Player player : titanEvent.playersInGame) {
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
                lines.add("§fИгроки:");
                for (Player p : titanEvent.playersInGame) {
                    lines.add("§7 - §f" + p.getName() + " §c❤❤❤");
                }
                lines.add("  ");
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
}