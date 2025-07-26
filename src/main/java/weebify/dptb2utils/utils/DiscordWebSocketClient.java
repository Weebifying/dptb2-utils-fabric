package weebify.dptb2utils.utils;

import com.google.gson.Gson;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.gui.widget.NotificationToast;

import java.util.Map;
import java.net.URI;

public class DiscordWebSocketClient extends WebSocketClient {
    private static final Gson GSON = new Gson();
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static final DPTB2Utils MOD = DPTB2Utils.getInstance();

    public DiscordWebSocketClient(String uri) {
        super(URI.create(uri));
    }

    // run when the connection is established
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        MC.getToastManager().add(new NotificationToast("DPTBot", "Connected!", 0xFFFFFF, SoundEvents.ENTITY_BAT_TAKEOFF));
    }

    // run when a message is received from the server
    @Override
    public void onMessage(String message) {
        Map<?, ?> data = GSON.fromJson(message, Map.class);
        String type = (String) data.get("type");
        String text = (String) data.get("text");
        MinecraftClient.getInstance().execute(() -> {
                if (type.equalsIgnoreCase("delegate")) {
                    if (MOD.getDiscordRamper() && MC.player != null) {
                        MinecraftClient.getInstance().getToastManager().add(new NotificationToast("DPTBot", "You are now the chat ramper!", 0xFFFFFF, SoundEvents.ENTITY_BAT_TAKEOFF));
                        MOD.isRamper = true;
                        this.sendModMessage("confirm", MC.player.getGameProfile().getName());
                    }
                } else if (type.equalsIgnoreCase("broadcast")) {
                    String name = data.get("name") != null ? (String) data.get("name") : "Anonymous";
                    MinecraftClient.getInstance().getToastManager().add(new NotificationToast(String.format("From %s", name), text, 0xFFFFFF, SoundEvents.ENTITY_BAT_TAKEOFF));
                }
        });
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        MC.getToastManager().add(new NotificationToast("DPTBot", "Disconnected!", 0xFFFFFF, SoundEvents.ENTITY_BAT_TAKEOFF));
        DPTB2Utils.LOGGER.error("WebSocket connection closed: {} (code: {}, remote: {})", reason, code, remote);
    }

    @Override
    public void onError(Exception ex) {
        MC.getToastManager().add(new NotificationToast("DPTBot", "Disconnected!", 0xFFFFFF, SoundEvents.ENTITY_BAT_TAKEOFF));
        ex.printStackTrace();
    }

    public void sendModMessage(String type, String message) {
        if (this.isOpen() && MC.player != null) {
            this.send(GSON.toJson(Map.of("type", type, "text", message)));
        }
    }
}
