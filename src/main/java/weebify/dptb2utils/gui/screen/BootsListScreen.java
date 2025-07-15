package weebify.dptb2utils.gui.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ScrollableTextWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.gui.ScrollableBootsList;

import java.util.List;

public class BootsListScreen extends Screen {
    private final DPTB2Utils mod;
    public Screen parent;
    public ScrollableBootsList listWidget;

    protected BootsListScreen(Screen parent, DPTB2Utils mod) {
        super(Text.of("Boots List"));
        this.parent = parent;
        this.mod = mod;
    }

    @Override
    protected void init() {
        listWidget = new ScrollableBootsList(40, 40, this.width-80, this.height-80-30, 3, 5, this.mod.bootsList, this.textRenderer);
        this.addDrawableChild(listWidget);

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), (btn) -> {
            assert this.client != null;
            this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 75, this.height - 30 - 10, 150, 20).build());
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
        context.fill(this.listWidget.getX(), this.listWidget.getY(),
                     this.listWidget.getX() + this.listWidget.getWidth(),
                     this.listWidget.getY() + this.listWidget.getHeight(), 0x33000000);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width/2, 20, 0xFFFFFF);

    }

    @Override
    public boolean mouseScrolled(double x, double y, double horizontalAmount, double verticalAmount) {
        if (listWidget.mouseScrolled(x, y, 0, verticalAmount)) return true;
        return super.mouseScrolled(x, y, horizontalAmount, verticalAmount);
    }
}
