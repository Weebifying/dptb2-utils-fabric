package weebify.dptb2utils.utils;

import com.google.gson.Gson;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Colors;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.gui.widget.NotificationToast;

import java.util.List;
import java.util.Map;
import java.net.URI;

public class DiscordWebSocketClient extends WebSocketClient {
    private static final Gson GSON = new Gson();
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static final DPTB2Utils mod = DPTB2Utils.getInstance();

    public DiscordWebSocketClient(String uri) {
        super(URI.create(uri));
    }

    // run when the connection is established
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        mod.tryingToConnect = false;
        if (MC.player != null) {
            this.send(GSON.toJson(Map.of("type", "greet", "name", MC.player.getGameProfile().getName())));
        }
        MC.execute(() -> MC.getToastManager().add(new NotificationToast("DPTBot", "Connected!", Colors.WHITE, SoundEvents.ENTITY_BAT_TAKEOFF)));
    }

    // run when a message is received from the server
    @Override
    public void onMessage(String message) {
        Map<?, ?> data = GSON.fromJson(message, Map.class);
        String type = (String) data.get("type");
        String text = (String) data.get("text");
        MinecraftClient.getInstance().execute(() -> {
                if (type.equalsIgnoreCase("delegate")) {
                    if (mod.getDiscordRamper() && MC.player != null) {
                        MinecraftClient.getInstance().getToastManager().add(new NotificationToast("DPTBot", "You are now the chat ramper!", Colors.WHITE, SoundEvents.ENTITY_BAT_TAKEOFF));
                        mod.isRamper = true;
                        this.sendModMessage("confirm", MC.player.getGameProfile().getName());
                    }
                } else if (type.equalsIgnoreCase("broadcast")) {
                    String name = data.get("name") != null ? (String) data.get("name") : "Anonymous";
                    MinecraftClient.getInstance().getToastManager().add(new NotificationToast(String.format("From %s", name), text, Colors.WHITE, SoundEvents.ENTITY_BAT_TAKEOFF));
                } else if (type.equalsIgnoreCase("askTabList")) {
                    String id = (String) data.get("id");
                    if (MC.getNetworkHandler() != null) {
                        List<String> players = MC.getNetworkHandler().getPlayerList().stream()
                                .map(player -> player.getProfile().getName())
                                .toList();
                        this.send(GSON.toJson(Map.of("type", "tabList", "id", id, "players", players)));
                    }
                }
        });
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (!mod.tryingToConnect) {
            MC.getToastManager().add(new NotificationToast("DPTBot", String.format("Disconnected: %s (code:%s)", reason, code), Colors.WHITE, SoundEvents.ENTITY_BAT_TAKEOFF));
        }
        DPTB2Utils.LOGGER.error("WebSocket connection closed: {} (code:{}, remote:{})", reason, code, remote);
        this.retryConnection();
    }

    @Override
    public void onError(Exception ex) {
        if (!mod.tryingToConnect) {
            MC.execute(() -> MC.getToastManager().add(new NotificationToast("DPTBot", "Connecting to DPTBot failed!", Colors.WHITE, SoundEvents.ENTITY_BAT_TAKEOFF)));
        }
        ex.printStackTrace();
        this.retryConnection();
    }

    public void sendModMessage(String type, String message) {
        if (this.isOpen() && MC.player != null) {
            this.send(GSON.toJson(Map.of("type", type, "text", message)));
        }
    }

    public void retryConnection() {
        mod.tryingToConnect = true;
        mod.scheduleTask( 1200, () -> {
            if ((mod.websocketClient == null || mod.websocketClient.isClosed()) & mod.getDiscordRamper() && mod.tryingToConnect) {
                String host = mod.getDPTBotHost();
                int port = mod.getDPTBotPort();

                DPTB2Utils.LOGGER.info("Attempting Websocket connection to ws://{}:{}", host, port);
                mod.websocketClient = new DiscordWebSocketClient(String.format("ws://%s:%d", host, port));
                mod.websocketClient.connect();
            }
        });
    }
}
