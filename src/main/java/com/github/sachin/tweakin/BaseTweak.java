package com.github.sachin.tweakin;

import java.util.ArrayList;
import java.util.List;

import com.github.sachin.tweakin.manager.TweakManager;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import co.aikar.commands.BaseCommand;

public abstract class BaseTweak {

    protected final Tweakin plugin;
    private final TweakManager tweakManager;
    private boolean shouldEnable;
    private ConfigurationSection config;
    private String configKey;
    public boolean registered;

    public BaseTweak(Tweakin plugin,String configKey){
        this.plugin = plugin;
        this.configKey = configKey;
        this.tweakManager = plugin.getTweakManager();
        this.config = plugin.getConfig().getConfigurationSection(configKey);
        if(config == null || !config.contains("enabled")){
            plugin.getLogger().info("Could not found config section for "+configKey+", ignoring the tweak module..");
            this.shouldEnable = false;
            return;
        }
        this.reload();
        this.shouldEnable = config.getBoolean("enabled",true);
    }



    public TweakManager getTweakManager() {
        return tweakManager;
    }
    
    public boolean shouldEnable() {
        return shouldEnable;
    }

    public boolean isPluginEnabled(String name){
        return plugin.getServer().getPluginManager().isPluginEnabled(name);
    }


    public ConfigurationSection getConfig() {
        return config;
    }

    public String getName() {
        return configKey;
    }

    public List<String> getBlackListWorlds(){
        if(config.contains("black-list-worlds")){
            List<String> list = config.getStringList("black-list-worlds");
            if(list != null){
                return list;
            }
        }
        return new ArrayList<>();
    }

    


    protected void registerEvents(Listener listener) {
        this.plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    protected void unregisterEvents(Listener listener) {
        HandlerList.unregisterAll(listener);
    }

    protected void registerCommands(BaseCommand command){
        plugin.getCommandManager().registerCommand(command);
    }

    protected void unregisterCommands(BaseCommand command){
        plugin.getCommandManager().unregisterCommand(command);
    }

    public Tweakin getPlugin() {
        return plugin;
    }

    public void register(){
        if(this instanceof Listener){
            registerEvents((Listener)this);
        }
        registered = true;
    }

    public void unregister(){
        if(this instanceof Listener){
            unregisterEvents((Listener)this);
        }
        registered = false;
    }

    public void reload() {
        this.config = plugin.getConfig().getConfigurationSection(configKey);
        this.shouldEnable = config.getBoolean("enabled");
    }
}
