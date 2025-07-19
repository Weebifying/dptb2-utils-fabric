package weebify.dptb2utils.gui.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.gui.widget.DraggableTextWidget;
import weebify.dptb2utils.utils.ButtonTimerManager;


public class ButtonTimerConfigScreen extends Screen {
    private final DPTB2Utils mod;
    public Screen parent;
    public DraggableTextWidget textWidget;

    public ButtonTimerConfigScreen(Screen parent, DPTB2Utils mod) {
        super(Text.literal("Button Timer HUD Config"));
        this.parent = parent;
        this.mod = mod;
    }

    @Override
    protected void init() {
        MinecraftClient mc = MinecraftClient.getInstance();

        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Enabled: %s", mod.getButtonTimerEnabled() ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Text.of(String.format("Enabled: %s", !mod.setButtonTimerEnabled(!mod.getButtonTimerEnabled()) ? "ON" : "OFF")));
        }).dimensions(this.width/2 - 80 - 75, 75, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Text Shadow: %s", mod.getButtonTimerTextShadow() ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Text.of(String.format("Text Shadow: %s", !mod.setButtonTimerTextShadow(!mod.getButtonTimerTextShadow()) ? "ON" : "OFF")));
        }).dimensions(this.width/2 + 80 - 75, 75, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Render Background: %s", mod.getButtonTimerRenderBG() ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Text.of(String.format("Render Background: %s", !mod.setButtonTimerRenderBG(!mod.getButtonTimerRenderBG()) ? "ON" : "OFF")));
        }).dimensions(this.width/2 - 80 - 75, 100, 150, 20).build());

        this.textWidget = new DraggableTextWidget(
                mod.getButtonTimerConfigs("posX", Float.class),
                mod.getButtonTimerConfigs("posY", Float.class),
                ButtonTimerManager.tickToTime((!mod.isInDPTB2 || ButtonTimerManager.buttonTimer < 0) ? MathHelper.nextInt(Random.create(), 0, 400) : ButtonTimerManager.buttonTimer)
        );
        this.textWidget.updatePosition(this.width, this.height);
        this.addDrawableChild(this.textWidget);


        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), (btn) -> {
            assert this.client != null;
            this.mod.setButtonTimerConfigs("posX", this.textWidget.relX, Float.class);
            this.mod.setButtonTimerConfigs("posY", this.textWidget.relY, Float.class);
            this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 75, this.height - 30 - 10, 150, 20).build());
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        this.mod.setButtonTimerConfigs("posX", this.textWidget.relX, Float.class);
        this.mod.setButtonTimerConfigs("posY", this.textWidget.relY, Float.class);
        this.mod.saveSettings();
        super.close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width/2, 20, Colors.WHITE);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("(You can drag the timer HUD to move its position on this screen.)"), this.width / 2, 30, Colors.WHITE);
    }
}
