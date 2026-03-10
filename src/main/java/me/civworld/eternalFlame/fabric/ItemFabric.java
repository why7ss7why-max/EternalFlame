package me.civworld.eternalFlame.fabric;

import me.civworld.eternalFlame.EternalFlame;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import ru.civworld.darkAPI.DarkAPI;

import java.util.List;

public class ItemFabric {
    public static ItemStack getTitanShard(int amount){
        ItemStack item = new ItemStack(Material.DISC_FRAGMENT_5);
        ItemMeta meta = item.getItemMeta();
        if(meta == null) return null;

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);

        meta.displayName(DarkAPI.parse("<#3446eb>❈ <white>Осколок Титана <#3446eb>❈"));
        meta.lore(List.of(
                DarkAPI.parse(" <#3446eb>▪ <white>Данный <green>предмет <white>необходим"),
                DarkAPI.parse(" <#3446eb>▪ <white>для мероприятия <#3446eb>Падение Титана"),
                DarkAPI.parse(" "),
                DarkAPI.parse(" <#3446eb>→ <white>Собери таких <yellow>" + amount + " шт."),
                DarkAPI.parse(" <#3446eb>→ <white>и отправляйся на <red>битву<white>!")
        ));

        NamespacedKey namespacedKey = new NamespacedKey(EternalFlame.getPlugin(EternalFlame.class), "eternalflame");
        meta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, "titan_shard");

        item.setItemMeta(meta);
        return item;
    }
}