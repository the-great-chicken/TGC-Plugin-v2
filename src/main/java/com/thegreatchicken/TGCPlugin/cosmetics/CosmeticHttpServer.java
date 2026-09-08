package com.thegreatchicken.TGCPlugin.cosmetics;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.HandlerList;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

/** Private server-to-server endpoint. No browser cookies or arbitrary commands are accepted. */
public final class CosmeticHttpServer implements AutoCloseable {
    private static final Gson JSON = new GsonBuilder().serializeNulls().create();
    private final HttpServer http;
    private final ExecutorService workers;
    private final byte[] secretHash;
    private final Function<Callable<CosmeticService.State>, Future<CosmeticService.State>> dispatch;
    private final Logger logger;
    private final CosmeticService service;
    private final Supplier<CosmeticCatalogue> catalogues;
    private DatapackCatalogue reloadListener;

    public static CosmeticHttpServer start(JavaPlugin plugin) throws IOException {
        String packName = plugin.getConfig().getString("cosmetics.datapack", "TGCdatapack");
        if (!packName.matches("[A-Za-z0-9_-][A-Za-z0-9_.-]*")) throw new IllegalArgumentException("cosmetics.datapack must be a datapack directory name");
        var server = plugin.getServer();
        var primaryWorld = server.getWorlds().getFirst();
        var path = datapackPath(server.getWorldContainer().toPath(), primaryWorld.getName(), packName);
        var catalogues = new DatapackCatalogue(() -> {
            if (server.getDatapackManager().getEnabledPacks().stream().noneMatch(pack -> pack.getName().equals("file/" + packName))) {
                throw new IOException("Datapack is not enabled: " + packName);
            }
            return CosmeticCatalogue.load(path);
        }, plugin.getLogger());
        var service = new CosmeticService(catalogues, new DatapackCosmetics(server, catalogues), Clock.systemUTC());
        var bridge = new CosmeticHttpServer(new InetSocketAddress(
                plugin.getConfig().getString("cosmetics.bind", "127.0.0.1"),
                plugin.getConfig().getInt("cosmetics.port", 8766)),
                plugin.getConfig().getString("cosmetics.secret", ""), catalogues, service,
                operation -> plugin.getServer().getScheduler().callSyncMethod(plugin, operation), plugin.getLogger());
        bridge.reloadListener = catalogues;
        server.getPluginManager().registerEvents(catalogues, plugin);
        return bridge;
    }


    /**
     * Datapacks belong to the level root, not to a dimension directory.
     * On modern Paper, World#getWorldFolder() may point at e.g.
     * world/dimensions/minecraft/overworld, so resolving "datapacks" from it is wrong.
     */
    static Path datapackPath(Path worldContainer, String primaryWorldName, String packName) {
        return worldContainer.resolve(primaryWorldName).resolve("datapacks").resolve(packName).normalize();
    }

    CosmeticHttpServer(InetSocketAddress address, String secret, Supplier<CosmeticCatalogue> catalogues, CosmeticService service,
                       Function<Callable<CosmeticService.State>, Future<CosmeticService.State>> dispatch, Logger logger) throws IOException {
        this.dispatch = dispatch;
        this.logger = logger;
        this.catalogues = catalogues;
        this.service = service;
        if (secret.length() < 32 || !secret.matches("[A-Za-z0-9_-]+")) {
            throw new IllegalArgumentException("cosmetics.secret must contain at least 32 random base64url characters");
        }
        secretHash = digest("Bearer " + secret);
        http = HttpServer.create(address, 16);
        workers = new ThreadPoolExecutor(2, 4, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(16),
                Thread.ofPlatform().daemon().name("sgp-cosmetics-http-", 0).factory(),
                new ThreadPoolExecutor.AbortPolicy());
        http.setExecutor(workers);
        http.createContext("/v1/", this::handle);
        http.start();
    }
    private void handle(HttpExchange exchange) throws IOException {
        try {
            String authorization = exchange.getRequestHeaders().getFirst("Authorization");
            if (authorization == null || !MessageDigest.isEqual(secretHash, digest(authorization))) {
                throw new CosmeticException(401, "UNAUTHORIZED");
            }
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            if (path.equals("/v1/catalogue") && method.equals("GET")) {
                respond(exchange, 200, Map.of("protocolVersion", 2, "catalogue", catalogues.get().entries()));
                return;
            }
            boolean mutate = path.equals("/v1/equipment") && method.equals("PUT");
            if (!mutate && !(path.equals("/v1/state") && method.equals("POST"))) {
                throw new CosmeticException(404, "NOT_FOUND");
            }
            JsonObject body = readBody(exchange);
            Set<String> fields = mutate
                    ? Set.of("discordId", "playerUuid", "category", "cosmeticId", "requestId", "issuedAt")
                    : Set.of("discordId", "playerUuid");
            if (!body.keySet().equals(fields)) throw new CosmeticException(400, "INVALID_REQUEST");
            var identity = new CosmeticService.Identity(string(body, "discordId"), uuid(body, "playerUuid"));
            Callable<CosmeticService.State> operation;
            if (mutate) {
                String cosmeticId = body.get("cosmeticId").isJsonNull() ? null : string(body, "cosmeticId");
                var issuedAt = body.get("issuedAt");
                if (!issuedAt.isJsonPrimitive() || !issuedAt.getAsJsonPrimitive().isNumber()
                        || !issuedAt.toString().matches("[0-9]{1,15}")) throw new CosmeticException(400, "INVALID_REQUEST");
                var change = new CosmeticService.Change(identity, string(body, "category"), cosmeticId,
                        uuid(body, "requestId"), issuedAt.getAsLong());
                operation = () -> service.change(change);
            } else {
                operation = () -> service.state(identity);
            }
            var task = dispatch.apply(operation);
            try {
                respond(exchange, 200, task.get(3, TimeUnit.SECONDS));
            } finally {
                // Cancel work still queued after timeout/disconnect; an already running call may have applied.
                task.cancel(false);
            }
        } catch (ExecutionException e) {
            if (e.getCause() instanceof CosmeticException failure) respond(exchange, failure.status(), Map.of("error", failure.code()));
            else {
                logger.warning("Cosmetic operation failed: " + e.getCause().getClass().getSimpleName());
                respond(exchange, 503, Map.of("error", "UNCONFIRMED"));
            }
        } catch (CosmeticException e) {
            respond(exchange, e.status(), Map.of("error", e.code()));
        } catch (TimeoutException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            respond(exchange, 503, Map.of("error", "UNCONFIRMED"));
        } catch (RejectedExecutionException | CancellationException e) {
            respond(exchange, 503, Map.of("error", "UNCONFIRMED"));
        } catch (JsonParseException | IllegalArgumentException | IllegalStateException e) {
            respond(exchange, 400, Map.of("error", "INVALID_REQUEST"));
        } finally {
            exchange.close();
        }
    }
    private static JsonObject readBody(HttpExchange exchange) throws IOException {
        String type = exchange.getRequestHeaders().getFirst("Content-Type");
        if (type == null || !type.split(";")[0].trim().equalsIgnoreCase("application/json")) {
            throw new CosmeticException(415, "INVALID_CONTENT_TYPE");
        }
        byte[] bytes = exchange.getRequestBody().readNBytes(4097);
        if (bytes.length > 4096) throw new CosmeticException(413, "REQUEST_TOO_LARGE");
        var parsed = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8));
        if (!parsed.isJsonObject()) throw new CosmeticException(400, "INVALID_REQUEST");
        return parsed.getAsJsonObject();
    }
    private static String string(JsonObject body, String key) {
        var value = body.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new CosmeticException(400, "INVALID_REQUEST");
        }
        return value.getAsString();
    }
    private static UUID uuid(JsonObject body, String key) {
        String value = string(body, key);
        if (!value.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
            throw new CosmeticException(400, "INVALID_REQUEST");
        }
        return UUID.fromString(value);
    }
    private static byte[] digest(String text) {
        try { return MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)); }
        catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    private static void respond(HttpExchange exchange, int status, Object value) throws IOException {
        byte[] bytes = JSON.toJson(value).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
    }
    @Override public void close() {
        if (reloadListener != null) HandlerList.unregisterAll(reloadListener);
        http.stop(0);
        workers.shutdownNow();
    }
    int port() { return http.getAddress().getPort(); }
}
