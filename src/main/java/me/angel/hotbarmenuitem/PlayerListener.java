package me.angel.hotbarmenuitem;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListener implements Listener {

    private final MenuItemManager manager;

    public PlayerListener(MenuItemManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        manager.giveMenuItem(e.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        // Dar el item con delay para asegurar que el respawn esté completo
        new BukkitRunnable() {
            @Override
            public void run() {
                manager.giveMenuItem(e.getPlayer());
            }
        }.runTaskLater(HotbarMenuItem.getInstance(), 1L);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getItem() == null || !manager.isMenuItem(e.getItem())) return;

        Action action = e.getAction();
        String click = manager.getClickType();

        if ((click.equalsIgnoreCase("RIGHT") && (action.name().contains("RIGHT"))) ||
                (click.equalsIgnoreCase("LEFT") && (action.name().contains("LEFT"))) ||
                (click.equalsIgnoreCase("ALL"))) {

            e.setCancelled(true);
            manager.executeCommands(e.getPlayer());
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = e.getCurrentItem();
        ItemStack cursor = e.getCursor();

        // Verificar si está intentando mover el menu item
        boolean clickedIsMenuItem = clicked != null && manager.isMenuItem(clicked);
        boolean cursorIsMenuItem = cursor != null && manager.isMenuItem(cursor);

        if (clickedIsMenuItem || cursorIsMenuItem) {
            e.setCancelled(true);

            // Restaurar el item a su slot correcto después de cancelar
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Usar el método mejorado para asegurar posición correcta
                    manager.ensureCorrectPlacement(player);
                }
            }.runTaskLater(HotbarMenuItem.getInstance(), 1L);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;

        // Verificar si está arrastrando el menu item
        ItemStack draggedItem = e.getOldCursor();
        if (manager.isMenuItem(draggedItem)) {
            e.setCancelled(true);
            return;
        }

        // Verificar si alguno de los items nuevos es el menu item
        for (ItemStack item : e.getNewItems().values()) {
            if (manager.isMenuItem(item)) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (manager.isMenuItem(e.getItemDrop().getItemStack())) {
            e.setCancelled(true);

            // Restaurar el item a su slot después de cancelar el drop
            new BukkitRunnable() {
                @Override
                public void run() {
                    manager.giveMenuItem(e.getPlayer());
                }
            }.runTaskLater(HotbarMenuItem.getInstance(), 1L);
        }
    }
}