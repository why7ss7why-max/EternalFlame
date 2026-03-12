package me.civworld.eternalFlame.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Config {
    private final Plugin plugin;
    public final Map<String, Object> configCache = new HashMap<>();

    private File actionsFile;
    private FileConfiguration actionsYaml;

    public Config(Plugin plugin){
        this.plugin = plugin;
        loadMainConfig();
        loadActionsYaml();
    }

    // ===== config.yml =====

    public void loadMainConfig(){
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();

        configCache.clear();
        for(String key : config.getKeys(true)){
            configCache.put(key, config.get(key));
        }
    }

    public void saveMainConfig(){
        FileConfiguration config = plugin.getConfig();
        for(Map.Entry<String, Object> entry : configCache.entrySet()){
            config.set(entry.getKey(), entry.getValue());
        }
        plugin.saveConfig();
    }

    public Object get(String key){
        return configCache.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type){
        Object val = configCache.get(key);
        if(type.isInstance(val)){
            return (T) val;
        }
        return null;
    }

    public void set(String key, Object value){
        configCache.put(key, value);
    }

    public void loadActionsYaml(){
        actionsFile = new File(plugin.getDataFolder(), "actions.yml");

        if(!actionsFile.exists()){
            try {
                plugin.getDataFolder().mkdirs();
                actionsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Error: " + e.getMessage());
            }
        }

        actionsYaml = YamlConfiguration.loadConfiguration(actionsFile);
    }

    public void reloadAll(){
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        configCache.clear();
        for(String key : config.getKeys(true)){
            configCache.put(key, config.get(key));
        }

        actionsYaml = YamlConfiguration.loadConfiguration(actionsFile);
    }

    public FileConfiguration getActionsYaml(){
        return actionsYaml;
    }

    public void saveActionsYaml(){
        try{
            actionsYaml.save(actionsFile);
        } catch(IOException e){
            plugin.getLogger().warning("Error: " + e.getMessage());
        }
    }
}