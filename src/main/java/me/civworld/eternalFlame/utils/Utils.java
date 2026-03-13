package me.civworld.eternalFlame.utils;

import me.civworld.eternalFlame.type.ParkourDifficult;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Random;

public class Utils {
    public static void removePlayerScoreboard(Player player){
        Scoreboard board = player.getScoreboard();
        Objective obj = board.getObjective("titanEvent");
        if (obj != null) {
            obj.unregister();
        }
    }

    public static String formatMillis(long millis) {
        long totalSeconds = millis / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if(hours > 0) sb.append(hours).append(" ч. ");
        if(minutes > 0) sb.append(minutes).append(" мин. ");
        sb.append(seconds).append(" сек.");

        return sb.toString();
    }

    public static boolean isLucky(int chance){
        Random random = new Random();
        return random.nextInt(100) < chance;
    }

    public static Location findSafeSpawnNearPlayer(Player player, int attempts, int radiusX, int radiusY) {
        Random random = new Random();
        World world = player.getWorld();

        for (int attempt = 0; attempt < attempts; attempt++) {
            double dx = (random.nextDouble() * 2 - 1) * radiusX; // 100
            double dz = (random.nextDouble() * 2 - 1) * radiusX; // 100

            double dy = radiusY + random.nextInt((radiusY * 2) - radiusY + 1);

            Location loc = player.getLocation().clone().add(dx, dy, dz);

            int y = loc.getBlockY();
            while (y > 0 && loc.getWorld().getBlockAt(loc.getBlockX(), y, loc.getBlockZ()).getType() == Material.AIR) y--;

            Block ground = loc.getWorld().getBlockAt(loc.getBlockX(), y, loc.getBlockZ());
            if (ground.getType().isSolid()) {
                Location spawn = new Location(world, loc.getX(), y + 1, loc.getZ());
                if (spawn.getBlock().getType() == Material.AIR) return spawn;
            }
        }

        return null;
    }

    public static String difficultToString(ParkourDifficult difficult){
        return switch (difficult) {
            case SUPER_EASY -> "§a§nСупер лёгкая";
            case EASY -> "§2§nЛёгкая";
            case LITE -> "§e§nЛайт";
            case NORMAL -> "§6§nНормальная";
            case LITE_HARD -> "§c§nЛайт-сложная";
            case HARD -> "§4§nСложная";
            case SUPER_HARD -> "§4§nСупер сложная";
            case MEGA_HARD -> "§5§nМега сложная";
            case LEGEND -> "§0§nЛегендарная";
        };
    }
}