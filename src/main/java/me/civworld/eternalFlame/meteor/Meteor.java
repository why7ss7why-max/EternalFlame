package me.civworld.eternalFlame.meteor;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Meteor {

    private final Plugin plugin;

    public Meteor(Plugin plugin) {
        this.plugin = plugin;
    }

    public void spawnMeteor(Location start, Location target) {

        World world = start.getWorld();

        // создаём один большой магма-блок
        BlockDisplay display = (BlockDisplay) world.spawnEntity(start, EntityType.BLOCK_DISPLAY);
        display.setBlock(Bukkit.createBlockData(Material.MAGMA_BLOCK));

        float size = 8f; // размер блока
        Vector3f scale = new Vector3f(size, size, size);

        // вектор направления
        Vector direction = target.toVector().subtract(start.toVector()).normalize();

        // один поворот в сторону полёта
        Quaternionf rotation = lookAtQuaternion(direction);

        display.setTransformation(new Transformation(
                new Vector3f(0,0,0),
                rotation,
                scale,
                new Quaternionf()
        ));

        Vector velocity = direction.clone().multiply(0.6);

        new BukkitRunnable() {

            boolean landed = false;
            long landTime = 0;

            @Override
            public void run() {
                if (!display.isValid()) {
                    cancel();
                    return;
                }

                Location loc = display.getLocation();

                if (!landed) {
                    loc.add(velocity);

                    int groundY = world.getHighestBlockYAt(loc);
                    float bottomY = (float) (loc.getY() - size / 2f);

                    if (bottomY <= groundY + 1) {
                        loc.setY(groundY + 1 + size / 2f);
                        landed = true;
                        landTime = System.currentTimeMillis();

                        world.spawnParticle(Particle.LAVA, loc, 40, 1,1,1,0.2);
                        world.spawnParticle(Particle.SMOKE_LARGE, loc, 40, 1,1,1,0.05);
                    }
                }

                display.teleport(loc);

                if (!landed) {
                    world.spawnParticle(Particle.FLAME, loc, 25, 0.8,0.8,0.8,0.02);
                    world.spawnParticle(Particle.SMOKE_NORMAL, loc, 15, 0.5,0.5,0.5,0.01);
                }

                if (landed) {
                    display.remove();
                    cancel();
                }
            }

        }.runTaskTimer(plugin, 0, 1);
    }

    private Quaternionf lookAtQuaternion(Vector direction) {
        Vector3f dir = new Vector3f((float) direction.getX(), (float) direction.getY(), (float) direction.getZ());
        dir.normalize();
        Vector3f up = new Vector3f(0,1,0);
        Vector3f right = new Vector3f();
        up.cross(dir, right).normalize();
        up = dir.cross(right, new Vector3f()).normalize();

        float m00 = right.x, m01 = right.y, m02 = right.z;
        float m10 = up.x,    m11 = up.y,    m12 = up.z;
        float m20 = dir.x,   m21 = dir.y,   m22 = dir.z;

        float t = m00 + m11 + m22;
        float qw, qx, qy, qz;
        if (t > 0) {
            float s = (float)Math.sqrt(t + 1.0f) * 2;
            qw = 0.25f * s;
            qx = (m21 - m12) / s;
            qy = (m02 - m20) / s;
            qz = (m10 - m01) / s;
        } else if ((m00 > m11) & (m00 > m22)) {
            float s = (float)Math.sqrt(1.0f + m00 - m11 - m22) * 2;
            qw = (m21 - m12) / s;
            qx = 0.25f * s;
            qy = (m01 + m10) / s;
            qz = (m02 + m20) / s;
        } else if (m11 > m22) {
            float s = (float)Math.sqrt(1.0f + m11 - m00 - m22) * 2;
            qw = (m02 - m20) / s;
            qx = (m01 + m10) / s;
            qy = 0.25f * s;
            qz = (m12 + m21) / s;
        } else {
            float s = (float)Math.sqrt(1.0f + m22 - m00 - m11) * 2;
            qw = (m10 - m01) / s;
            qx = (m02 + m20) / s;
            qy = (m12 + m21) / s;
            qz = 0.25f * s;
        }
        return new Quaternionf(qx, qy, qz, qw);
    }
}