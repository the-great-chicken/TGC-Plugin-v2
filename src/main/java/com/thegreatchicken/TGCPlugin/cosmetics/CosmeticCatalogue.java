package com.thegreatchicken.TGCPlugin.cosmetics;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

public final class CosmeticCatalogue {
    public static final List<String> CATEGORIES = List.of("particle", "intensity", "kill");
    public record Cosmetic(String id, String category, String name, int sortOrder) {
        public String tag() { return "sgp." + id; }
        public String objective() { return tag() + "_unlocked"; }
        public String equipFunction() { return "sgp.cosmetics:api/equip/" + id.replace('.', '/'); }
    }
    private final List<Cosmetic> entries;
    private final Map<String, Cosmetic> byId = new HashMap<>();
    private static final Pattern OBJECTIVE = Pattern.compile(
            "scoreboard\\s+objectives\\s+add\\s+sgp\\.((particle|intensity|kill)\\.[a-z_]+)_unlocked\\s+dummy\\s+(\"(?:[^\"\\\\]|\\\\.)*\")");

    public CosmeticCatalogue(List<Cosmetic> entries) {
        if (entries.isEmpty() || entries.size() > 128) throw new IllegalArgumentException("Expected 1–128 cosmetics");
        this.entries = List.copyOf(entries);
        for (Cosmetic entry : entries) {
            if (!CATEGORIES.contains(entry.category()) || !entry.id().matches(entry.category() + "\\.[a-z_]+")
                    || entry.name() == null || entry.name().isBlank() || entry.name().length() > 100
                    || entry.sortOrder() < 0 || byId.put(entry.id(), entry) != null) {
                throw new IllegalArgumentException("Invalid cosmetic catalogue");
            }
        }
    }
    /** Objective declarations own IDs, names and category order; equip functions own availability. */
    public static CosmeticCatalogue load(Path datapack) throws IOException {
        Path root = datapack.resolve("data/sgp.cosmetics/function");
        List<Cosmetic> entries = new ArrayList<>();
        Map<String, Integer> order = new HashMap<>();
        for (String line : Files.readAllLines(root.resolve("initialization.mcfunction"))) {
            String command = line.strip();
            if (!command.matches("scoreboard\\s+objectives\\s+add\\s+sgp\\.(particle|intensity|kill)\\..*")) continue;
            var match = OBJECTIVE.matcher(command);
            if (!match.matches()) throw new IOException("Expected a cosmetic unlock objective with a quoted display name: " + command);
            String category = match.group(2);
            try {
                entries.add(new Cosmetic(match.group(1), category, JsonParser.parseString(match.group(3)).getAsString(),
                        order.merge(category, 1, Integer::sum) - 1));
            } catch (com.google.gson.JsonParseException e) {
                throw new IOException("Invalid cosmetic display name: " + match.group(1), e);
            }
        }
        var catalogue = new CosmeticCatalogue(entries);
        Set<String> expected = new HashSet<>();
        for (var cosmetic : entries) expected.add(cosmetic.equipFunction().substring("sgp.cosmetics:api/equip/".length()) + ".mcfunction");
        Path equip = root.resolve("api/equip");
        Set<String> actual = new HashSet<>();
        try (var paths = Files.walk(equip)) {
            paths.filter(Files::isRegularFile).filter(path -> path.toString().endsWith(".mcfunction"))
                    .forEach(path -> actual.add(equip.relativize(path).toString().replace('\\', '/')));
        }
        if (!actual.equals(expected)) throw new IOException("Cosmetic objective declarations and equip hooks differ");
        for (String category : CATEGORIES) {
            if (!Files.isRegularFile(root.resolve("api/unequip/" + category + ".mcfunction"))) {
                throw new IOException("Missing cosmetic unequip hook: " + category);
            }
        }
        if (!Files.isRegularFile(root.resolve("api/ready.mcfunction"))) throw new IOException("Missing cosmetic ready hook");
        return catalogue;
    }
    public List<Cosmetic> entries() { return entries; }
    public Cosmetic get(String id) { return byId.get(id); }
}
