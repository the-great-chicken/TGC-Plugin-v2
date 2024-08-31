package com.thegreatchicken.TGCPlugin.glow.containers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class GlowingClient {
    private final Player player;

    private HashMap<LivingEntity, GlowingEntity> entities;

    public GlowingClient (Player player) {
        this.player = player;

        this.entities = new HashMap<>();
    }

    public void clear (LivingEntity entity) {
        //System.out.println("CLEARING " + entity);
        //for(Map.Entry<LivingEntity, GlowingEntity> entityEntry : entities.entrySet()) {
        //    System.out.println(entityEntry.getKey() + " " + entityEntry.getValue().entity);
        //}
        GlowingEntity glowingEntity = entities.get(entity);
        //System.out.println("FOUND " + glowingEntity);

        if (glowingEntity == null) return ;

        entities.remove(entity);

        glowingEntity.unregister(player);
    }
    public void add (GlowingEntity glowingEntity) {
        clear(glowingEntity.entity);

        entities.put(glowingEntity.entity, glowingEntity);
    }
}
