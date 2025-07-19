package weebify.dptb2utils.gui.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import weebify.dptb2utils.DPTB2Utils;

public class NotificationsScreen extends Screen {
    private final DPTB2Utils mod;
    public Screen parent;

    protected NotificationsScreen(Screen parent, DPTB2Utils mod) {
        super(Text.of("Notifications Config"));
        this.parent = parent;
        this.mod = mod;
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Shop Update: %s", mod.getBoolNotifs("shopUpdate") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Text.of(String.format("Shop Update: %s", !mod.setBoolNotifs("shopUpdate", !mod.getBoolNotifs("shopUpdate")) ? "ON" : "OFF")));
        }).dimensions(this.width / 2 - 80 - 75, 75, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("City Door Switch: %s", mod.getBoolNotifs("doorSwitch") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Text.of(String.format("City Door Switch: %s", !mod.setBoolNotifs("doorSwitch", !mod.getBoolNotifs("doorSwitch")) ? "ON" : "OFF")));
        }).dimensions(this.width / 2 + 80 - 75, 75, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Button Mayhem: %s", mod.getBoolNotifs("buttonMayhem") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Text.of(String.format("Button Mayhem: %s", !mod.setBoolNotifs("buttonMayhem", !mod.getBoolNotifs("buttonMayhem")) ? "ON" : "OFF")));
        }).dimensions(this.width / 2 - 80 - 75, 100, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Button Disabled: %s", mod.getBoolNotifs("buttonDisable") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Text.of(String.format("Button Disabled: %s", !mod.setBoolNotifs("buttonDisable", !mod.getBoolNotifs("buttonDisable")) ? "ON" : "OFF")));
        }).dimensions(this.width / 2 + 80 - 75, 100, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Button Immunity: %s", mod.getBoolNotifs("buttonImmunity") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Text.of(String.format("Button Immunity: %s", !mod.setBoolNotifs("buttonImmunity", !mod.getBoolNotifs("buttonImmunity")) ? "ON" : "OFF")));
        }).dimensions(this.width / 2 - 80 - 75, 125, 150, 20).build());

        ButtonWidget slimeBtn = ButtonWidget.builder(Text.of(String.format("Slime Boots Notify: %s", mod.getBoolNotifs("slimeBoots") ? "ON" : "OFF")), (btn) -> {
            btn.setMessage(Text.of(String.format("Slime Boots Notify: %s", !mod.setBoolNotifs("slimeBoots", !mod.getBoolNotifs("slimeBoots")) ? "ON" : "OFF")));
        }).dimensions(this.width / 2 - 80 - 75, 150, 150, 20).build();
        slimeBtn.active = mod.getBoolNotifs("bootsCollected");
        this.addDrawableChild(slimeBtn);

        this.addDrawableChild(ButtonWidget.builder(Text.of(String.format("Boots Tracking: %s", mod.getBoolNotifs("bootsCollected") ? "ON" : "OFF")), (btn) -> {
            boolean a = !mod.setBoolNotifs("bootsCollected", !mod.getBoolNotifs("bootsCollected"));
            btn.setMessage(Text.of(String.format("Boots Tracking: %s", a ? "ON" : "OFF")));
            slimeBtn.active = a;
        }).dimensions(this.width / 2 + 80 - 75, 125, 150, 20).build());


        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), (btn) -> {
            assert this.client != null;
            this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 80 - 75, this.height - 30 - 10, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("Notification Settings"), (btn) -> {
            assert this.client != null;
            this.client.setScreen(new NotificationConfigScreen(this, mod));
        }).dimensions(this.width / 2 + 80 - 75, this.height - 30 - 10, 150, 20).build());
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        this.mod.saveSettings();
        super.close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width/2, 20, Colors.WHITE);
    }
}
