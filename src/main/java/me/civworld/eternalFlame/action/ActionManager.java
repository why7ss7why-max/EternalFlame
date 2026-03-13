package me.civworld.eternalFlame.action;

import me.civworld.eternalFlame.config.Config;
import me.civworld.eternalFlame.type.ParkourDifficult;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionManager implements Listener {
    private final Config config;

    public HashMap<ParkourDifficult, List<PlayerAction>> titanActions = new HashMap<>();
    public String record = null;
    public ParkourDifficult difficult = null;

    public ActionManager(Config config){
        this.config = config;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e){
        if(record == null) return;
        Player player = e.getPlayer();
        if(!record.equals(player.getName())) return;

        titanActions
                .computeIfAbsent(difficult, k -> new ArrayList<>())
                .add(new PlayerAction(e.getTo(), System.currentTimeMillis()));
    }

    public HashMap<ParkourDifficult, List<PlayerAction>> loadActions(String name){
        HashMap<ParkourDifficult, List<PlayerAction>> actions = new HashMap<>();
        String path = "titans." + name;

        if (!config.getActionsYaml().contains(path)) return actions;

        for (ParkourDifficult difficulty : ParkourDifficult.values()) {
            String diffPath = path + "." + difficulty.toString().toLowerCase();
            if (!config.getActionsYaml().contains(diffPath)) continue;

            List<Map<?, ?>> list = config.getActionsYaml().getMapList(diffPath);
            List<PlayerAction> actionList = new ArrayList<>();

            for (Map<?, ?> map : list) {
                double x = ((Number) map.get("x")).doubleValue();
                double y = ((Number) map.get("y")).doubleValue();
                double z = ((Number) map.get("z")).doubleValue();
                float yaw = ((Number) map.get("yaw")).floatValue();
                float pitch = ((Number) map.get("pitch")).floatValue();

                Location loc = new Location(Bukkit.getWorld("world"), x, y, z, yaw, pitch);
                actionList.add(new PlayerAction(loc, 0));
            }

            actions.put(difficulty, actionList);
        }

        return actions;
    }

    public void saveActions(String name){
        for(ParkourDifficult difficulty : titanActions.keySet()){
            List<Map<String, Object>> list = new ArrayList<>();
            for(PlayerAction action : titanActions.get(difficulty)){
                list.add(Map.of(
                        "x", action.location.getX(),
                        "y", action.location.getY(),
                        "z", action.location.getZ(),
                        "yaw", action.location.getYaw(),
                        "pitch", action.location.getPitch()
                ));
            }
            config.getActionsYaml().set("titans." + name + "." + difficulty.toString().toLowerCase(), list);
        }
        config.saveActionsYaml();
    }

    public void clear(){
        record = null;
        titanActions.clear();
    }
}