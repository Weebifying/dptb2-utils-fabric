package weebify.dptb2utils.gui.widget;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import weebify.dptb2utils.DPTB2Utils;

import java.util.List;

public class NotificationToast implements Toast {
    private static final Identifier TEXTURE = Identifier.ofVanilla("toast/advancement");
    private static final Identifier ICON = Identifier.of(DPTB2Utils.MOD_ID, "textures/notif.png");
    public static final float DEFAULT_DURATION_MS = 8000;
    public static final float TITLE_PHASE_MS = 2500;
    public static final float FADE_DURATION = 300;
    private final String title;
    private final String description;
    private final int color;
    private final SoundEvent sfx;
    private boolean soundPlayed = false;
    private Toast.Visibility visibility = Toast.Visibility.HIDE;

    public NotificationToast(String title, String description, int color, @Nullable SoundEvent sfx) {
        this.title = title;
        this.description = description;
        this.color = color;
        this.sfx = sfx;
    }

    @Override
    public Visibility getVisibility() {
        return this.visibility;
    }

    @Override
    public void update(ToastManager manager, long time) {
        if (!this.soundPlayed && time > 0) {
            this.soundPlayed = true;
            if (this.sfx != null) {
                manager.getClient().getSoundManager().play(PositionedSoundInstance.master(this.sfx, 1, 1));
            }
        }

        this.visibility = time >= DEFAULT_DURATION_MS * manager.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

    @Override
    public void draw(DrawContext context, TextRenderer textRenderer, long startTime) {
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, this.getWidth(), this.getHeight());

        List<OrderedText> list = textRenderer.wrapLines(StringVisitable.plain(this.description), 125);
        if (list.size() == 1) {
            context.drawText(textRenderer, this.title, 30, 7, this.color, false);
            context.drawText(textRenderer, list.get(0), 30, 18, this.color, false);
        } else {
            if (startTime < TITLE_PHASE_MS) {
                int k = MathHelper.floor(MathHelper.clamp((TITLE_PHASE_MS - startTime) / FADE_DURATION, 0.f, 1.f) * 255.f) << 24 | 67108864;
                context.drawText(textRenderer, this.title, 30, 11, this.color & Colors.WHITE | k, false);
            } else {
                int k = MathHelper.floor(MathHelper.clamp((startTime - TITLE_PHASE_MS) / FADE_DURATION, 0.f, 1.f) * 252.f) << 24 | 67108864;
                int l = this.getHeight() / 2 - list.size() * 9 / 2;


                for (OrderedText orderedText : list) {
                    context.drawText(textRenderer, orderedText, 30, l, this.color & Colors.WHITE | k, false);
                    l += 9;
                }
            }
        }

        context.drawTexture(RenderPipelines.GUI_TEXTURED, ICON, 8, 8, 0, 0, 16, 16, 16, 16, this.color & Colors.WHITE | 0xFF000000);
    }
}
