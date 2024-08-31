package com.thegreatchicken.TGCPlugin.glow.containers;

import java.util.Collection;
import java.util.HashSet;

import com.thegreatchicken.TGCPlugin.glow.GlowManager;
import com.thegreatchicken.TGCPlugin.utils.Scheduler;
import com.thegreatchicken.TGCPlugin.utils.Scheduler.Task;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class GlowingEntity {
    public final LivingEntity entity;

    public final HashSet<Player> clients;    
    public final String color;

    public GlowingEntity (
        LivingEntity entity,
        Collection<Player> clients,
        String color
    ) {
        this.entity  = entity;
        this.clients = new HashSet<Player>(clients);
        this.color   = color;
    }

    void register () {
        for (Player client : clients)
            GlowManager.addGlow(client, entity, color);
    }
    void unregisterAll () {
        for (Player client : clients) {
            GlowingMaintainer.instance().unregister(client, this);
            GlowManager.removeGlow(client, entity);
        }

        clients.clear();

        GlowingMaintainer.instance().unregister(this);
    }
    void unregister (Player player) {
        if (!clients.contains(player)) return ;
        if (clients.size() == 1) {
            unregisterAll();
            return ;
        }

        GlowingMaintainer.instance().unregister(player, this);

        GlowManager.removeGlow(player, entity);

        clients.remove(player);
    }

    private Task task = null;
    public void start (int duration) {
        if (this.task != null) return ;
        if (duration <= 0) return ;

        this.task = Scheduler.schedule(() -> {
            this.unregisterAll();
        }, duration);
    }
    public void end () {
        task.cancelAndRun();
    }
}
