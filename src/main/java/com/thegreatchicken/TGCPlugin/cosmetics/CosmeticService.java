package com.thegreatchicken.TGCPlugin.cosmetics;

import java.time.Clock;
import java.util.*;
import java.util.function.Supplier;
import static com.thegreatchicken.TGCPlugin.cosmetics.CosmeticCatalogue.CATEGORIES;

/** All methods are called on the server thread. Transport has no access to game state. */
public final class CosmeticService {
    public record Identity(String discordId, UUID playerUuid) {}
    public record Change(Identity identity, String category, String cosmeticId, UUID requestId, long issuedAt) {}
    public record State(int protocolVersion, UUID playerUuid, long observedAt,
                        List<CosmeticCatalogue.Cosmetic> catalogue, Set<String> unlocked,
                        Map<String, String> equipment, List<String> issues) {}
    public interface Game {
        void requireLinkedOnline(Identity identity);
        void requireReady();
        Set<String> unlocked(UUID player);
        Set<String> tags(UUID player);
        boolean apply(UUID player, String function);
    }
    private record Receipt(Change change, String error, int status) {}
    private final Supplier<CosmeticCatalogue> catalogues;
    private final Game game;
    private final Clock clock;
    private final long startedAt;
    private final Map<UUID, Receipt> receipts = new HashMap<>();
    public static final long REQUEST_LIFETIME_MS = 30_000;

    public CosmeticService(Supplier<CosmeticCatalogue> catalogues, Game game, Clock clock) {
        this.catalogues = catalogues;
        this.game = game;
        this.clock = clock;
        this.startedAt = clock.millis();
    }
    public State state(Identity identity) {
        validateIdentity(identity);
        game.requireLinkedOnline(identity);
        game.requireReady();
        var catalogue = catalogues.get();
        Set<String> unlocked = Set.copyOf(game.unlocked(identity.playerUuid()));
        Set<String> tags = game.tags(identity.playerUuid());
        Map<String, String> equipment = new LinkedHashMap<>();
        List<String> issues = new ArrayList<>();
        for (String category : CATEGORIES) {
            var selected = catalogue.entries().stream()
                    .filter(c -> c.category().equals(category) && tags.contains(c.tag())).toList();
            equipment.put(category, selected.size() == 1 ? selected.getFirst().id() : null);
            if (selected.size() > 1) issues.add("multiple:" + category);
            for (var cosmetic : selected) {
                if (!unlocked.contains(cosmetic.id())) issues.add("locked:" + cosmetic.id());
            }
        }
        tags.stream().filter(tag -> CATEGORIES.stream().anyMatch(c -> tag.startsWith("sgp." + c + ".")))
                .filter(tag -> catalogue.get(tag.substring(4)) == null).sorted()
                .forEach(tag -> issues.add("unknown:" + tag.substring(4)));
        return new State(2, identity.playerUuid(), clock.millis(), catalogue.entries(),
                unlocked, Collections.unmodifiableMap(equipment), List.copyOf(issues));
    }
    public State change(Change change) {
        validateIdentity(change.identity());
        var catalogue = catalogues.get();
        if (!CATEGORIES.contains(change.category())) throw new CosmeticException(400, "INVALID_CATEGORY");
        var cosmetic = change.cosmeticId() == null ? null : catalogue.get(change.cosmeticId());
        if (change.cosmeticId() != null && cosmetic == null) throw new CosmeticException(400, "INVALID_COSMETIC");
        if (cosmetic != null && !cosmetic.category().equals(change.category())) throw new CosmeticException(400, "INVALID_CATEGORY");
        long now = clock.millis();
        if (change.requestId() == null || change.issuedAt() < startedAt || change.issuedAt() > now + 5_000
                || now - change.issuedAt() > REQUEST_LIFETIME_MS) throw new CosmeticException(409, "EXPIRED_REQUEST");

        // Even a duplicate must still belong to the currently linked player.
        game.requireLinkedOnline(change.identity());
        receipts.values().removeIf(r -> now - r.change().issuedAt() > REQUEST_LIFETIME_MS);
        Receipt previous = receipts.get(change.requestId());
        if (previous != null) {
            if (!previous.change().equals(change)) throw new CosmeticException(409, "REQUEST_CONFLICT");
            if (previous.error() != null) throw new CosmeticException(previous.status(), previous.error());
            // Return today's state, never replay an old mutation or old snapshot.
            return state(change.identity());
        }
        if (receipts.size() >= 1024) throw new CosmeticException(503, "BUSY");
        receipts.put(change.requestId(), new Receipt(change, "UNCONFIRMED", 503));
        try {
            State before = state(change.identity());
            if (cosmetic != null && !before.unlocked().contains(cosmetic.id())) throw new CosmeticException(403, "LOCKED");
            if (before.issues().stream().anyMatch(i -> i.startsWith("unknown:"))) {
                throw new CosmeticException(409, "INCONSISTENT_STATE");
            }
            String function = cosmetic == null ? "sgp.cosmetics:api/unequip/" + change.category() : cosmetic.equipFunction();
            if (!game.apply(change.identity().playerUuid(), function)) throw new CosmeticException(503, "APPLY_FAILED");
            State after = state(change.identity());
            if (!Objects.equals(after.equipment().get(change.category()), change.cosmeticId())
                    || after.issues().contains("multiple:" + change.category())) {
                throw new CosmeticException(503, "UNCONFIRMED");
            }
            receipts.put(change.requestId(), new Receipt(change, null, 200));
            return after;
        } catch (CosmeticException e) {
            receipts.put(change.requestId(), new Receipt(change, e.code(), e.status()));
            throw e;
        }
    }
    private void validateIdentity(Identity identity) {
        if (identity == null || identity.playerUuid() == null || identity.discordId() == null
                || !identity.discordId().matches("[0-9]{17,20}")) throw new CosmeticException(400, "INVALID_IDENTITY");
    }
}
