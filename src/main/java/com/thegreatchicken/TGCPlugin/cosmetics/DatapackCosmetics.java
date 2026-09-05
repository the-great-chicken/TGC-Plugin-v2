package com.thegreatchicken.TGCPlugin.cosmetics;

import github.scarsz.discordsrv.DiscordSRV;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import java.util.*;
import java.util.function.Supplier;

/** The single adapter for datapack objectives, tags and validated function hooks. */
public final class DatapackCosmetics implements CosmeticService.Game {
    private final Server server;
    private final Supplier<CosmeticCatalogue> catalogues;
    public DatapackCosmetics(Server server, Supplier<CosmeticCatalogue> catalogues) {
        this.server = server;
        this.catalogues = catalogues;
    }
    @Override public void requireLinkedOnline(CosmeticService.Identity identity) {
        var links = DiscordSRV.getPlugin().getAccountLinkManager();
        if (links == null || !links.isInCache(identity.discordId())) throw new CosmeticException(503, "LINK_UNAVAILABLE");
        if (!identity.playerUuid().equals(links.getUuidFromCache(identity.discordId()))) {
            throw new CosmeticException(403, "LINK_MISMATCH");
        }
        player(identity.playerUuid());
    }
    @Override public void requireReady() {
        for (var cosmetic : catalogues.get().entries()) objective(cosmetic.objective());
        if (!invoke("function sgp.cosmetics:api/ready")) throw new CosmeticException(503, "DATAPACK_UNAVAILABLE");
    }
    @Override public Set<String> unlocked(UUID uuid) {
        Player player = player(uuid);
        Set<String> unlocked = new HashSet<>();
        for (var cosmetic : catalogues.get().entries()) {
            var score = objective(cosmetic.objective()).getScoreFor(player);
            if (score.isScoreSet() && score.getScore() == 1) unlocked.add(cosmetic.id());
        }
        return unlocked;
    }
    @Override public Set<String> tags(UUID uuid) { return Set.copyOf(player(uuid).getScoreboardTags()); }
    @Override public boolean apply(UUID uuid, String function) {
        player(uuid);
        return invoke("execute as " + uuid + " at @s run function " + function);
    }
    private boolean invoke(String command) {
        var result = objective("sgp.cosmetics.api").getScore("#bridge");
        // A missing/unloaded function must never inherit the preceding call's success.
        result.setScore(0);
        server.dispatchCommand(server.getConsoleSender(),
                "minecraft:execute store result score #bridge sgp.cosmetics.api run " + command);
        return result.getScore() == 1;
    }
    private Objective objective(String name) {
        var objective = server.getScoreboardManager().getMainScoreboard().getObjective(name);
        if (objective == null) throw new CosmeticException(503, "DATAPACK_UNAVAILABLE");
        return objective;
    }
    private Player player(UUID uuid) {
        var player = server.getPlayer(uuid);
        if (player == null || !player.isOnline()) throw new CosmeticException(409, "OFFLINE");
        return player;
    }
}
