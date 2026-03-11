package me.civworld.eternalFlame.command;

import me.civworld.eternalFlame.circle.CircleManager;
import me.civworld.eternalFlame.config.Config;
import me.civworld.eternalFlame.event.TitanEvent;
import me.civworld.eternalFlame.spawner.ItemSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.civworld.darkAPI.DarkAPI;

public class EternalCommand implements CommandExecutor {
    private final Plugin plugin;
    private final Config config;
    private final ItemSpawner itemSpawner;
    private final CircleManager circleManager;
    private final TitanEvent titanEvent;

    public EternalCommand(Plugin plugin, Config config, ItemSpawner itemSpawner, CircleManager circleManager, TitanEvent titanEvent){
        this.plugin = plugin;
        this.config = config;
        this.itemSpawner = itemSpawner;
        this.circleManager = circleManager;
        this.titanEvent = titanEvent;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!sender.hasPermission("eternal.maincommand")){
            sender.sendMessage(DarkAPI.parse("<prefix>Вы <red>не администратор<white>!"));
            return true;
        }

        if(args.length < 1){
            helpCommand(sender, label);
            return true;
        }

        switch(args[0].toLowerCase()){
            case "reload" -> {
                if(!sender.hasPermission("eternal.reload")){
                    sender.sendMessage(DarkAPI.parse("<prefix>Вы <red>не администратор<white>!"));
                    return true;
                }

                config.reloadConfig();

                itemSpawner.updateSpawnings();
                circleManager.updateCircleRound();
                sender.sendMessage(DarkAPI.parse("<prefix>" + config.get("config-reload", String.class)));
            }
            case "settitan" -> {
                if(!sender.hasPermission("eternal.settitan")){
                    sender.sendMessage(DarkAPI.parse("<prefix>Вы <red>не администратор<white>!"));
                    return true;
                }

                if(!(sender instanceof Player player)){
                    sender.sendMessage(DarkAPI.parse("<prefix>Вы <red>не игрок<white>!"));
                    return true;
                }

                config.set("titan-event.position", player.getLocation());
                config.configCache.put("titan-event.position", player.getLocation());
                config.saveConfig();
                player.sendMessage(DarkAPI.parse("<prefix>Успешно <green>установлено<white>!"));
            }
            case "starttitan" -> {
                if(!sender.hasPermission("eternal.starttitan")){
                    sender.sendMessage(DarkAPI.parse("<prefix>Вы <red>не администратор<white>!"));
                    return true;
                }

                if(titanEvent.playersInCircle.isEmpty()){
                    sender.sendMessage(DarkAPI.parse("<prefix>Недостаточно <red>игроков<white> для <yellow>старта<white>!"));
                    return true;
                }

                titanEvent.startGame();
            }
            default -> helpCommand(sender, label);
        }

        return true;
    }

    private void helpCommand(CommandSender sender, String label){
        sender.sendMessage(DarkAPI.parse("<prefix>Использование:"));
        sender.sendMessage(DarkAPI.parse("<prefix><green>/" + label + " reload <gray>- <white>Перезагрузить <yellow>плагин"));
        sender.sendMessage(DarkAPI.parse("<prefix><green>/" + label + " settitan <gray>- <white>Установить <blue>титан"));
    }
}