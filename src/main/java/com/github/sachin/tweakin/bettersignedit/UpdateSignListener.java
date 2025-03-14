package com.github.sachin.tweakin.bettersignedit;

import java.lang.reflect.InvocationTargetException;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.github.sachin.tweakin.Tweakin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class UpdateSignListener extends PacketAdapter{

    private BetterSignEditTweak instance;
    private Tweakin plugin;
    
    public UpdateSignListener(Tweakin plugin,BetterSignEditTweak instance) {
        super(plugin, PacketType.Play.Client.UPDATE_SIGN);
        this.instance = instance;
        this.plugin = plugin;
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        Player player = event.getPlayer();
        BlockPosition pos = packet.getBlockPositionModifier().read(0);
        Block block = player.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        if(!instance.lines.contains(player)) return;
        event.setCancelled(true);
        new BukkitRunnable(){
            @Override
            public void run() {
                if(block.getType().toString().endsWith("_SIGN")){
                    
                    Sign sign = (Sign) block.getState();
                    if(sign.getPersistentDataContainer().has(instance.key, PersistentDataType.STRING)){
                        sign.getPersistentDataContainer().remove(instance.key);
                        String[] lines = packet.getStringArrays().read(0);
                        int i = 0;
                        for(String l : lines){
                            sign.setLine(i, l);
                            i++;
                        }
                        sign.update(true);
                        // Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> {sign.update(true);},20);
                        PacketContainer packet = instance.manager.createPacket(PacketType.Play.Client.UPDATE_SIGN);
                        packet.getBlockPositionModifier().write(0, pos);
                        packet.getStringArrays().write(0, lines);
                        try {
                            instance.manager.recieveClientPacket(player, packet);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                        instance.lines.remove(player);
                    }
                }
            
            }
        }.runTask(plugin);
    }

}
