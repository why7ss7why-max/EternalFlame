package me.civworld.eternalFlame.trait;

import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.List;

import static me.civworld.eternalFlame.utils.Utils.getSupportingBlockBehindNPC;
import static me.civworld.eternalFlame.utils.Utils.makeNpcJump;

public class WaypointTrait extends Trait {
    private final List<Location> waypoints;
    private int currentIndex = 0;
    private int jumpCooldown = 0;

    public WaypointTrait(List<Location> waypoints) {
        super("test");
        this.waypoints = waypoints;
    }

    @Override
    public void run() {
        if (!npc.isSpawned()) return;

        if (jumpCooldown > 0) jumpCooldown--;

        npc.getNavigator().getLocalParameters().speedModifier(1.55f);

        if (!npc.getNavigator().isNavigating()) {
            Location target = waypoints.get(currentIndex);
            npc.getNavigator().setStraightLineTarget(target);
            currentIndex = (currentIndex + 1) % waypoints.size();
            return;
        }

        Location current = npc.getEntity().getLocation();
        Location feet = current.clone().subtract(0, 1, 0);

        Vector lookDir = current.getDirection().setY(0).normalize();

        // Идем по линии вперед на 3 блока
        boolean obstacleAhead = false;
        for (int i = 1; i <= 3; i++) {
            Location checkLoc = feet.clone().add(lookDir.clone().multiply(i));
            Block blockAt = checkLoc.getBlock();
            Block blockAbove = checkLoc.clone().add(0, 1, 0).getBlock();
            if (blockAt.getType() != Material.AIR || blockAbove.getType() != Material.AIR) {
                obstacleAhead = true;
                break;
            }
        }

        if (obstacleAhead && jumpCooldown <= 0) {
            makeNpcJump(npc, 0.6, 0.35);
            jumpCooldown = 35;
        }
    }
}