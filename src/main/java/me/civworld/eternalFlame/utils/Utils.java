package me.civworld.eternalFlame.utils;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Random;

public class Utils {
    public static Block getSupportingBlockBehindNPC(Location loc, double distanceBack) {
        Vector backward = loc.getDirection().setY(0).normalize().multiply(-distanceBack);

        Location feet = loc.clone().subtract(0, 1, 0);

        Location checkLoc = feet.clone().add(backward);
        return checkLoc.getBlock();
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

    public static void makeNpcJump(NPC npc, double jumpHeight, double forwardMultiplier) {
        if (!npc.isSpawned()) return;

        LivingEntity entity = (LivingEntity) npc.getEntity();

        Vector currentVelocity = entity.getVelocity();

        Vector lookDir = entity.getLocation().getDirection().setY(0).normalize();
        Vector jumpVector = lookDir.multiply(forwardMultiplier);

        jumpVector.setY(jumpHeight);

        entity.setVelocity(jumpVector.add(currentVelocity));
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
//            double dy = 5 + random.nextInt(10 - 5 + 1);

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
}