package com.thegreatchicken.TGCPlugin.utils;

import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Selector {
    public static Collection<Entity> select (CommandSender sender, String selector) {
        if (selector.length() >= 1 && selector.charAt(0) == '@')
            return Bukkit.selectEntities(sender, selector);
        return List.of( Bukkit.getPlayer(selector) );
    }
    public static Collection<Player> selectPlayers (CommandSender sender, String selector) {
        return select(sender, selector)
                .stream()
                .filter( (x) -> x instanceof Player )
                .map( (x) -> (Player) x )
                .toList();
    }
}
