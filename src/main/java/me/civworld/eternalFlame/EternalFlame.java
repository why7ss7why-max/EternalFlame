package me.civworld.eternalFlame;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.civworld.eternalFlame.circle.CircleManager;
import me.civworld.eternalFlame.command.EternalCommand;
import me.civworld.eternalFlame.config.Config;
import me.civworld.eternalFlame.event.TitanEvent;
import me.civworld.eternalFlame.listener.LeaveListener;
import me.civworld.eternalFlame.npc.NPCManager;
import me.civworld.eternalFlame.spawner.ItemSpawner;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.civworld.darkAPI.DarkAPI;

import java.util.ArrayList;
import java.util.List;

public final class EternalFlame extends JavaPlugin {
    private final List<NPC> npcs = new ArrayList<>();
    private ProtocolManager protocolManager;

    @Override
    public void onEnable() {
        protocolManager = ProtocolLibrary.getProtocolManager();

        Config config = new Config(this);
        config.loadConfig();

        DarkAPI.registerPlugin(this, config.get("plugin-prefix", String.class));

        ItemSpawner itemSpawner = new ItemSpawner(this, config);
        itemSpawner.updateSpawnings();

        NPCManager npcManager = new NPCManager(this, config);

        TitanEvent titanEvent = new TitanEvent(this, config, npcManager, protocolManager);
        titanEvent.updateHologram();

        protocolManager.addPacketListener(
                new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.SYSTEM_CHAT, PacketType.Play.Server.CHAT, PacketType.Play.Client.CHAT) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        Player player = event.getPlayer();

                        WrappedChatComponent chat = event.getPacket().getChatComponents().read(0);
                        String json = chat.getJson();

                        if (titanEvent.playersBlindness.contains(player)) {
                            Component component = GsonComponentSerializer.gson().deserialize(json);
                            String plain = PlainTextComponentSerializer.plainText().serialize(component);

                            if (!plain.contains("Титан")) {
                                event.setCancelled(true);
                            }
                        }
                    }
                }
        );

        CircleManager circleManager = new CircleManager(this, config, titanEvent);
        circleManager.updateCircleRound();

        getServer().getPluginManager().registerEvents(titanEvent, this);
        getServer().getPluginManager().registerEvents(new LeaveListener(this, config, titanEvent, npcManager), this);

        DarkAPI.setCommand("eternal", new EternalCommand(this, config, itemSpawner, circleManager, titanEvent));

        getLogger().info("Plugin is enabled.");
    }

    @Override
    public void onDisable() {
        for(NPC npc : npcs){
            CitizensAPI.getNPCRegistry().deregister(npc);
            npc.despawn();
            npc.destroy();
        }
    }
}