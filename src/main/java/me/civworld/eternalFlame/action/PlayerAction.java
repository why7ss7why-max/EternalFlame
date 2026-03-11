package me.civworld.eternalFlame.action;

import org.bukkit.Location;

public class PlayerAction {
    public final Location location;
    public final long timestamp;

    public PlayerAction(Location location, long timestamp) {
        this.location = location.clone();
        this.timestamp = timestamp;
    }
}