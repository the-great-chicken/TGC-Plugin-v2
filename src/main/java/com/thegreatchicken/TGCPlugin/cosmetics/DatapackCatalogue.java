package com.thegreatchicken.TGCPlugin.cosmetics;

import io.papermc.paper.event.server.ServerResourcesReloadedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.io.IOException;
import java.util.function.Supplier;
import java.util.logging.Logger;

/** Replaced on startup/reload, never read from disk by an HTTP request or a ticking function. */
final class DatapackCatalogue implements Supplier<CosmeticCatalogue>, Listener {
    @FunctionalInterface interface Source { CosmeticCatalogue load() throws IOException; }
    private final Source source;
    private final Logger logger;
    private volatile CosmeticCatalogue current;

    DatapackCatalogue(Source source, Logger logger) {
        this.source = source;
        this.logger = logger;
        reload();
    }
    @EventHandler public void onReload(ServerResourcesReloadedEvent event) { reload(); }
    void reload() {
        try {
            current = source.load();
            logger.info("Loaded " + current.entries().size() + " cosmetics from the datapack");
        } catch (IOException | IllegalArgumentException e) {
            // An invalid/disabled datapack must not keep advertising the preceding catalogue.
            current = null;
            logger.severe("Cosmetic catalogue unavailable: " + e.getMessage());
        }
    }
    @Override public CosmeticCatalogue get() {
        var snapshot = current;
        if (snapshot == null) throw new CosmeticException(503, "DATAPACK_UNAVAILABLE");
        return snapshot;
    }
}
