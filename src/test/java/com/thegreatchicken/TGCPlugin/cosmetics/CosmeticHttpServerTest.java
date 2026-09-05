package com.thegreatchicken.TGCPlugin.cosmetics;

import com.google.gson.*;
import org.junit.jupiter.api.Test;
import java.net.*;
import java.net.http.*;
import java.nio.file.Path;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
import static org.junit.jupiter.api.Assertions.*;

class CosmeticHttpServerTest {
    @Test void datapackPathUsesPrimaryWorldRootNotDimensionFolder() {
        Path root = Path.of(".");
        assertEquals(root.resolve("world/datapacks/TGCdatapack").normalize(),
                CosmeticHttpServer.datapackPath(root, "world", "TGCdatapack"));
    }

    final String secret = "a".repeat(43);
    final CosmeticServiceTest fixture = new CosmeticServiceTest();
    final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    final ExecutorService gameThread = Executors.newSingleThreadExecutor();
    CosmeticHttpServer server() throws Exception {
        return new CosmeticHttpServer(new InetSocketAddress("127.0.0.1", 0), secret,
                () -> fixture.catalogue, fixture.service, gameThread::submit, Logger.getAnonymousLogger());
    }
    String identity() {
        return "\"discordId\":\"" + fixture.identity.discordId() + "\",\"playerUuid\":\"" + fixture.player + "\"";
    }
    String change() {
        return "{" + identity() + ",\"category\":\"particle\",\"cosmeticId\":\"particle.cloud\",\"requestId\":\""
                + UUID.randomUUID() + "\",\"issuedAt\":" + fixture.clock.millis() + "}";
    }
    HttpResponse<String> send(CosmeticHttpServer server, String path, String method, String body, String token) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + server.port() + path))
                .timeout(Duration.ofSeconds(6)).header("Content-Type", "application/json");
        if (token != null) request.header("Authorization", "Bearer " + token);
        return client.send(request.method(method, HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }
    @Test void privateHttpAuthIdentityAndMutationContract() throws Exception {
        try (var server = server()) {
            assertEquals(401, send(server, "/v1/state", "POST", "{" + identity() + "}", null).statusCode());
            assertEquals(401, send(server, "/v1/catalogue", "GET", "", "incorrect").statusCode());
            assertEquals(200, send(server, "/v1/catalogue", "GET", "", secret).statusCode());
            String request = change();
            var response = send(server, "/v1/equipment", "PUT", request, secret);
            assertEquals(200, response.statusCode());
            var state = JsonParser.parseString(response.body()).getAsJsonObject();
            assertEquals("particle.cloud", state.getAsJsonObject("equipment").get("particle").getAsString());
            assertTrue(state.getAsJsonObject("equipment").get("intensity").isJsonNull());
            assertEquals("no-store", response.headers().firstValue("Cache-Control").orElseThrow());
            assertEquals(200, send(server, "/v1/equipment", "PUT", request, secret).statusCode());
            assertEquals(1, fixture.game.applies);
            assertEquals(403, send(server, "/v1/state", "POST",
                    "{" + identity().replace(fixture.player.toString(), UUID.randomUUID().toString()) + "}", secret).statusCode());
        } finally { gameThread.shutdownNow(); }
    }
    @Test void malformedAndUnsupportedHttpInputsCannotReachGame() throws Exception {
        try (var server = server()) {
            assertEquals(400, send(server, "/v1/equipment", "PUT", "{", secret).statusCode());
            assertEquals(400, send(server, "/v1/state", "POST", "{}", secret).statusCode());
            assertEquals(400, send(server, "/v1/state", "POST", "{" + identity() + ",\"command\":\"kill @a\"}", secret).statusCode());
            assertEquals(400, send(server, "/v1/state", "POST", "{" + identity().replace("\"discordId\":\"" + fixture.identity.discordId() + "\"", "\"discordId\":1") + "}", secret).statusCode());
            assertEquals(413, send(server, "/v1/state", "POST", "x".repeat(4097), secret).statusCode());
            assertEquals(404, send(server, "/v1/equipment", "GET", "", secret).statusCode());
            assertEquals(0, fixture.game.applies);
        } finally { gameThread.shutdownNow(); }
    }
    @Test void catalogueEndpointRefreshesAndInvalidReloadBlocksReadsAndWrites() throws Exception {
        var valid = new java.util.concurrent.atomic.AtomicBoolean(true);
        var catalogues = new DatapackCatalogue(() -> {
            if (!valid.get()) throw new java.io.IOException("Invalid datapack");
            return fixture.catalogue;
        }, Logger.getAnonymousLogger());
        var service = new CosmeticService(catalogues, fixture.game, fixture.clock);
        try (var server = new CosmeticHttpServer(new InetSocketAddress("127.0.0.1", 0), secret,
                catalogues, service, gameThread::submit, Logger.getAnonymousLogger())) {
            fixture.catalogue = new CosmeticCatalogue(List.of(new CosmeticCatalogue.Cosmetic("particle.spark", "particle", "Étincelle", 0)));
            catalogues.reload();
            var response = send(server, "/v1/catalogue", "GET", "", secret);
            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("Étincelle"));
            assertFalse(response.body().contains("particle.cloud"));
            valid.set(false);
            catalogues.reload();
            assertEquals(503, send(server, "/v1/catalogue", "GET", "", secret).statusCode());
            assertEquals(503, send(server, "/v1/state", "POST", "{" + identity() + "}", secret).statusCode());
            assertEquals(503, send(server, "/v1/equipment", "PUT", change(), secret).statusCode());
            assertEquals(0, fixture.game.applies);
        } finally { gameThread.shutdownNow(); }
    }
    @Test void stalledMainThreadCancelsQueuedWorkAndReportsUnconfirmed() throws Exception {
        var pending = new CompletableFuture<CosmeticService.State>();
        try (var server = new CosmeticHttpServer(new InetSocketAddress("127.0.0.1", 0), secret,
                () -> fixture.catalogue, fixture.service, operation -> pending, Logger.getAnonymousLogger())) {
            var response = send(server, "/v1/equipment", "PUT", change(), secret);
            assertEquals(503, response.statusCode());
            assertTrue(response.body().contains("UNCONFIRMED"));
            assertTrue(pending.isCancelled());
            assertEquals(0, fixture.game.applies);
        } finally { gameThread.shutdownNow(); }
    }
}
