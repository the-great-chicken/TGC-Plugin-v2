package com.thegreatchicken.TGCPlugin.cosmetics;

import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CosmeticServiceTest {
    final UUID player = UUID.fromString("11111111-1111-4111-8111-111111111111");
    final CosmeticService.Identity identity = new CosmeticService.Identity("111111111111111111", player);
    CosmeticCatalogue catalogue = new CosmeticCatalogue(List.of(
            new CosmeticCatalogue.Cosmetic("particle.cloud", "particle", "Nuage", 0),
            new CosmeticCatalogue.Cosmetic("particle.smoke", "particle", "Fumée", 1),
            new CosmeticCatalogue.Cosmetic("kill.anvil", "kill", "Enclume", 0)));
    final MutableClock clock = new MutableClock();
    final FakeGame game = new FakeGame();
    final CosmeticService service = new CosmeticService(() -> catalogue, game, clock);

    @Test void catalogueAndSnapshotCoverThreeSlots() {
        assertEquals(3, catalogue.entries().size());
        game.tags.add("sgp.particle.cloud");
        var state = service.state(identity);
        assertEquals("particle.cloud", state.equipment().get("particle"));
        assertNull(state.equipment().get("intensity"));
        assertEquals(3, state.equipment().size());
    }
    @Test void equipReplaceUnequipAndRepeatsAreSafe() {
        var first = change("particle", "particle.cloud");
        assertEquals("particle.cloud", service.change(first).equipment().get("particle"));
        service.change(first);
        assertEquals(1, game.applies);
        service.change(change("particle", "particle.smoke"));
        // A delayed duplicate returns current state without restoring the old selection.
        assertEquals("particle.smoke", service.change(first).equipment().get("particle"));
        assertEquals(2, game.applies);
        service.change(change("particle", null));
        service.change(change("particle", null));
        assertNull(service.state(identity).equipment().get("particle"));
        assertEquals(Set.of("unrelated"), game.tags);
    }
    @Test void invalidCategoryAndCosmeticsNeverMutate() {
        error("INVALID_CATEGORY", () -> service.change(change("title", null)));
        error("INVALID_CATEGORY", () -> service.change(change("kill", "particle.cloud")));
        error("INVALID_COSMETIC", () -> service.change(change("particle", "particle.missing")));
        error("INVALID_COSMETIC", () -> service.change(change("particle", "particle.cloud\nkill @a")));
        assertEquals(0, game.applies);
    }
    @Test void lockedCosmeticKeepsExistingSelection() {
        game.tags.add("sgp.particle.cloud");
        game.unlocked.remove("particle.smoke");
        error("LOCKED", () -> service.change(change("particle", "particle.smoke")));
        assertTrue(game.tags.contains("sgp.particle.cloud"));
        assertEquals(0, game.applies);
    }
    @Test void linkChangesAndOtherPlayerRequestsAreRejectedIncludingRetries() {
        var request = change("particle", "particle.cloud");
        service.change(request);
        game.linked = false;
        error("LINK_MISMATCH", () -> service.change(request));
        game.linked = true;
        error("LINK_MISMATCH", () -> service.state(new CosmeticService.Identity(identity.discordId(), UUID.randomUUID())));
        error("INVALID_IDENTITY", () -> service.state(new CosmeticService.Identity("x", player)));
        assertEquals(1, game.applies);
    }
    @Test void offlineAndUnloadedDatapackRejectChanges() {
        game.online = false;
        error("OFFLINE", () -> service.change(change("kill", null)));
        game.online = true;
        game.ready = false;
        error("DATAPACK_UNAVAILABLE", () -> service.change(change("kill", null)));
        assertEquals(0, game.applies);
    }
    @Test void explicitHookFailureAndWrongReadbackCannotSucceed() {
        game.hookSuccess = false;
        var request = change("particle", "particle.cloud");
        error("APPLY_FAILED", () -> service.change(request));
        error("APPLY_FAILED", () -> service.change(request));
        assertEquals(1, game.applies);
        game.hookSuccess = true;
        game.changeTags = false;
        error("UNCONFIRMED", () -> service.change(change("particle", "particle.cloud")));
    }
    @Test void reusedRequestIdsWithDifferentPayloadAreRejected() {
        var request = change("particle", "particle.cloud");
        service.change(request);
        var altered = new CosmeticService.Change(identity, "particle", "particle.smoke", request.requestId(), request.issuedAt());
        error("REQUEST_CONFLICT", () -> service.change(altered));
        assertEquals(1, game.applies);
    }
    @Test void expiredFutureAndPreviousProcessRequestsAreRejected() {
        var request = change("particle", "particle.cloud");
        clock.now += 30_001;
        error("EXPIRED_REQUEST", () -> service.change(request));
        error("EXPIRED_REQUEST", () -> service.change(new CosmeticService.Change(identity, "kill", null, UUID.randomUUID(), clock.now + 6_000)));
        var restarted = new CosmeticService(() -> catalogue, game, clock);
        error("EXPIRED_REQUEST", () -> restarted.change(request));
        assertEquals(0, game.applies);
    }
    @Test void inconsistentSelectionsAreReportedAndKnownConflictsCanBeCleared() {
        game.tags.addAll(List.of("sgp.particle.cloud", "sgp.particle.smoke", "sgp.kill.anvil"));
        var state = service.state(identity);
        assertTrue(state.issues().contains("multiple:particle"));
        assertTrue(state.issues().contains("locked:kill.anvil"));
        service.change(change("particle", null));
        assertFalse(service.state(identity).issues().contains("multiple:particle"));
        game.tags.add("sgp.particle.removed");
        assertTrue(service.state(identity).issues().contains("unknown:particle.removed"));
        error("INCONSISTENT_STATE", () -> service.change(change("particle", "particle.cloud")));
    }
    @Test void catalogueReloadAddsAndRemovesCosmeticsWithoutRecreatingService() {
        var cloud = change("particle", "particle.cloud");
        service.change(cloud);
        catalogue = new CosmeticCatalogue(List.of(
                new CosmeticCatalogue.Cosmetic("particle.cloud", "particle", "Nouveau nuage", 0),
                new CosmeticCatalogue.Cosmetic("particle.spark", "particle", "Étincelle", 1)));
        game.unlocked.remove("particle.smoke");
        game.unlocked.add("particle.spark");
        assertEquals("Nouveau nuage", service.state(identity).catalogue().getFirst().name());
        service.change(change("particle", "particle.spark"));
        assertEquals("particle.spark", service.change(cloud).equipment().get("particle"));
        assertEquals(2, game.applies, "Reload keeps request receipts, so an old request cannot restore equipment");
        error("INVALID_COSMETIC", () -> service.change(change("particle", "particle.smoke")));
        catalogue = new CosmeticCatalogue(List.of(new CosmeticCatalogue.Cosmetic("particle.cloud", "particle", "Nuage", 0)));
        game.unlocked.remove("particle.spark");
        var state = service.state(identity);
        assertNull(state.equipment().get("particle"));
        assertTrue(state.issues().contains("unknown:particle.spark"));
        error("INCONSISTENT_STATE", () -> service.change(change("particle", "particle.cloud")));
    }
    CosmeticService.Change change(String category, String id) {
        return new CosmeticService.Change(identity, category, id, UUID.randomUUID(), clock.millis());
    }
    void error(String expected, org.junit.jupiter.api.function.Executable action) {
        assertEquals(expected, assertThrows(CosmeticException.class, action).code());
    }
    class FakeGame implements CosmeticService.Game {
        Set<String> unlocked = new HashSet<>(Set.of("particle.cloud", "particle.smoke"));
        Set<String> tags = new HashSet<>(Set.of("unrelated"));
        boolean linked = true, online = true, ready = true, hookSuccess = true, changeTags = true;
        int applies;
        public void requireLinkedOnline(CosmeticService.Identity supplied) {
            if (!linked || !identity.equals(supplied)) throw new CosmeticException(403, "LINK_MISMATCH");
            if (!online) throw new CosmeticException(409, "OFFLINE");
        }
        public void requireReady() { if (!ready) throw new CosmeticException(503, "DATAPACK_UNAVAILABLE"); }
        public Set<String> unlocked(UUID uuid) { return unlocked; }
        public Set<String> tags(UUID uuid) { return tags; }
        public boolean apply(UUID uuid, String function) {
            applies++;
            if (!hookSuccess) return false;
            if (changeTags) {
                String[] parts = function.split("/");
                String category = parts[2];
                tags.removeIf(t -> t.startsWith("sgp." + category + "."));
                if (parts[1].equals("equip")) tags.add("sgp." + category + "." + parts[3]);
            }
            return true;
        }
    }
    static class MutableClock extends Clock {
        long now = 1_800_000_000_000L;
        public ZoneId getZone() { return ZoneOffset.UTC; }
        public Clock withZone(ZoneId zone) { return this; }
        public Instant instant() { return Instant.ofEpochMilli(now); }
    }
}
