package weebify.dptb2utils.gui.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import weebify.dptb2utils.DPTB2Utils;

public class DPTBotConfigScreen extends Screen {
    private final DPTB2Utils mod;
    public Screen parent;
    private EditBoxWidget host;
    private EditBoxWidget port;
    private boolean showIPOptions = false;

    public DPTBotConfigScreen(Screen parent, DPTB2Utils mod) {
        super(Text.literal("DPTBot Settings"));
        this.parent = parent;
        this.mod = mod;
    }

    @Override
    protected void init() {
        MinecraftClient mc = MinecraftClient.getInstance();

        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Allow DPTBot Connection: %s", mod.getDiscordRamper() ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Text.of(String.format("Allow DPTBot Connection: %s", !mod.setDiscordRamper(!mod.getDiscordRamper()) ? "ON" : "OFF")));
            mod.refreshRamperStatus();
        }).dimensions(this.width/2 - 80 - 75, 75, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Broadcast Notifications: %s", mod.getBroadcastToast() ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Text.of(String.format("Broadcast Notifications: %s", !mod.setBroadcastToast(!mod.getBroadcastToast()) ? "ON" : "OFF")));
        }).dimensions(this.width/2 - 80 - 75, 100, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Broadcast Chat: %s", mod.getBroadcastChat() ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Text.of(String.format("Broadcast Chat: %s", !mod.setBroadcastChat(!mod.getBroadcastChat()) ? "ON" : "OFF")));
        }).dimensions(this.width/2 + 80 - 75, 100, 150, 20).build());

        this.host = new EditBoxWidget(this.textRenderer, this.width / 2 - 80 - 75, 125, 150, 20, Text.of("Websocket Host"), Text.empty());
        this.host.setText(mod.getDPTBotHost());
        this.host.visible = false;
        this.addDrawableChild(this.host);

        this.port = new EditBoxWidget(this.textRenderer, this.width / 2 + 80 - 75, 125, 150, 20, Text.of("Websocket Port"), Text.empty());
        this.port.setText(Integer.toString(mod.getDPTBotPort()));
        this.port.visible = false;
        this.addDrawableChild(this.port);

        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Advanced DPTBot options: %s",this.showIPOptions ? "ON" : "OFF")), (btn) -> {
            this.showIPOptions = !this.showIPOptions;
            btn.setMessage(Text.of(String.format("Advanced DPTBot options: %s", this.showIPOptions ? "ON" : "OFF")));
            this.host.visible = this.showIPOptions;
            this.port.visible = this.showIPOptions;
        }).dimensions(this.width/2 + 80 - 75, 75, 150, 20).build());


        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), (btn) -> {
            this.saveIPSettings();
            this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 75, this.height - 30 - 10, 150, 20).build());
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        this.saveIPSettings();
        this.mod.saveSettings();
        super.close();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        this.saveIPSettings();
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width/2, 20, Colors.WHITE);
    }

    private void saveIPSettings() {
        mod.setDPTBotHost(this.host.getText());
        try {
            mod.setDPTBotPort(Integer.parseInt(this.port.getText()));
        } catch (NumberFormatException e) {
            // Handle invalid port input
        }
    }
}
