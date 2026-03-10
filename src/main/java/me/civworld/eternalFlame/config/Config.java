package me.civworld.eternalFlame.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class Config {
    private final Plugin plugin;
    public final Map<String, Object> configCache = new HashMap<>();

    public Config(Plugin plugin){
        this.plugin = plugin;
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        configCache.clear();

        for (String key : config.getKeys(true)) {
            configCache.put(key, config.get(key));
        }
    }

    public void loadConfig(){
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();

        for(String key : config.getKeys(true)){
            configCache.put(key, config.get(key));
        }
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

    public void saveConfig(){
        FileConfiguration config = plugin.getConfig();
        for(Map.Entry<String, Object> entry : configCache.entrySet()){
            config.set(entry.getKey(), entry.getValue());
        }
        plugin.saveConfig();
    }
}