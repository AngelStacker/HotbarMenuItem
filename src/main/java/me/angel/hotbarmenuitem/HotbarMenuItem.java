package me.angel.hotbarmenuitem;

import org.bukkit.plugin.java.JavaPlugin;

public class HotbarMenuItem extends JavaPlugin {

    private static HotbarMenuItem instance;
    private MenuItemManager itemManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        itemManager = new MenuItemManager(this);

        getServer().getPluginManager().registerEvents(new PlayerListener(itemManager), this);
    }

    public static HotbarMenuItem getInstance() {
        return instance;
    }

    public MenuItemManager getItemManager() {
        return itemManager;
    }
}