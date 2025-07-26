package weebify.dptb2utils.gui.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.EditBox;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import weebify.dptb2utils.DPTB2Utils;

public class ModMenuScreen extends Screen {
    private final DPTB2Utils mod;
    private EditBoxWidget host;
    private EditBoxWidget port;

    public ModMenuScreen(DPTB2Utils mod) {
        super(Text.literal("DPTB2 Utils"));
        this.mod = mod;
    }

    @Override
    protected void init() {
        MinecraftClient mc = MinecraftClient.getInstance();
        this.addDrawableChild(ButtonWidget.builder(Text.of("Session's Boots List"), (btn) -> {
            assert this.client != null;
            this.client.setScreen(new BootsListScreen(this, mod));
        }).dimensions(this.width/2 - 80 - 75, 75, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("AutoCheer: %s", mod.getAutoCheer() ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Text.of(String.format("AutoCheer: %s", !mod.setAutoCheer(!mod.getAutoCheer()) ? "ON" : "OFF")));
        }).dimensions(this.width/2 + 80 - 75, 75, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("Notifications Config"), (btn) -> {
            mc.setScreen(new NotificationsScreen(this, mod));
        }).dimensions(this.width/2 - 80 - 75, 100, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("Button Timer HUD"), (btn) -> {
            mc.setScreen(new ButtonTimerConfigScreen(this, mod));
        }).dimensions(this.width/2 + 80 - 75, 100, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Allow Chat Ramp: %s", mod.getDiscordRamper() ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Text.of(String.format("Allow Chat Ramp: %s", !mod.setDiscordRamper(!mod.getDiscordRamper()) ? "ON" : "OFF")));
            mod.refreshRamperStatus();
        }).dimensions(this.width/2 - 80 - 75, 125, 150, 20).build());

        this.host = new EditBoxWidget(this.textRenderer, this.width / 2 - 80 - 75, 150, 150, 20, Text.of("Websocket Host"), Text.of(DPTB2Utils.HOST));
        this.host.setText(DPTB2Utils.HOST);
        this.addDrawableChild(this.host);

        this.port = new EditBoxWidget(this.textRenderer, this.width / 2 + 80 - 75, 150, 150, 20, Text.of("Websocket Port"), Text.of(Integer.toString(DPTB2Utils.PORT)));
        this.port.setText(Integer.toString(DPTB2Utils.PORT));
        this.addDrawableChild(this.port);

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), (btn) -> {
            this.close();
        }).dimensions(this.width / 2 - 75, this.height - 30 - 10, 150, 20).build());
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        DPTB2Utils.HOST = this.host.getText();
        DPTB2Utils.PORT = Integer.parseInt(this.port.getText());
        this.mod.saveSettings();
        super.close();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        DPTB2Utils.HOST = this.host.getText();
        try {
            DPTB2Utils.PORT = Integer.parseInt(this.port.getText());
        } catch (NumberFormatException e) {
            // :)
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width/2, 20, Colors.WHITE);
    }
}
