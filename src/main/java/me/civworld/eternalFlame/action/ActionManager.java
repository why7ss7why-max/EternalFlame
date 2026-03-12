package me.civworld.eternalFlame.action;

import me.civworld.eternalFlame.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ActionManager implements Listener {
    private final Config config;

    public List<PlayerAction> titanActions = new ArrayList<>();
    public String record = null;

    public ActionManager(Config config){
        this.config = config;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e){
        if(record == null) return;
        Player player = e.getPlayer();
        if(!record.equals(player.getName())) return;

        titanActions.add(new PlayerAction(e.getTo(), System.currentTimeMillis()));
    }

    public List<PlayerAction> loadActions(String name){
        List<PlayerAction> actions = new ArrayList<>();

        if(!config.getActionsYaml().contains("titans." + name)) return actions;

        List<Map<?, ?>> list = config.getActionsYaml().getMapList("titans." + name);

        for(Map<?, ?> map : list){

            double x = ((Number) map.get("x")).doubleValue();
            double y = ((Number) map.get("y")).doubleValue();
            double z = ((Number) map.get("z")).doubleValue();
            float yaw = ((Number) map.get("yaw")).floatValue();
            float pitch = ((Number) map.get("pitch")).floatValue();

            Location loc = new Location(
                    Bukkit.getWorld("world"),
                    x, y, z,
                    yaw, pitch
            );

            actions.add(new PlayerAction(loc, System.currentTimeMillis()));
        }

        return actions;
    }

    public void saveActions(String name){

        List<Map<String, Object>> list = new ArrayList<>();

        for(PlayerAction action : titanActions){
            list.add(Map.of(
                    "x", action.location.getX(),
                    "y", action.location.getY(),
                    "z", action.location.getZ(),
                    "yaw", action.location.getYaw(),
                    "pitch", action.location.getPitch()
            ));
        }

        config.getActionsYaml().set("titans." + name, list);
        config.saveActionsYaml();
    }

    public void clear(){
        record = null;
        titanActions.clear();
    }
}