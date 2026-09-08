package com.thegreatchicken.TGCPlugin.cosmetics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class CosmeticCatalogueTest {
    @TempDir Path pack;
    static final String CLOUD = "scoreboard objectives add sgp.particle.cloud_unlocked dummy {text:\"Nuage\",color:\"#ffffff\"}";

    void write(String relative, String source) throws IOException {
        Path path = pack.resolve("data/sgp.cosmetics/function").resolve(relative);
        Files.createDirectories(path.getParent());
        Files.writeString(path, source);
    }
    void setup(String declarations, String... ids) throws IOException {
        write("initialization.mcfunction", "# Cosmetic metadata is actual Minecraft code.\n" + declarations);
        write("api/ready.mcfunction", "return 1\n");
        for (String category : CosmeticCatalogue.CATEGORIES) write("api/unequip/" + category + ".mcfunction", "return 1\n");
        for (String id : ids) write("api/equip/" + id.replace('.', '/') + ".mcfunction", "return 1\n");
    }
    @Test void readsDatapackNamesAndPerCategoryDeclarationOrder() throws IOException {
        setup(CLOUD + "\nscoreboard objectives add sgp.kill.anvil_unlocked dummy {text:\"Enclume\",color:\"#aaaaaa\"}\n"
                + "scoreboard objectives add sgp.particle.smoke_unlocked dummy {text:\"Fumée \\\"douce\\\"\",color:\"#555555\"}\n"
                + "scoreboard objectives add sgp.death_effect deathCount\n", "particle.cloud", "kill.anvil", "particle.smoke");
        var catalogue = CosmeticCatalogue.load(pack);
        assertEquals(3, catalogue.entries().size());
        assertEquals("Fumée \"douce\"", catalogue.get("particle.smoke").name());
        assertEquals("#555555", catalogue.get("particle.smoke").color());
        assertEquals(1, catalogue.get("particle.smoke").sortOrder());
        assertEquals(0, catalogue.get("kill.anvil").sortOrder());
        assertEquals("sgp.particle.smoke_unlocked", catalogue.get("particle.smoke").objective());
    }
    @Test void rejectsMissingOrExtraHooksAndMissingDisplayNames() throws IOException {
        setup(CLOUD, "particle.smoke");
        assertThrows(IOException.class, () -> CosmeticCatalogue.load(pack));
        write("api/equip/particle/cloud.mcfunction", "return 1\n");
        assertThrows(IOException.class, () -> CosmeticCatalogue.load(pack));
        write("initialization.mcfunction", "scoreboard objectives add sgp.particle.cloud_unlocked dummy\n");
        assertThrows(IOException.class, () -> CosmeticCatalogue.load(pack));
    }
    @Test void rejectsDuplicatesAndInvalidNames() throws IOException {
        setup(CLOUD + "\n" + CLOUD, "particle.cloud");
        assertThrows(IllegalArgumentException.class, () -> CosmeticCatalogue.load(pack));
        for (String name : List.of("", " ", "x".repeat(101))) {
            write("initialization.mcfunction", CLOUD.replace("Nuage", name));
            assertThrows(IllegalArgumentException.class, () -> CosmeticCatalogue.load(pack));
        }
        write("initialization.mcfunction", CLOUD.replace("cloud_unlocked", "cloud;kill_unlocked"));
        assertThrows(IOException.class, () -> CosmeticCatalogue.load(pack));
    }
    @Test void rejectsMissingInvalidAndNonliteralColors() throws IOException {
        setup(CLOUD, "particle.cloud");
        for (String component : List.of("{text:\"Nuage\"}", "{text:\"Nuage\",color:42}", "{text:\"Nuage\",color:\"red\"}",
                "{text:\"Nuage\",color:\"url(bad)\"}", "{text:\"Nuage\",color:\"#ffffff\",extra:[]}")) {
            write("initialization.mcfunction", CLOUD.substring(0, CLOUD.indexOf('{')) + component);
            assertThrows(Exception.class, () -> CosmeticCatalogue.load(pack));
        }
    }
    @Test void reloadPublishesNewMetadataAndRecoversFromInvalidOrDisabledPack() throws IOException {
        setup(CLOUD, "particle.cloud");
        var enabled = new AtomicBoolean(true);
        var catalogues = new DatapackCatalogue(() -> {
            if (!enabled.get()) throw new IOException("Datapack disabled");
            return CosmeticCatalogue.load(pack);
        }, Logger.getAnonymousLogger());
        assertEquals("Nuage", catalogues.get().get("particle.cloud").name());
        write("initialization.mcfunction", CLOUD.replace("Nuage", "Nouveau nuage"));
        assertEquals("Nuage", catalogues.get().get("particle.cloud").name(), "Disk edits take effect only on reload");
        catalogues.reload();
        assertEquals("Nouveau nuage", catalogues.get().get("particle.cloud").name());
        write("initialization.mcfunction", CLOUD.replace("Nuage", ""));
        catalogues.reload();
        assertEquals("DATAPACK_UNAVAILABLE", assertThrows(CosmeticException.class, catalogues::get).code());
        write("initialization.mcfunction", CLOUD.replace("Nuage", "\\x"));
        catalogues.reload();
        assertThrows(CosmeticException.class, catalogues::get, "Malformed JSON must also discard the previous catalogue");
        write("initialization.mcfunction", CLOUD);
        catalogues.reload();
        assertNotNull(catalogues.get().get("particle.cloud"));
        enabled.set(false);
        catalogues.reload();
        assertThrows(CosmeticException.class, catalogues::get);
        enabled.set(true);
        catalogues.reload();
        assertNotNull(catalogues.get());
    }
    @Test void readsInstalledDatapackWhenExplicitlyProvided() throws IOException {
        String path = System.getProperty("cosmetics.datapack");
        assumeTrue(path != null, "Optional read-only check of the installed datapack");
        var catalogue = CosmeticCatalogue.load(Path.of(path));
        assertTrue(catalogue.entries().stream().allMatch(c -> !c.name().isBlank()));
        System.out.println("Read " + catalogue.entries().size() + " cosmetics from " + path + "; no Minecraft server started.");
    }
}
