package weebify.dptb2utils.gui;

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import weebify.dptb2utils.DPTB2Utils;

import java.util.List;

public class NotificationToast implements Toast {
    private static final Identifier TEXTURE = Identifier.ofVanilla("toast/advancement");
    private static final Identifier ICON = Identifier.of(DPTB2Utils.MOD_ID, "textures/notif.png");
    public static final int DEFAULT_DURATION_MS = 8000;
    private final String title;
    private final String description;
    private final int color;
    private boolean soundPlayed = false;
    private Toast.Visibility visibility = Toast.Visibility.HIDE;

    public NotificationToast(String title, String description, int color) {
        this.title = title;
        this.description = description;
        this.color = color;
    }

    @Override
    public Visibility getVisibility() {
        return this.visibility;
    }

    @Override
    public void update(ToastManager manager, long time) {
        if (!this.soundPlayed && time > 0) {
            this.soundPlayed = true;
            manager.getClient().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_PLAYER_LEVELUP, 1, 1));
        }

        this.visibility = time >= 8000.0 * manager.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

    @Override
    public void draw(DrawContext context, TextRenderer textRenderer, long startTime) {
        context.drawGuiTexture(RenderLayer::getGuiTextured, TEXTURE, 0, 0, this.getWidth(), this.getHeight());

        List<OrderedText> list = textRenderer.wrapLines(StringVisitable.plain(this.description), 125);
        if (list.size() == 1) {
            context.drawText(textRenderer, this.title, 30, 7, this.color, false);
            context.drawText(textRenderer, list.get(0), 30, 18, Colors.WHITE, false);
        } else {
            int j = 2500;
            float f = 300.f;
            if (startTime < 2500) {
                int k = MathHelper.floor(MathHelper.clamp((float) (2500 - startTime) / 300.f, 0.f, 1.f) * 255.f);
                context.drawText(textRenderer, this.title, 30, 11, this.color & Colors.WHITE | k << 24, false);
            } else {
                int k = MathHelper.floor(MathHelper.clamp((float) (startTime - 2500) / 300.f, 0.f, 1.f) * 252.f);
                int l = this.getHeight() / 2 - list.size() * 9 / 2;

                for (OrderedText orderedText : list) {
                    context.drawText(textRenderer, orderedText, 30, l, this.color & Colors.WHITE | k << 24, false);
                    l += 9;
                }
            }
        }

        context.drawTexture(RenderLayer::getGuiTextured, ICON, 8, 8, 0, 0, 16, 16, 16, 16, this.color);
    }
}
