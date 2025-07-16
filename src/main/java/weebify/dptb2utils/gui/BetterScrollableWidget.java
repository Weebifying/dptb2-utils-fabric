package weebify.dptb2utils.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public abstract class BetterScrollableWidget extends ClickableWidget {
    public static final int SCROLLBAR_WIDTH = 6;
    private double scrollY;
    private static final Identifier SCROLLER_TEXTURE = Identifier.of("minecraft", "widget/scroller");
    private static final Identifier SCROLLER_BACKGROUND_TEXTURE = Identifier.of("minecraft", "widget/scroller_background");
    private boolean scrollbarDragged;

    public BetterScrollableWidget(int i, int j, int k, int l, Text text) {
        super(i, j, k, l, text);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!this.visible) {
            return false;
        } else {
            this.setScrollY(this.getScrollY() - verticalAmount * this.getDeltaYPerScroll());
            return true;
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.scrollbarDragged) {
            if (mouseY < this.getY()) {
                this.setScrollY(0.0);
            } else if (mouseY > this.getBottom()) {
                this.setScrollY(this.getMaxScrollY());
            } else {
                double d = Math.max(1, this.getMaxScrollY());
                int i = this.getScrollbarThumbHeight();
                double e = Math.max(1.0, d / (this.height - i));
                this.setScrollY(this.getScrollY() + deltaY * e);
            }

            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        this.scrollbarDragged = false;
    }

    public double getScrollY() {
        return this.scrollY;
    }

    public void setScrollY(double scrollY) {
        this.scrollY = MathHelper.clamp(scrollY, 0.0, (double)this.getMaxScrollY());
    }

    public boolean checkScrollbarDragged(double mouseX, double mouseY, int button) {
        this.scrollbarDragged = this.overflows()
                && this.isValidClickButton(button)
                && mouseX >= this.getScrollbarX()
                && mouseX <= this.getScrollbarX() + 6
                && mouseY >= this.getY()
                && mouseY < this.getBottom();
        return this.scrollbarDragged;
    }

    public void refreshScroll() {
        this.setScrollY(this.scrollY);
    }

    public int getMaxScrollY() {
        return Math.max(0, this.getContentsHeightWithPadding() - this.height);
    }

    /**
     * {@return whether the contents overflow and needs a scrollbar}
     */
    protected boolean overflows() {
        return this.getMaxScrollY() > 0;
    }

    protected int getScrollbarThumbHeight() {
        return MathHelper.clamp((int)((float)(this.height * this.height) / this.getContentsHeightWithPadding()), 32, this.height - 8);
    }

    protected int getScrollbarX() {
        return this.getRight() - 6;
    }

    protected int getScrollbarThumbY() {
        return Math.max(this.getY(), (int)this.scrollY * (this.height - this.getScrollbarThumbHeight()) / this.getMaxScrollY() + this.getY());
    }

    protected void drawScrollbar(DrawContext context) {
        if (this.overflows()) {
            int i = this.getScrollbarX();
            int j = this.getScrollbarThumbHeight();
            int k = this.getScrollbarThumbY();
            context.drawGuiTexture(SCROLLER_BACKGROUND_TEXTURE, i, this.getY(), 6, this.getHeight());
            context.drawGuiTexture(SCROLLER_TEXTURE, i, k, 6, j);
        }
    }

    protected abstract int getContentsHeightWithPadding();

    protected abstract double getDeltaYPerScroll();
}
