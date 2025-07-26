package weebify.dptb2utils.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.gui.widget.NotificationToast;
import weebify.dptb2utils.utils.ButtonTimerManager;

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
    private static boolean isTravel = false;

    @Unique
    private static void triggerNotif(String title, String message, int color, SoundEvent sfx) {
        DPTB2Utils mod = DPTB2Utils.getInstance();
        MinecraftClient mc = MinecraftClient.getInstance();
        ToastManager toastManager = mc.getToastManager();
        if (mod.getBoolNotifs("dontDelaySfx")) {
            mc.getSoundManager().play(PositionedSoundInstance.master(sfx, 1, 1));
        }
        toastManager.add(new NotificationToast(title, message, color, mod.getBoolNotifs("dontDelaySfx") ? null : sfx));
    }

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"))
    private void addMessageInject(Text message, CallbackInfo ci) {
        DPTB2Utils mod = DPTB2Utils.getInstance();
        MinecraftClient mc = MinecraftClient.getInstance();

        String content = message.getString().replaceAll("§[0-9a-fk-or]", "").trim();
        SoundEvent sound = SoundEvents.ENTITY_PLAYER_LEVELUP;

        if (mod.isRamper && !content.isBlank()) {
            // inclusion
            if
            (
                    content.matches("[^:]+:.+")
                 || content.matches("\\* .+")
            ) {
                // exclusion
                if
                (
                        content.startsWith("*   SEWER TRAVEL!")
                     || content.startsWith("*   CANNON!")
                ) {
                    isTravel = true;
                } else if
                (
                        !content.startsWith("* TIP!")
                     && !content.startsWith("* DISCORD!")
                     && !content.startsWith("* LOOTBOX!")
                     && !content.startsWith("* CHA CHING!")
                     && !content.startsWith("* Reward:")
                     && !content.startsWith("* Error! You have already claimed")
                     && !content.startsWith("* Time until next daily reward:")
                     && !content.startsWith("* YAY!")
                     && !content.startsWith("* SORRY!")
                     && !content.startsWith("* UH OH...")
                     && !content.startsWith("* BETTER HURRY!")
                     && !content.startsWith("* FINAL STRETCH!")
                     && !content.startsWith("* OH NO!")
                     && !content.startsWith("* Run started!")
                     && !content.startsWith("* Whoah!")
                     && !content.startsWith("* OUCH!")
                     && !content.startsWith("* Welcome to 7/11!")
                     && !content.startsWith("* -----") // wrappers
                     && !content.startsWith("* You earned")
                     && !content.startsWith("*   CONGRATS! You made it to the end!")
                     && !content.startsWith("*   With a time of")
                     && !content.startsWith("* COMPLETION STREAK!")
                     && !content.startsWith("* [!] You are now on a")
                     && !content.startsWith("* ➜ Get another Completion in the next")
                     && !content.startsWith("* --- LEGENDARY GAMES")
                     && !content.startsWith("* Join our Discord")
                     && !content.startsWith("* https://")
                     && !content.startsWith("* Apply for staff")
                     && !content.matches("\\* [0-9,]+⛂ Gold & [0-9,]+xp from that Completion Streak!")
                     && !content.matches("\\* Successfully converted [0-9,]+⛂ gold into stat form!")
                     && !content.matches("\\* Total: [0-9,]+⛂ Gold")
                     && !(Pattern.compile("\\* \\+[0-9,]+⛂").matcher(content).find())
                ) {
                    if (isTravel) {
                        isTravel = false;
                    } else {
                        DPTB2Utils.websocketClient.sendModMessage("chat", message.getString());
                    }
                }
            }
        }

        if (mod.getBoolNotifs("shopUpdate") && content.startsWith("* SHOP! New items available at the Rotating Shop!")) {
            triggerNotif("Shop Update!", "New items available at the Rotating Shop!", 0xFF55FF, sound);
        } else if (content.startsWith("* [!] MAYHEM! The BUTTON has no cooldown for 10s!")) {
            ButtonTimerManager.isMayhem = true;
            mod.scheduleTask(200, () -> ButtonTimerManager.isMayhem = false);
            if (mod.getBoolNotifs("buttonMayhem")) {
                triggerNotif("Button Mayhem!", "The BUTTON has no cooldown for 10s!", 0xFF0000, sound);
            }
        } else if  (content.startsWith("* [!] The BUTTON has been disabled for 5s!")) {
            ButtonTimerManager.isDisabled = true;
            mod.scheduleTask(100, () -> ButtonTimerManager.isDisabled = false);
            if (mod.getBoolNotifs("buttonDisable")) {
                triggerNotif("Button Disabled!", "The BUTTON has been disabled for 5s!", 0x00FF00, sound);
            }
        } else if (mod.getBoolNotifs("buttonImmunity") && content.startsWith("* [!] Whoever clicks the BUTTON next will not die!")) {
            triggerNotif("Button Immunity!", "Whoever clicks the BUTTON next will not die!", 0x55FFFF, sound);
        } else if (content.startsWith("* WOAH")) {
            if (mod.getBoolNotifs("bootsCollected")) {
                // placeholders in case shit goes down
                String t = "Someone just found a rare boots!";
                String b = "Boots";
                Pattern pattern1 = Pattern.compile("\\* WOAH!? \\[([\\w-]+)] (\\w+) just found ([A-Z]+) (.+?)!");
                Pattern pattern2 = Pattern.compile("\\* WOAH!? \\[([\\w-]+)] (\\w+) received (.+?) from an \\[Admin]");
                Pattern pattern3 = Pattern.compile("\\* WOAH!? \\[([\\w-]+)] (\\w+) just found (.+?)!");

                Matcher matcher1 = pattern1.matcher(content);
                Matcher matcher2 = pattern2.matcher(content);
                Matcher matcher3 = pattern3.matcher(content);

                if (matcher1.find()) {
                    t = String.format("[%s] %s found %s %s!", matcher1.group(1), matcher1.group(2), matcher1.group(3), matcher1.group(4));
                    b = matcher1.group(4);
                } else if (matcher2.find()) {
                    t = String.format("[%s] %s received %s!", matcher2.group(1), matcher2.group(2), matcher2.group(3));
                    b = matcher2.group(3);
                } else if (matcher3.find()) {
                    t = String.format("[%s] %s found %s!", matcher3.group(1), matcher3.group(2), matcher3.group(3));
                    b = matcher3.group(3);
                }

                if (mod.getBoolNotifs("slimeBoots") || !b.equalsIgnoreCase("Slime Boots")) {
                    triggerNotif(b + " Found!", t, 0xFFFF55, sound);
                }
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            Text text = Text.empty()
                    .append(Text.literal(String.format("[%s] ", timestamp)).formatted(Formatting.GRAY)
                    .append(message));
            mod.bootsList.add(text);
        } else if (mod.getBoolNotifs("doorSwitch") && content.startsWith("* [!] The DOOR has cycled! Which one is it now?")) {
            triggerNotif("Door Switch!", "The DOOR has cycled! Which one is it now?", 0xFFAA00, sound);
        } else if (mod.getAutoCheer() && content.startsWith("* COMMUNITY GOAL!")) {
            if (mc.getNetworkHandler() != null) {
                mod.scheduleTask(rand.nextInt(26) + 5, () -> mc.getNetworkHandler().sendChatCommand("cheer"));
            }
        } else if (content.startsWith("* ➜ The BUTTON was just clicked")) {
            ButtonTimerManager.buttonTimer = 0; // reset the button timer

            // chaos button handling
            if (content.endsWith("by CHAOS!")) {
                if (ButtonTimerManager.isChaos) {
                    ButtonTimerManager.chaosCounter -= 1;
                    if (ButtonTimerManager.chaosCounter <= 0) {
                        ButtonTimerManager.isChaos = false;
                    }
                } else {
                    ButtonTimerManager.isChaos = true;
                    ButtonTimerManager.chaosCounter = 32;
                }
            }
        } else if (content.startsWith("*   MINOR EVENT! ➜ CHAOS BUTTON")) {
            ButtonTimerManager.buttonTimer = 0;
            ButtonTimerManager.isChaos = true;
            ButtonTimerManager.chaosCounter = 33;
        }
    }
}
