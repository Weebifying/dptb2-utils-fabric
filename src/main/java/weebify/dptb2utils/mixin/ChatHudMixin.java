package weebify.dptb2utils.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.gui.NotificationToast;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Mixin(ChatHud.class)
public class ChatHudMixin {
    @Unique
    private static final Random rand = new Random();
    
    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"))
    private void addMessageInject(Text message, CallbackInfo ci) {
        DPTB2Utils mod = DPTB2Utils.getInstance();
        MinecraftClient mc = MinecraftClient.getInstance();
        ToastManager toastManager = mc.getToastManager();

        String content = message.getString().replaceAll("§[0-9a-fk-or]", "");

        if (mod.getNotifs("shopUpdate") && content.startsWith("* SHOP! New items available at the Rotating Shop!")) {
            toastManager.add(new NotificationToast("Shop Update!", "New items available at the Rotating Shop!", 0xFFFF55FF));
        } else if (mod.getNotifs("buttonMayhem") && content.startsWith("* [!] MAYHEM! The BUTTON has no cooldown for 10s!")) {
            toastManager.add(new NotificationToast("Button Mayhem!", "The BUTTON has no cooldown for 10s!", 0xFFFF0000));
        } else if (mod.getNotifs("buttonDisable") && content.startsWith("* [!] The BUTTON has been disabled for 5s!")) {
            toastManager.add(new NotificationToast("Button Disabled!", "The BUTTON has been disabled for 5s!", 0xFF00FF00));
        } else if (mod.getNotifs("buttonImmunity") && content.startsWith("* [!] Whoever clicks the BUTTON next will not die!")) {
            toastManager.add(new NotificationToast("Button Immunity!", "Whoever clicks the BUTTON next will not die!", 0xFF55FFFF));
        } else if (mod.getNotifs("bootsCollected") && content.startsWith("* WOAH!")) {
            toastManager.add(new NotificationToast("Boots Acquisition!", "Someone just collected a rare boots!", 0xFFFFFF55));

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            Text text = Text.empty()
                    .append(Text.literal(String.format("[%s] ", timestamp)).formatted(Formatting.GRAY)
                    .append(message));
            mod.bootsList.add(text);
        }
        else if (mod.getNotifs("doorSwitch") && content.startsWith("* [!] The DOOR has cycled! Which one is it now?")) {
            toastManager.add(new NotificationToast("Door Switch!", "The DOOR has cycled! Which one is it now?", 0xFFFFAA00));

        }

        if (mod.getAutoCheer() && content.startsWith("* COMMUNITY GOAL!")) {
            if (mc.getNetworkHandler() != null) {
                mod.scheduleTask(rand.nextInt(26) + 5, () -> mc.getNetworkHandler().sendChatCommand("cheer"));
            }
        }
    }
}
