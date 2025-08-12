package weebify.dptb2utils.gui.screen;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import weebify.dptb2utils.DPTB2Utils;

import java.io.IOException;

public class GuiDPTBotConfig extends GuiScreen {
    private final DPTB2Utils mod;
    public GuiScreen parent;
    private GuiTextField host;
    private GuiTextField port;
    private boolean showIPOptions = false;

    public GuiDPTBotConfig(GuiScreen parent, DPTB2Utils mod) {
        this.parent = parent;
        this.mod = mod;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(1, width / 2 - 80 - 75, 75, 150, 20, String.format("DPTBot Connection: %s", this.mod.getDiscordRamper() ? "ON" : "OFF")));
        this.buttonList.add(new GuiButton(2, width / 2 + 80 - 75, 75, 150, 20, String.format("Advanced Options: %s", this.showIPOptions ? "ON" : "OFF")));
        this.buttonList.add(new GuiButton(3, width / 2 - 80 - 75, 100, 150, 20, String.format("Broadcast Notifs: %s", this.mod.getBroadcastToast() ? "ON" : "OFF")));
        this.buttonList.add(new GuiButton(4, width / 2 + 80 - 75, 100, 150, 20, String.format("Broadcast Chat: %s", this.mod.getBroadcastChat() ? "ON" : "OFF")));
        
        this.host = new GuiTextField(-1, fontRendererObj, width / 2 - 80 - 75, 125, 150, 20);
        this.host.setText(this.mod.getDPTBotHost());
        this.host.setVisible(false);
        
        this.port = new GuiTextField(-2, fontRendererObj, width / 2 + 80 - 75, 125, 150, 20);
        this.port.setText(Integer.toString(this.mod.getDPTBotPort()));
        this.port.setVisible(false);
        
        this.buttonList.add(new GuiButton(999, width / 2 - 75, height - 30 - 10, 150, 20, I18n.format("gui.done")));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        this.saveIPSettings();
        this.mod.saveSettings();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch(button.id) {
            case 1:
                button.displayString = String.format("DPTBot Connection: %s", !this.mod.setDiscordRamper(!this.mod.getDiscordRamper()) ? "ON" : "OFF");
                this.mod.refreshRamperStatus();
                break;
            case 2:
                this.showIPOptions = !this.showIPOptions;
                button.displayString = String.format("Advanced Options: %s", this.showIPOptions ? "ON" : "OFF");
                this.host.setVisible(this.showIPOptions);
                this.port.setVisible(this.showIPOptions);
                break;
            case 3:
                button.displayString = String.format("Broadcast Notifs: %s", !this.mod.setBroadcastToast(!this.mod.getBroadcastToast()) ? "ON" : "OFF");
                break;
            case 4:
                button.displayString = String.format("Broadcast Chat: %s", !this.mod.setBroadcastChat(!this.mod.getBroadcastChat()) ? "ON" : "OFF");
                break;
            case 999:
                this.saveIPSettings();
                this.mc.displayGuiScreen(this.parent);
                break;
        }

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.host.drawTextBox();
        this.port.drawTextBox();
        this.drawCenteredString(fontRendererObj, "DPTBot Settings", width / 2, 20, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        this.saveIPSettings();
        this.host.textboxKeyTyped(typedChar, keyCode);
        this.port.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.host.mouseClicked(mouseX, mouseY, mouseButton);
        this.port.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.host.updateCursorCounter();
        this.port.updateCursorCounter();
    }

    private void saveIPSettings() {
        this.mod.setDPTBotHost(this.host.getText());

        try {
            this.mod.setDPTBotPort(Integer.parseInt(this.port.getText()));
        } catch (NumberFormatException var2) {
            // Handle invalid port input
        }

    }
}