package me.civworld.eternalFlame.command;

import me.civworld.eternalFlame.action.ActionManager;
import me.civworld.eternalFlame.circle.CircleManager;
import me.civworld.eternalFlame.config.Config;
import me.civworld.eternalFlame.event.TitanEvent;
import me.civworld.eternalFlame.spawner.ItemSpawner;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.civworld.darkAPI.DarkAPI;

public class EternalCommand implements CommandExecutor {
    private final Config config;
    private final ItemSpawner itemSpawner;
    private final CircleManager circleManager;
    private final TitanEvent titanEvent;
    private final ActionManager actionManager;

    public EternalCommand(Config config, ItemSpawner itemSpawner, CircleManager circleManager, TitanEvent titanEvent, ActionManager actionManager){
        this.config = config;
        this.itemSpawner = itemSpawner;
        this.circleManager = circleManager;
        this.titanEvent = titanEvent;
        this.actionManager = actionManager;
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

                config.reloadAll();

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
                config.saveMainConfig();
                config.saveActionsYaml();
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
            case "getloc" -> {
                if(!sender.hasPermission("eternal.getloc")){
                    sender.sendMessage(DarkAPI.parse("<prefix>Вы <red>не администратор<white>!"));
                    return true;
                }

                if(!(sender instanceof Player player)){
                    sender.sendMessage(DarkAPI.parse("<prefix>Вы <red>не игрок<white>!"));
                    return true;
                }

                Location loc = player.getLocation();
                String text = "new Location(Bukkit.getWorld(\"world\"), " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + ", " + loc.getPitch() + "f, " + loc.getYaw() + "f);";
                player.sendMessage(DarkAPI.parse("<prefix><click:copy_to_clipboard:'" + text + "'>" +
                        "<hover:show_text:'<white>Нажмите <gray>[<green>ЛКМ<gray>]<white>, чтобы <yellow>скопировать'>" +
                        text +
                        "</hover></click>"));
            }
            case "startrec" -> {
                if(!sender.hasPermission("eternal.startrec")){
                    sender.sendMessage(DarkAPI.parse("<prefix>Вы <red>не администратор<white>!"));
                    return true;
                }

                if(!(sender instanceof Player player)){
                    sender.sendMessage(DarkAPI.parse("<prefix>Вы <red>не игрок<white>!"));
                    return true;
                }

                actionManager.clear();
                actionManager.record = player.getName();
                player.sendMessage(DarkAPI.parse("<prefix>Начинаем записывать ваши движения..."));
            }
            case "stoprec" -> {
                if(!sender.hasPermission("eternal.stoprec")){
                    sender.sendMessage(DarkAPI.parse("<prefix>Вы <red>не администратор<white>!"));
                    return true;
                }

                if(!(sender instanceof Player player)){
                    sender.sendMessage(DarkAPI.parse("<prefix>Вы <red>не игрок<white>!"));
                    return true;
                }

                actionManager.record = null;
                actionManager.saveActions("titan");
                player.sendMessage(DarkAPI.parse("<prefix>Останавливаем запись..."));
            }
            default -> helpCommand(sender, label);
        }
        return true;
    }

    private void helpCommand(CommandSender sender, String label){
        sender.sendMessage(DarkAPI.parse("<prefix>Использование:"));
        sender.sendMessage(DarkAPI.parse("<prefix><green>/" + label + " reload <gray>- <white>Перезагрузить <yellow>плагин"));
        sender.sendMessage(DarkAPI.parse("<prefix><green>/" + label + " settitan <gray>- <white>Установить <blue>титан"));
        sender.sendMessage(DarkAPI.parse("<prefix><green>/" + label + " starttitan <gray>- <white>Запустить <green>титана"));
    }
}