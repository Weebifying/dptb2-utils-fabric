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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ChatHud.class)
public class ChatHudMixin {
    @Unique
    private static final Random rand = new Random();

    @Unique
    private static void triggerNotif(String title, String message, int color) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ToastManager toastManager = mc.getToastManager();
        toastManager.add(new NotificationToast(title, message, color));
    }

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"))
    private void addMessageInject(Text message, CallbackInfo ci) {
        DPTB2Utils mod = DPTB2Utils.getInstance();
        MinecraftClient mc = MinecraftClient.getInstance();

        String content = message.getString().replaceAll("§[0-9a-fk-or]", "");

        if (mod.getNotifs("shopUpdate") && content.startsWith("* SHOP! New items available at the Rotating Shop!")) {
            triggerNotif("Shop Update!", "New items available at the Rotating Shop!", 0xFF55FF);
        } else if (mod.getNotifs("buttonMayhem") && content.startsWith("* [!] MAYHEM! The BUTTON has no cooldown for 10s!")) {
            triggerNotif("Button Mayhem!", "The BUTTON has no cooldown for 10s!", 0xFF0000);
        } else if (mod.getNotifs("buttonDisable") && content.startsWith("* [!] The BUTTON has been disabled for 5s!")) {
            triggerNotif("Button Disabled!", "The BUTTON has been disabled for 5s!", 0x00FF00);
        } else if (mod.getNotifs("buttonImmunity") && content.startsWith("* [!] Whoever clicks the BUTTON next will not die!")) {
            triggerNotif("Button Immunity!", "Whoever clicks the BUTTON next will not die!", 0x55FFFF);
        } else if (mod.getNotifs("bootsCollected") && content.startsWith("* WOAH!")) {
            String t = content.substring(8);
            Pattern pattern1 = Pattern.compile("\\* WOAH! \\[(\\w+)] (\\w+) just found ([A-Z]+) (.+?)! \\(([^)]+)\\)");
            Pattern pattern2 = Pattern.compile("\\* WOAH! \\[(\\w+)] (\\w+) received (.+?) from an \\[Admin]");
            Pattern pattern3 = Pattern.compile("\\* WOAH! \\[(\\w+)] (\\w+) just found (.+?)! \\(\\?\\?\\?\\)");

            Matcher matcher1 = pattern1.matcher(content);
            Matcher matcher2 = pattern2.matcher(content);
            Matcher matcher3 = pattern3.matcher(content);

            if (matcher1.find()) {
                t = String.format("[%s] %s found %s %s!", matcher1.group(1), matcher1.group(2), matcher1.group(3), matcher1.group(4));
            } else if (matcher2.find()) {
                t = String.format("[%s] %s received %s!", matcher2.group(1), matcher2.group(2), matcher2.group(3));
            } else if (matcher3.find()) {
                t = String.format("[%s] %s found %s!", matcher3.group(1), matcher3.group(2), matcher3.group(3));
            }

            triggerNotif("Boots Found!", t, 0xFFFF55);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            Text text = Text.empty()
                    .append(Text.literal(String.format("[%s] ", timestamp)).formatted(Formatting.GRAY)
                    .append(message));
            mod.bootsList.add(text);
        }
        else if (mod.getNotifs("doorSwitch") && content.startsWith("* [!] The DOOR has cycled! Which one is it now?")) {
            triggerNotif("Door Switch!", "The DOOR has cycled! Which one is it now?", 0xFFAA00);
        }

        if (mod.getAutoCheer() && content.startsWith("* COMMUNITY GOAL!")) {
            if (mc.getNetworkHandler() != null) {
                mod.scheduleTask(rand.nextInt(26) + 5, () -> mc.getNetworkHandler().sendChatCommand("cheer"));
            }
        }
    }
}
