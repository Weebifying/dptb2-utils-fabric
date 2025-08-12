package weebify.dptb2utils.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.List;

// i love vibecoding
// will probably check later though..
public class ScrollableBootsList extends Gui {
    private final Minecraft mc;
    private final FontRenderer font;
    private final List<String> items;

    private final int x, y, width, height;
    private final int entrySpacing;
    private final int padding;

    private float scrollAmount = 0;
    private boolean scrolling = false;
    private int lastMouseY;

    public ScrollableBootsList(int x, int y, int width, int height,
                               int entrySpacing, int padding,
                               List<String> items, FontRenderer font) {
        this.mc = Minecraft.getMinecraft();
        this.font = font;
        this.items = items;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.entrySpacing = entrySpacing;
        this.padding = padding;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void draw(int mouseX, int mouseY) {
        // Background
        drawRect(x, y, x + width, y + height, 0x33000000);

        // Enable scissor to clip entries
        double scale = mc.displayHeight / (double) mc.currentScreen.height;
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int) (x * scale),
                mc.displayHeight - (int) ((y + height) * scale),
                (int) (width * scale),
                (int) (height * scale));

        int lineY = y - (int) scrollAmount;
        for (String item : items) {
            int textHeight = font.FONT_HEIGHT;
            font.drawString(item, x + padding, lineY, 0xFFFFFF);
            lineY += textHeight + entrySpacing;
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // Scrollbar
        int contentHeight = items.size() * (font.FONT_HEIGHT + entrySpacing);
        if (contentHeight > height) {
            int barHeight = (int) ((float) height * height / contentHeight);
            barHeight = Math.max(barHeight, 10);
            int barY = y + (int) (scrollAmount * (height - barHeight) / (contentHeight - height));
            drawRect(x + width - 6, barY, x + width - 2, barY + barHeight, 0xFFAAAAAA);
        }

        handleMouse(mouseX, mouseY);
    }

    private void handleMouse(int mouseX, int mouseY) {
        int contentHeight = items.size() * (font.FONT_HEIGHT + entrySpacing);
        if (contentHeight <= height) return;

        // Mouse wheel
        int wheel = Mouse.getDWheel();
        if (wheel != 0) {
            scrollAmount -= Math.signum(wheel) * (font.FONT_HEIGHT + entrySpacing);
        }

        // Scrollbar dragging
        boolean overBar = mouseX >= x + width - 6 && mouseX <= x + width - 2 && mouseY >= y && mouseY <= y + height;
        if (Mouse.isButtonDown(0) && (overBar || scrolling)) {
            if (!scrolling) {
                scrolling = true;
                lastMouseY = mouseY;
            } else {
                float delta = mouseY - lastMouseY;
                lastMouseY = mouseY;
                float scrollRatio = (float) (contentHeight - height) / (height - getBarHeight(contentHeight));
                scrollAmount += delta * scrollRatio;
            }
        } else {
            scrolling = false;
        }

        clampScroll(contentHeight);
    }

    private int getBarHeight(int contentHeight) {
        int barHeight = (int) ((float) height * height / contentHeight);
        return Math.max(barHeight, 10);
    }

    private void clampScroll(int contentHeight) {
        if (scrollAmount < 0) scrollAmount = 0;
        if (scrollAmount > contentHeight - height) scrollAmount = contentHeight - height;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        int contentHeight = items.size() * (font.FONT_HEIGHT + entrySpacing);
        if (contentHeight <= height) return false;
        scrollAmount -= vertical * (font.FONT_HEIGHT + entrySpacing);
        clampScroll(contentHeight);
        return true;
    }
}