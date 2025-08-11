package me.angel.hotbarmenuitem;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class MenuItemManager {

    private final HotbarMenuItem plugin;
    private final ItemStack menuItem;
    private final int slot;
    private final List<String> commands;
    private final String clickType;
    private final boolean bedrockOnly;
    private final String permission;

    public MenuItemManager(HotbarMenuItem plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();

        Material material = Material.valueOf(config.getString("item.material", "NETHER_STAR"));
        slot = config.getInt("item.slot", 4);
        clickType = config.getString("item.click-type", "RIGHT");
        commands = config.getStringList("item.commands");
        bedrockOnly = config.getBoolean("item.bedrock-only", false);
        permission = config.getString("item.permission", "");

        menuItem = new ItemStack(material);
        ItemMeta meta = menuItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(config.getString("item.name", "§6§lMenú"));
            meta.setLore(config.getStringList("item.lore"));
            meta.setUnbreakable(true);
            menuItem.setItemMeta(meta);
        }
    }

    public void giveMenuItem(Player player) {
        if (bedrockOnly && !isBedrockPlayer(player)) return;
        if (!permission.isEmpty() && !player.hasPermission(permission)) return;

        // Limpiar duplicados antes de asignar
        cleanupDuplicates(player);

        // No asignar si ya tiene el item en el slot correcto
        ItemStack current = player.getInventory().getItem(slot);
        if (isMenuItem(current)) return;

        player.getInventory().setItem(slot, menuItem.clone());
    }

    /**
     * Limpia duplicados del ítem del menú en todo el inventario excepto en el slot correcto
     */
    public void cleanupDuplicates(Player player) {
        // Limpiar inventario principal (slots 0-35)
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            if (i == slot) continue;
            ItemStack item = player.getInventory().getItem(i);
            if (isMenuItem(item)) {
                player.getInventory().setItem(i, null);
            }
        }

        // Limpiar cursor si tiene el ítem
        if (isMenuItem(player.getItemOnCursor())) {
            player.setItemOnCursor(null);
        }

        // Limpiar slots de armadura (por si acaso)
        ItemStack[] armorContents = player.getInventory().getArmorContents();
        boolean armorChanged = false;
        for (int i = 0; i < armorContents.length; i++) {
            if (isMenuItem(armorContents[i])) {
                armorContents[i] = null;
                armorChanged = true;
            }
        }
        if (armorChanged) {
            player.getInventory().setArmorContents(armorContents);
        }

        // Limpiar offhand (mano secundaria)
        if (isMenuItem(player.getInventory().getItemInOffHand())) {
            player.getInventory().setItemInOffHand(null);
        }
    }

    /**
     * Asegura que el ítem esté en su slot correcto y limpia duplicados
     */
    public void ensureCorrectPlacement(Player player) {
        cleanupDuplicates(player);

        // Asegurar que el item esté en su slot correcto
        ItemStack correctSlotItem = player.getInventory().getItem(slot);
        if (!isMenuItem(correctSlotItem)) {
            player.getInventory().setItem(slot, menuItem.clone());
        }
    }

    public boolean isMenuItem(ItemStack item) {
        return item != null && item.isSimilar(menuItem);
    }

    public void executeCommands(Player player) {
        for (String cmd : commands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
        }
    }

    public int getSlot() {
        return slot;
    }

    public String getClickType() {
        return clickType;
    }

    public ItemStack getItemClone() {
        return menuItem.clone();
    }

    // ✔️ Detecta si es Bedrock
    private boolean isBedrockPlayer(Player player) {
        return player.getClass().getSimpleName().equalsIgnoreCase("FloodgatePlayer") ||
                player.getEffectivePermissions().stream().anyMatch(p -> p.getPermission().toLowerCase().contains("floodgate"));
    }
}