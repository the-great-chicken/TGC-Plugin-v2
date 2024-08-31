package com.thegreatchicken.TGCPlugin.glow.containers;

import java.util.HashMap;

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
        GlowingEntity glowingEntity = entities.get(entity);

        if (glowingEntity == null) return ;

        entities.remove(entity);

        glowingEntity.unregister(player);
    }
    public void add (LivingEntity entity, GlowingEntity glowingEntity) {
        clear(entity);

        entities.put(entity, glowingEntity);
    }
}
