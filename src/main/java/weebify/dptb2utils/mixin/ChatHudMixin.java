package weebify.dptb2utils.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.InGameHud;
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

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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
    private static int counter = 0;
    @Unique
    private static List<String> bulks = new ArrayList<>();

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

    @Inject(method = "addVisibleMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V", at = @At("HEAD"))
    private void addVisibleMessageInject(ChatHudLine message, CallbackInfo ci) {
        DPTB2Utils mod = DPTB2Utils.getInstance();
        MinecraftClient mc = MinecraftClient.getInstance();

        String content = message.content().getString().replaceAll("§[0-9a-fk-or]", "");
        SoundEvent sound = SoundEvents.ENTITY_PLAYER_LEVELUP;

        if (mod.isRamper && !content.isBlank()) {
            // inclusion
            if (content.matches("[^:]+:.+") && !content.startsWith("* ")) {
                if
                (
                        !content.startsWith("From ")
                     && !content.startsWith("To ")
                     && !content.startsWith("Party >")
                     && !content.startsWith("Guild >")
                     && !content.startsWith("Officer >")
                     && !content.startsWith("You'll be ")
                ) {
                    DPTB2Utils.websocketClient.sendModMessage("chat", message.getString());
                }
            } else if (content.matches("\\* .+")) {
                handleSystemMessage(content, message.content().getString());
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
                            .append(message.content()));
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

    @Unique
    private static void handleSystemMessage(String content, String message) {
        // number of lines
             if (content.startsWith("*   MINOR EVENT! ➜ SANDSTORM"))                            counter = 5;
        else if (content.startsWith("*   The SANDSTORM has ended!"))                            counter = 1;
        else if (content.startsWith("*   MINOR EVENT! ➜ HEAT WAVE"))                            counter = 5;
        else if (content.startsWith("*   MINOR EVENT! ➜ CHAOS BUTTON"))                         counter = 5;
        else if (content.startsWith("*   MINOR EVENT! ➜ DON'T PRESS THE BUTTON (literally)"))   counter = 6;
        else if (content.startsWith("*   GG! The BUTTON was not pressed for 45s!"))             counter = 3;
        else if (content.startsWith("*   MEGA EVENT! ➜ RAFFLE"))                                counter = 4;
        else if (content.startsWith("*   The BANK is now off cooldown!"))                       counter = 1;
        else if (content.startsWith("*   THE BANK HAS BEEN BROKEN INTO!"))                      counter = 2;
        else if (content.startsWith("*   THE BANK HAS CLOSED!"))                                counter = 3;
        else if (content.startsWith("*   BANK HEIST SUCCESS!"))                                 counter = 3;
        else if (content.startsWith("*   PARKOUR CIVILIZATION Challenge complete!"))            counter = 8;
        else if (content.startsWith("*   MEGA EVENT! ➜ DERBY"))                                 counter = 5;
        else if (content.startsWith("*  MEGA EVENT! ➜ DERBY"))                                  counter = 3;
        else if (content.startsWith("*   THE DERBY HAS ENDED!"))                                counter = 6;
        else if (content.startsWith("*   MEGA EVENT! ➜ GANG WARFARE"))                          counter = 4;
        else if (content.startsWith("*   The GANG WARFARE has ended!"))                         counter = 3;

        if (counter > 0) {
            bulks.add(message);
            if (content.matches("\\* {3}Starting in [0-9,]+s!")) {
                counter = 0;
            } else {
                counter--;
            }
        }

        if (counter == 0) {
            if (!bulks.isEmpty()) {
                String bulkMessage = String.join("\n", bulks);
                bulks.clear();
                DPTB2Utils.websocketClient.sendModMessage("chat", String.format("* \n%s\n* ", bulkMessage));
                return;
            }

            if (content.startsWith("*   SEWER TRAVEL!") || content.startsWith("*   CANNON")) {
                isTravel = true;
                return;
            }
            if
            (
                    !content.contains("is currently on cooldown!")
                 && !content.contains("is on Cooldown!")
                 && !content.startsWith("* [STATS]")
                 && !content.startsWith("*  - ")
                 && !content.startsWith("* OOPS")
                 && !content.startsWith("* TIP")
                 && !content.startsWith("* DISCORD")
                 && !content.startsWith("* SETTINGS")
                 && !content.startsWith("* LOOTBOX")
                 && !content.startsWith("* CHA CHING")
                 && !content.startsWith("* THANK YOU")
                 && !content.startsWith("*   GOOD JOB")
                 && !content.startsWith("* Reward")
                 && !content.startsWith("* Error")
                 && !content.startsWith("* Time until next daily reward:")
                 && !content.startsWith("* YAY")
                 && !content.startsWith("* SORRY")
                 && !content.startsWith("*   AFK")
                 && !content.startsWith("* UH OH...")
                 && !content.startsWith("* BETTER HURRY!")
                 && !content.startsWith("* FINAL STRETCH!")
                 && !content.startsWith("* OH NO!")
                 && !content.startsWith("* Run started!")
                 && !content.startsWith("* Whoah!")
                 && !content.startsWith("* OUCH!")
                 && !content.startsWith("* Welcome to 7/11!")
                 && !content.startsWith("* -----") // wrappers
                 && !content.startsWith("* SHOP! You received")
                 && !content.startsWith("*   You got")
                 && !content.startsWith("*   Take ")
                 && !content.startsWith("*   You obtained ")
                 && !content.startsWith("* You have claimed")
                 && !content.startsWith("* You are now on a")
                 && !content.startsWith("* Log back on in")
                 && !content.startsWith("* You earned")
                 && !content.startsWith("* You have been")
                 && !content.startsWith("* The button is currently on cooldown")
                 && !content.startsWith("* [!] You consumed the Immune Apple")
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
                if (isTravel) isTravel = false;
                else {
                    DPTB2Utils.websocketClient.sendModMessage("chat", message);
                }
            }
        }
    }
}
