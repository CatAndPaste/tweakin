package com.github.sachin.tweakin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.github.sachin.tweakin.bottledcloud.BottledCloudItem;
import com.github.sachin.tweakin.bottledcloud.BottledCloudItem.CloudEntity;
import com.github.sachin.tweakin.bstats.Metrics;
import com.github.sachin.tweakin.bstats.Metrics.AdvancedPie;
import com.github.sachin.tweakin.bstats.Metrics.DrilldownPie;
import com.github.sachin.tweakin.bstats.Metrics.MultiLineChart;
import com.github.sachin.tweakin.bstats.Metrics.SimpleBarChart;
import com.github.sachin.tweakin.bstats.Metrics.SimplePie;
import com.github.sachin.tweakin.commands.CoreCommand;
import com.github.sachin.tweakin.gui.GuiListener;
import com.github.sachin.tweakin.lapisintable.LapisData;
import com.github.sachin.tweakin.lapisintable.LapisInTableTweak;
import com.github.sachin.tweakin.manager.TweakManager;
import com.github.sachin.tweakin.nbtapi.NBTAPI;
import com.github.sachin.tweakin.nbtapi.nms.NMSHelper;
import com.github.sachin.tweakin.utils.MiscItems;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import co.aikar.commands.PaperCommandManager;

public final class Tweakin extends JavaPlugin {

    private static Tweakin plugin;
    private String version;
    private PaperCommandManager commandManager;
    private TweakManager tweakManager;
    private NMSHelper nmsHelper;
    private boolean isEnabled;
    public boolean isProtocolLibEnabled;
    public boolean isFirstInstall;
    private MiscItems miscItems;
    private List<Player> placedPlayers = new ArrayList<>();



    @Override
    public void onEnable() {
        plugin = this;
        this.isEnabled = true;
        this.isFirstInstall = false;
        this.version = plugin.getServer().getClass().getPackage().getName().split("\\.")[3];
        NBTAPI nbtapi = new NBTAPI();
        if(!nbtapi.loadVersions(this,version)){
            getLogger().warning("Running incompataible version, stopping twekin");
            this.isEnabled = false;
            this.getServer().getPluginManager().disablePlugin(this);
            return;

        }

        reloadMiscItems();
        this.getServer().getPluginManager().registerEvents(new GuiListener(plugin), plugin);
        this.isProtocolLibEnabled = plugin.getServer().getPluginManager().isPluginEnabled("ProtocolLib");
        this.nmsHelper = nbtapi.getNMSHelper();
        this.saveDefaultConfig();
        this.reloadConfig();
        this.commandManager = new PaperCommandManager(this);
        this.tweakManager = new TweakManager(this);
        tweakManager.load();
        ConfigurationSerialization.registerClass(LapisData.class,"LapisData");
        commandManager.getCommandCompletions().registerCompletion("tweakitems", c -> tweakManager.getRegisteredItemNames());
        commandManager.getCommandCompletions().registerCompletion("tweaklist", c -> tweakManager.getTweakNames());
        commandManager.registerCommand(new CoreCommand(this));
        enabledBstats();
        getLogger().info("Tweakin loaded successfully");
    }

    public MiscItems getMiscItems() {
        return miscItems;
    }

    public void reloadMiscItems(){
        this.miscItems = new MiscItems(this);
    }

    @Override
    public void onDisable() {
        if(!isEnabled) return;
        LapisInTableTweak tweak = (LapisInTableTweak) getTweakManager().getTweakList().get(6);
        for(TweakItem i: tweakManager.getRegisteredItems()){
            if(i instanceof BottledCloudItem && i.registered){
                BottledCloudItem instance = (BottledCloudItem) i;
                for(CloudEntity entity : instance.clouds.values()){
                    entity.ticker.removeAll();
                }
            }
        }
        if(tweak.registered){
            tweak.saveLapisData();
        }
    }

    public String getVersion() {
        return version;
    }

    private void enabledBstats(){
        if(getConfig().getBoolean("metrics",true)){
            Metrics metrics = new Metrics(this,11786);
            getLogger().info("Enabling bstats...");
            
            metrics.addCustomChart(new AdvancedPie("Enabled-Tweaks", new Callable<Map<String, Integer>>() {
                @Override
                public Map<String, Integer> call() throws Exception {
                    Map<String, Integer> map = new HashMap<>();
                    for(BaseTweak tweak : getTweakManager().getTweakList()){
                        if(tweak.shouldEnable()){
                            map.put(tweak.getName(), 1);
                        }
                    }
                    return map;
                }
            }));
        }
    }

    public void addPlacedPlayer(Player player){
        placedPlayers.add(player);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,() -> {placedPlayers.remove(player);}, 3);
    }

    public List<Player> getPlacedPlayers() {
        return placedPlayers;
    }

    public NMSHelper getNmsHelper() {
        return nmsHelper;
    }

    public static Tweakin getPlugin() {
        return plugin;
    }

    public PaperCommandManager getCommandManager() {
        return commandManager;
    }

    public TweakManager getTweakManager() {
        return tweakManager;
    }

    public static NamespacedKey getKey(String key){
        return new NamespacedKey(plugin, key);
    }

    
}
