package com.thegreatchicken.TGCPlugin.glow;

import com.thegreatchicken.TGCPlugin.PluginLoader;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.executors.CommandArguments;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class GlowCommand {
    private static final FileConfiguration config = PluginLoader.PLUGIN.getConfig();
    private static final Long GlowTime = config.getLong("glow.time");
    private static final Integer MinDistance = config.getInt("glow.minDistance");
    private static final long GlowCooldown = config.getLong("glow.cooldown");
    private static final ChatFormatting GlowColor = ChatFormatting.getByName(config.getString("glow.color"));
    private static boolean UseGlow = true;
    private static final List<UUID> playerGlowUse = new ArrayList<>();


    public static void CommandRegister(){

        if (GlowColor==null || !GlowColor.isColor())
            throw new IllegalArgumentException("Invalid color in config.yml");

        new CommandTree("glow")
                .withPermission("tgcplugin.glow")
                .then(new LiteralArgument("add")
                        .then(new EntitySelectorArgument.ManyPlayers("clients")
                                .then(new EntitySelectorArgument.ManyEntities("entities")
                                        .executes((sender, args) -> {
                                            addGlow( args, null);
                                        })
                                        .then(new ChatColorArgument("color")
                                                .executes((sender, args) -> {
                                                    ChatColor color = args.getUnchecked("color");
                                                    ChatFormatting chatFormatting = ChatFormatting.getByName(color.name());
                                                    addGlow(args, chatFormatting);

                                                })

                                        )
                                )
                        )
                ).then(new LiteralArgument("time")
                        .then(new EntitySelectorArgument.ManyPlayers("clients")
                                .then(new EntitySelectorArgument.ManyEntities("entities")
                                        .then(new TimeArgument("duration")
                                                .executes((sender, args) -> {
                                                    addGlowTime(args,null);
                                                }).then(new ChatColorArgument("color")
                                                        .executes((sender, args) -> {
                                                            ChatColor color = args.getUnchecked("color");
                                                            ChatFormatting chatFormatting = ChatFormatting.getByName(color.name());
                                                            addGlowTime(args, chatFormatting);
                                                        })
                                                )
                                        )
                                )
                        )
                )
                .then(new LiteralArgument("remove")
                        .then(new EntitySelectorArgument.ManyEntities("entities")
                                .executes((sender, args) -> {
                                    List<Entity> entities = args.getUnchecked("entities");
                                    for (Entity entity : entities){
                                        Glow.removeGlow(entity);
                                    }

                                })
                        )

                ).register();

        new CommandTree("useglow")
                .executesPlayer((player, args) -> {
                    if (!UseGlow) return;
                    if (playerGlowUse.contains(player.getUniqueId())) {
                        TextComponent textComponent = new TextComponent();
                        textComponent.setText("You can't use glow now");
                        textComponent.setColor(net.md_5.bungee.api.ChatColor.RED);
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, textComponent);
                        return;
                    }

                    HashMap<Player, Tuple<ChatFormatting,Long>> playerChatFormattingHashMap = new HashMap<>();
                    playerChatFormattingHashMap.put(player,new Tuple<>(GlowColor,GlowTime));
                    for (Entity entity : Bukkit.getOnlinePlayers()){
                        if (entity.getLocation().distance(player.getLocation()) < MinDistance) continue;
                        Glow.setGlowTime( entity,playerChatFormattingHashMap);
                    }

                    playerGlowUse.add(player.getUniqueId());
                    GlowCooldown(player);

                }).then(new LiteralArgument("toggle")
                        .withPermission("tgcplugin.glow")
                        .executes(((sender, args) -> {
                            UseGlow = !UseGlow;
                            sender.sendMessage(ChatColor.GREEN + "Glow : " + (UseGlow ? "on" : "off"));
                        }))).register();
    }

    private static void addGlow(CommandArguments args, ChatFormatting chatFormatting) {
        List<Player> players = args.getUnchecked("clients");
        List<Entity> entities = args.getUnchecked("entities");
        HashMap<Player, ChatFormatting> playerChatFormattingHashMap = new HashMap<>();
        for (Player player : players){
            playerChatFormattingHashMap.put(player,chatFormatting);
        }
        for (Entity entity : entities){
            Glow.setGlow(entity,playerChatFormattingHashMap);
        }
    }

    private static void addGlowTime(CommandArguments args, ChatFormatting chatFormatting) {
        List<Player> players = args.getUnchecked("clients");
        List<Entity> entities = args.getUnchecked("entities");
        Integer duration = args.getUnchecked("duration");
        Tuple<ChatFormatting,Long> tuple = new Tuple<>(chatFormatting,duration.longValue());
        HashMap<Player, Tuple<ChatFormatting,Long>> playerChatFormattingHashMap = new HashMap<>();
        for (Player player : players){
            playerChatFormattingHashMap.put(player,tuple);
        }
        for (Entity entity : entities){
            Glow.setGlowTime(entity,playerChatFormattingHashMap);
        }
    }

    private static void GlowCooldown(Player player) {
        Bukkit.getScheduler().runTaskLater(PluginLoader.PLUGIN, () -> {
            playerGlowUse.remove(player.getUniqueId());
        }, GlowCooldown);
    }
}
