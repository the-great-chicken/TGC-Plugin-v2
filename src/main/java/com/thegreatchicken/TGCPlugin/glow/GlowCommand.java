package com.thegreatchicken.TGCPlugin.glow;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.thegreatchicken.TGCPlugin.PluginLoader;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

import static net.kyori.adventure.text.Component.text;

public final class GlowCommand {
    private static final FileConfiguration config = PluginLoader.PLUGIN.getConfig();
    private static final Long GlowTime = config.getLong("glow.time");
    private static final Integer MinDistance = config.getInt("glow.minDistance");
    private static final long GlowCooldown = config.getLong("glow.cooldown");
    private static final NamedTextColor GlowColor = NamedTextColor.NAMES.value(config.getString("glow.color","white"));
    private static boolean UseGlow = true;
    private static final Map<UUID,Long> playerGlowUse = new HashMap();

    public static void commandRegister(Commands registry) {
        var glow_command = Commands.literal("glow")
                .requires(source -> source.getSender().hasPermission("tgcplugin.glow"))
                .then(Commands.literal("add")
                        .then(Commands.argument("client",ArgumentTypes.players())
                                .then(Commands.argument("entities",ArgumentTypes.entities())
                                        .executes(context ->
                                            addGlow( context, null)
                                        )
                                        .then(Commands.argument("color",ArgumentTypes.namedColor())
                                                .executes(ctx -> {
                                                    NamedTextColor color =ctx.getArgument("color",NamedTextColor.class);
                                                    return addGlow(ctx, color);
                                                })
                                        )
                                )
                        )
                ).then(Commands.literal("time")
                        .then(Commands.argument("client",ArgumentTypes.players())
                                .then(Commands.argument("entities",ArgumentTypes.entities())
                                        .then(Commands.argument("duration",ArgumentTypes.time())
                                                .executes(ctx ->
                                                    addGlowTime(ctx,null)
                                                ).then(Commands.argument("color",ArgumentTypes.namedColor())
                                                    .executes(ctx -> {
                                                        NamedTextColor color = ctx.getArgument("color",NamedTextColor.class);
                                                        return addGlowTime(ctx, color);
                                                    })
                                            )
                                        )
                                )
                        )
                )
                .then(Commands.literal("remove")
                        .then(Commands.argument("entities",ArgumentTypes.entities())
                                .executes((ctx -> {
                                    final var entitySelectorArgumentResolver = ctx.getArgument("entities", EntitySelectorArgumentResolver.class);
                                    final List<Entity> entities = entitySelectorArgumentResolver.resolve(ctx.getSource());
                                    for (Entity entity : entities){
                                        Glow.removeGlow(entity);
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )

                )).build();

        registry.register(glow_command,"make a entity glow for a specific player and amount of time");

        var use_glow = Commands.literal("useglow")
                .requires(source -> source.getSender() instanceof Player)
                .executes(ctx -> {
                    if (!UseGlow) ctx.getSource().getSender().sendMessage(text("Glow use not enabled").color(NamedTextColor.RED));
                    Player player = (Player) ctx.getSource().getExecutor();
                    if (playerGlowUse.containsKey(player.getUniqueId())) {
                        long startTime = playerGlowUse.get(player.getUniqueId());
                        long time = startTime - System.currentTimeMillis() + GlowTime*50;
                        player.sendActionBar(text("You need to wait "+time/1000+"s").color(NamedTextColor.RED));
                        return Command.SINGLE_SUCCESS;
                    }
                    HashMap<Player, Pair<NamedTextColor,Long>> playerChatFormattingHashMap = new HashMap<>();
                    playerChatFormattingHashMap.put(player,new MutablePair<>(GlowColor,GlowTime));
                    for (Entity entity : Bukkit.getOnlinePlayers()){
                        if (entity.getLocation().distance(player.getLocation()) > MinDistance)
                            Glow.setGlowTime( entity,playerChatFormattingHashMap);
                    }
                    playerGlowUse.put(player.getUniqueId(),System.currentTimeMillis());
                    GlowCooldown(player);
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.literal("toggle")
                        .requires(source -> source.getSender().hasPermission("tgcplugin.glow"))
                        .executes(ctx -> {
                            UseGlow = !UseGlow;
                            ctx.getSource().getSender().sendMessage(text("Glow : " + (UseGlow ? "on" : "off")).color(NamedTextColor.GREEN));
                            return Command.SINGLE_SUCCESS;
                        })).build();
        registry.register(use_glow,"allows the player to spot other players around them");
    }

    private static int addGlow(CommandContext<CommandSourceStack> ctx,NamedTextColor color) throws CommandSyntaxException {
        final var playerSelectorArgumentResolver = ctx.getArgument("client", PlayerSelectorArgumentResolver.class);
        final List<Player> players = playerSelectorArgumentResolver.resolve(ctx.getSource());
        final var entitySelectorArgumentResolver = ctx.getArgument("entities", EntitySelectorArgumentResolver.class);
        final List<Entity> entities = entitySelectorArgumentResolver.resolve(ctx.getSource());
        HashMap<Player, NamedTextColor> playerColorHashMap = new HashMap<>();
        for (Player player : players){
            playerColorHashMap.put(player,color);
        }
        for (Entity entity : entities){
            Glow.setGlow(entity,playerColorHashMap);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int addGlowTime(CommandContext<CommandSourceStack> ctx, NamedTextColor color) throws CommandSyntaxException {
        final var playerSelectorArgumentResolver = ctx.getArgument("client", PlayerSelectorArgumentResolver.class);
        final List<Player> players = playerSelectorArgumentResolver.resolve(ctx.getSource());
        final var entitySelectorArgumentResolver = ctx.getArgument("entities", EntitySelectorArgumentResolver.class);
        final List<Entity> entities = entitySelectorArgumentResolver.resolve(ctx.getSource());
        Integer duration = ctx.getArgument("duration",Integer.class);
        Pair<NamedTextColor,Long> tuple = new MutablePair<>(color,duration.longValue());
        HashMap<Player, Pair<NamedTextColor,Long>> playerChatFormattingHashMap = new HashMap<>();
        for (Player player : players){
            playerChatFormattingHashMap.put(player,tuple);
        }
        for (Entity entity : entities){
            Glow.setGlowTime(entity,playerChatFormattingHashMap);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static void GlowCooldown(Player player) {
        Bukkit.getScheduler().runTaskLater(PluginLoader.PLUGIN, () -> {
            playerGlowUse.remove(player.getUniqueId());
        }, GlowCooldown);
    }
}
