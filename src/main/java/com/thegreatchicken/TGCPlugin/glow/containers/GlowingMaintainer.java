package com.thegreatchicken.TGCPlugin.glow.containers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class GlowingMaintainer {
    
    private GlowingMaintainer () {}

    private static GlowingMaintainer INSTANCE = new GlowingMaintainer();
    
    public static GlowingMaintainer instance () { return INSTANCE; }

    private HashMap<Player, GlowingClient> clients = new HashMap<>();
    private HashMap<LivingEntity, HashSet<GlowingEntity>> entities = new HashMap<>();

    private GlowingClient getClient (Player player) {
        GlowingClient client = clients.get(player);
        if (client != null) return client;

        clients.put(player, new GlowingClient(player));

        return clients.get(player);
    }
    private HashSet<GlowingEntity> getGlowingEntities (LivingEntity ent) {
        HashSet<GlowingEntity> entity = entities.get( ent );
        if (entity != null) return entity;

        entities.put(ent, new HashSet<>());
        return entities.get(ent);
    }

    private void start (GlowingEntity entity, int duration) {
        for (Player player : entity.clients) {
            GlowingClient client = getClient(player);

            client.add(entity);
        }

        entity.register();

        entity.start(duration);

        getGlowingEntities(entity.entity).add(entity);
    }
    void unregister (Player player, GlowingEntity entity) {
        GlowingClient client = getClient(player);

        client.clear(entity.entity);
    }
    void unregister (GlowingEntity entity) {
        getGlowingEntities(entity.entity).remove(entity);
    }

    public void addGlow (LivingEntity entity, Collection<Player> players, String color, int duration) {
        GlowingEntity glowingEntity = new GlowingEntity(entity, players, color);

        start(glowingEntity, duration);
    }
    public void removeGlow (LivingEntity entity) {
        for (GlowingEntity entry : List.copyOf( getGlowingEntities(entity) ))
            entry.unregisterAll();
    }
    public void removeGlow (LivingEntity entity, Player player) {
        getClient(player).clear(entity);
    }
    public void clear () {
        for (LivingEntity entity : entities.keySet())
            removeGlow(entity);
    }

}
