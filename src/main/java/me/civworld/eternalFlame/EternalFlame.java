package me.civworld.eternalFlame;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.civworld.eternalFlame.action.ActionManager;
import me.civworld.eternalFlame.circle.CircleManager;
import me.civworld.eternalFlame.command.EternalCommand;
import me.civworld.eternalFlame.config.Config;
import me.civworld.eternalFlame.event.TitanEvent;
import me.civworld.eternalFlame.listener.LeaveListener;
import me.civworld.eternalFlame.manager.NPCManager;
import me.civworld.eternalFlame.manager.ScoreboardManager;
import me.civworld.eternalFlame.spawner.ItemSpawner;
import me.civworld.eternalFlame.tab.EternalTabCompleter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.civworld.darkAPI.DarkAPI;

public final class EternalFlame extends JavaPlugin {
    private NPCManager npcManager;
    private Config config;

    @Override
    public void onEnable() {
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

        config = new Config(this);

        ActionManager actionManager = new ActionManager(config);

        DarkAPI.registerPlugin(this, config.get("plugin-prefix", String.class));

        ItemSpawner itemSpawner = new ItemSpawner(this, config);
        itemSpawner.updateSpawnings();

        ScoreboardManager scoreboardManager = new ScoreboardManager(this);

        npcManager = new NPCManager(this, config, scoreboardManager, actionManager);

        TitanEvent titanEvent = new TitanEvent(this, config, npcManager);
        titanEvent.updateHologram();

        protocolManager.addPacketListener(
                new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.SYSTEM_CHAT, PacketType.Play.Server.CHAT, PacketType.Play.Client.CHAT) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        Player player = event.getPlayer();

                        WrappedChatComponent chat = event.getPacket().getChatComponents().read(0);
                        String json = chat.getJson();

                        if (titanEvent.playersInGame.contains(player)) {
                            Component component = GsonComponentSerializer.gson().deserialize(json);
                            String plain = PlainTextComponentSerializer.plainText().serialize(component);

                            if (!plain.contains("❖ ")) {
                                event.setCancelled(true);
                            }
                        }
                    }
                }
        );

        CircleManager circleManager = new CircleManager(this, config, titanEvent);
        circleManager.updateCircleRound();

        getServer().getPluginManager().registerEvents(titanEvent, this);
        getServer().getPluginManager().registerEvents(new LeaveListener(config, titanEvent, npcManager), this);
        getServer().getPluginManager().registerEvents(actionManager, this);

        var command = getCommand("eternal");
        if(command != null){
            command.setExecutor(new EternalCommand(config, itemSpawner, circleManager, titanEvent, actionManager));
            command.setTabCompleter(new EternalTabCompleter());
        }

        actionManager.titanActions = actionManager.loadActions("titan");

        getLogger().info("Plugin is enabled.");
    }

    @Override
    public void onDisable() {
        if(npcManager != null) npcManager.forceShutdown();
        if(config != null) {
            config.saveActionsYaml();
        }
    }
}