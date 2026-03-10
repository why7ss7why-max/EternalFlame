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

        Block blockUnder = current.getWorld().getBlockAt(feet);
        Block blockBehind = getSupportingBlockBehindNPC(current, 0.5);

        Vector lookDir = current.getDirection().setY(0).normalize();

        Location targetAhead = feet.clone().add(lookDir);
        Block blockAhead = targetAhead.getBlock();

        Location targetTwoAhead = feet.clone().add(lookDir.multiply(2.0));
        Block blockTwoAhead = targetTwoAhead.getBlock();

        if (blockAhead.getType() == Material.AIR &&
                blockUnder.getType() == Material.AIR &&
                blockBehind.getType() == Material.AIR) {

            if (jumpCooldown <= 0) {
                if (blockTwoAhead.getType() != Material.AIR) {
                    makeNpcJump(npc, 0.25, 0.15);
                } else {
                    makeNpcJump(npc, 0.55, 0.3);
                }
                jumpCooldown = 35;
            }
        }
    }
}