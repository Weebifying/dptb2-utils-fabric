package weebify.dptb2utils.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.List;
import java.util.stream.Collectors;

// Quite literally a carbon copy of ScrollableWidget from 1.21.4
public class ScrollableBootsList extends BetterScrollableWidget {
    private final List<OrderedText> lines;
    private final TextRenderer textRenderer;
    private final int lineSpacing;
    private final int padding;

    public ScrollableBootsList(int x, int y, int width, int height, int lineHeight, int padding, List<Text> lines, TextRenderer textRenderer) {
        super(x, y, width, height, Text.empty());
        this.textRenderer = textRenderer;
        this.lineSpacing = lineHeight;
        this.padding = padding;
        this.lines = lines.stream()
                .flatMap(t -> MinecraftClient.getInstance().textRenderer.wrapLines(t, width - SCROLLBAR_WIDTH - 4).stream())
                .collect(Collectors.toList());
    }

    @Override
    protected int getContentsHeightWithPadding() {
        return padding * 2
                + lines.size() * (textRenderer.fontHeight + this.lineSpacing);
    }

    @Override
    protected double getDeltaYPerScroll() {
        return textRenderer.fontHeight + lineSpacing;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        drawContent(context);
        drawScrollbar(context);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (checkScrollbarDragged(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        // forward scroll wheel
        return super.mouseScrolled(mx, my, 0, dy);
    }

    private void drawContent(DrawContext context) {
        int startY = getY() + padding;
        int yOffset = startY - (int) getScrollY();

        for (int i = 0; i < lines.size(); i++) {
            int drawY = yOffset + i * (textRenderer.fontHeight + lineSpacing);
            // only draw visible lines
            if (drawY + textRenderer.fontHeight >= getY()
                    && drawY <= getY() + height) {
                context.drawTextWithShadow(
                        textRenderer,
                        lines.get(i),
                        getX() + 2,
                        drawY,
                        0xFFFFFF
                );
            }
        }
    }
}
