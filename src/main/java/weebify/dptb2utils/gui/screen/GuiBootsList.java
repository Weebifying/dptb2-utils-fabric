package weebify.dptb2utils.gui.screen;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Mouse;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.gui.widget.ScrollableBootsList;

import java.io.IOException;

public class GuiBootsList extends GuiScreen {
    private final DPTB2Utils mod;
    public GuiScreen parent;
    ScrollableBootsList listWidget;

    public GuiBootsList(GuiScreen parent, DPTB2Utils mod) {
        this.parent = parent;
        this.mod = mod;
    }

    @Override
    public void initGui() {
        this.listWidget = new ScrollableBootsList(40, 40, this.width - 80, height - 80 - 30, 3, 5, this.mod.bootsList, this.fontRendererObj);
        this.buttonList.add(new GuiButton(999, this.width / 2 - 75, height - 30 - 10, 150, 20, I18n.format("gui.done")));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        this.mod.saveSettings();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 999) {
            this.mc.displayGuiScreen(this.parent);
        }

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        Gui.drawRect(this.listWidget.getX(), this.listWidget.getY(), this.listWidget.getX() + this.listWidget.getWidth(), this.listWidget.getY() + this.listWidget.getHeight(), 0x33000000);
        this.listWidget.draw(mouseX, mouseY);
        this.drawCenteredString(fontRendererObj, "Boots List", this.width / 2, 20, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            this.listWidget.mouseScrolled(0, 0, 0, wheel > 0 ? -1 : 1);
        }

    }
}