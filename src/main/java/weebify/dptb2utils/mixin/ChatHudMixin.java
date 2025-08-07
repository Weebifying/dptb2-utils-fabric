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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"))
    private void addMessageInject(Text message, CallbackInfo ci) {
        DPTB2Utils mod = DPTB2Utils.getInstance();
        MinecraftClient mc = MinecraftClient.getInstance();

        String content = message.getString().replaceAll("§[0-9a-fk-or]", "").trim();
        SoundEvent sound = SoundEvents.ENTITY_PLAYER_LEVELUP;

        if (mod.getBoolNotifs("shopUpdate") && content.startsWith("* SHOP! New items available at the Rotating Shop!")) {
            triggerNotif("Shop Update!", "New items available at the Rotating Shop!", 0xFF55FF, sound);
        } else if (content.startsWith("* [!] MAYHEM! The BUTTON has no cooldown for 10s!")) {
            ButtonTimerManager.isMayhem = true;
            mod.scheduleTask(200, () -> ButtonTimerManager.isMayhem = false);
            if (mod.getBoolNotifs("buttonMayhem")) {
                triggerNotif("Button Mayhem!", "The BUTTON has no cooldown for 10s!", 0xFF0000, sound);
            }
        } else if (content.startsWith("* [!] The BUTTON has been disabled for 5s!")) {
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

        if (mod.isRamper && !content.isBlank()) {
            // inclusion
            if (content.matches("[^:]+:.+") && !content.startsWith("* ")) {
                if (
                        !content.startsWith("From ")
                     && !content.startsWith("To ")
                     && !content.startsWith("Party >")
                     && !content.startsWith("Guild >")
                     && !content.startsWith("Officer >")
                     && !content.startsWith("You'll be ")
                ) {
                    mod.websocketClient.sendModMessage("chat", Map.of("text", message.getString()));
                }
            } else if (content.matches("\\* .+")) {
                handleSystemMessage(content, message.getString());
            }
        }
    }

    @Unique
    private static void handleSystemMessage(String content, String message) {
        // number of lines
        if (content.startsWith("*   MINOR EVENT! ➜ SANDSTORM")) counter = 5;
        else if (content.startsWith("*   The SANDSTORM has ended!")) counter = 1;
        else if (content.startsWith("*   MINOR EVENT! ➜ HEAT WAVE")) counter = 5;
        else if (content.startsWith("*   MINOR EVENT! ➜ CHAOS BUTTON")) counter = 5;
        else if (content.startsWith("*   MINOR EVENT! ➜ DON'T PRESS THE BUTTON (literally)")) counter = 6;
        else if (content.startsWith("*   The Don't Press the Button (literally) event has ended!")) counter = 6;
        else if (content.startsWith("*  MINOR EVENT! ➜ PRESS THE BUTTON")) counter = 3;
        else if (content.startsWith("*  The Press the Button event has ended!")) counter = 1;
        else if (content.startsWith("*  GG! The BUTTON was pressed by ")) counter = 2;
        else if (content.startsWith("*   GG! The BUTTON was not pressed for ")) counter = 3;
        else if (content.startsWith("*   MEGA EVENT! ➜ RAFFLE")) counter = 4;
        else if (content.startsWith("*   The BANK is now off cooldown!")) counter = 1;
        else if (content.startsWith("*   THE BANK HAS BEEN BROKEN INTO!")) counter = 2;
        else if (content.startsWith("*   THE BANK HAS CLOSED!")) counter = 3;
        else if (content.startsWith("*   BANK HEIST SUCCESS!")) counter = 3;
        else if (content.startsWith("*   PARKOUR CIVILIZATION Challenge complete!")) counter = 8;
        else if (content.startsWith("*   MEGA EVENT! ➜ DERBY")) counter = 5;
        else if (content.startsWith("*  MEGA EVENT! ➜ DERBY")) counter = 3;
        else if (content.startsWith("*   THE DERBY HAS ENDED!")) counter = 6;
        else if (content.startsWith("*   MEGA EVENT! ➜ GANG WARFARE")) counter = 4;
        else if (content.startsWith("*   The GANG WARFARE has ended!")) counter = 3;
        else if (content.startsWith("* TOP BUTTON PRESSERS")) counter = 3;
        else if (content.startsWith("* Map is preparing to change...")) counter = 4;
        else if (content.startsWith("* MOST WANTED")) counter = 5;
        else if (content.startsWith("*   REWARDS:")) counter = 4;
        else if (content.startsWith("*   CATACLYSMIC EVENT! ➜ HIGH NOON")) counter = 5;
        else if (content.startsWith("*   GUNS GIVEN!")) counter = 4;
        else if (content.startsWith("*   BOUNTY INCREASE!")) counter = 2;
        else if (content.startsWith("*   WANTED DEAD OR ALIVE!")) counter = 3;
        else if (content.startsWith("* COOKIE GOAL REACHED!")) counter = 7;
        else if (content.startsWith("* / / BUTTON Statistics \\ \\")) counter = 6;
        else if (content.startsWith("* ➜ The BUTTON was just clicked by")) counter = 2;
        else if (content.startsWith("* [!] Whoever clicks the BUTTON next will not die!")) counter = 2;

        if (ButtonTimerManager.isMayhem) counter = 1;

        if (counter > 0) {
            if (filter(content)) bulks.add(message);
            if (!ButtonTimerManager.isMayhem) {
                if (content.matches("\\* {3}Starting in [0-9,]+s!")) {
                    if (bulks.getFirst().contains("CATACLYSMIC EVENT")) {
                        counter = 2;
                    } else {
                        counter = 0;
                    }
                } else if (!content.equalsIgnoreCase("* They used a Remote Activation on that press!")
                        || !bulks.getFirst().contains("The BUTTON was just clicked")) {
                    counter--;
                }
            }
        }

        if (counter == 0) {
            if (!bulks.isEmpty()) {
                String bulkMessage = String.join("\n", bulks);
                boolean format = !(bulkMessage.startsWith("* ➜ The BUTTON was just clicked by") || bulkMessage.startsWith("* [!] Whoever clicks the BUTTON next will not die"));
                bulks.clear();
                DPTB2Utils.getInstance().websocketClient.sendModMessage("chat", Map.of("text", format ? String.format("* \n%s\n* ", bulkMessage) : bulkMessage));
                return;
            }

            if (content.startsWith("*   SEWER TRAVEL!") || content.startsWith("*   CANNON")) {
                isTravel = true;
                return;
            }

            if (filter(content)) {
                if (isTravel) isTravel = false;
                else {
                    DPTB2Utils.getInstance().websocketClient.sendModMessage("chat", Map.of("text", message));
                }
            }
        }
    }

    @Unique
    private static boolean filter(String content) {
        String lower = content.toLowerCase();

        return !lower.contains("is currently on cooldown")
            && !lower.contains("is on cooldown")
            && !lower.contains("is currently disabled")
            && !lower.contains(" you ")
            && !lower.contains("your ending bounty")
            && !lower.contains("total from bounty")
            && !lower.contains("s remaining")
            && !lower.startsWith("*  - ")
            && !lower.startsWith("* - ")
            && !lower.startsWith("* [stats]")
            && !lower.startsWith("* [debug]")
            && !lower.startsWith("* oops")
            && !lower.startsWith("* tip")
            && !lower.startsWith("* quest complete")
            && !lower.startsWith("* daily quests complete")
            && !lower.startsWith("* new quests")
            && !lower.startsWith("* [#")
            && !lower.startsWith("* discord")
            && !lower.startsWith("* settings")
            && !lower.startsWith("* lootbox")
            && !lower.startsWith("* cha ching")
            && !lower.startsWith("*   good job")
            && !lower.startsWith("* wahoo")
            && !lower.startsWith("* reward")
            && !lower.startsWith("* error")
            && !lower.startsWith("* hey")
            && !lower.startsWith("* time until next daily reward:")
            && !lower.startsWith("* yay")
            && !lower.startsWith("* sorry")
            && !lower.startsWith("*   afk")
            && !lower.startsWith("* uh oh...")
            && !lower.startsWith("* better hurry!")
            && !lower.startsWith("* final stretch!")
            && !lower.startsWith("* oh no!")
            && !lower.startsWith("* run started!")
            && !lower.startsWith("* whoah!")
            && !lower.startsWith("* ouch!")
            && !lower.startsWith("*  earn points")
            && !lower.startsWith("* the top 3 get")
            && !lower.startsWith("* welcome to 7/11!")
            && !lower.startsWith("* -----")
            && !lower.startsWith("*   take ")
            && !lower.startsWith("* log back on in")
            && !lower.startsWith("* the button is currently on cooldown")
            && !lower.startsWith("*   with a time of")
            && !lower.startsWith("*   joined the bank heist!")
            && !lower.startsWith("* completion streak!")
            && !lower.startsWith("* ➜ get another completion in the next")
            && !lower.startsWith("* --- legendary games")
            && !lower.startsWith("* join our discord")
            && !lower.startsWith("* https://")
            && !lower.startsWith("* apply for staff")
            && !lower.matches("\\* [0-9,]+⛂ gold & [0-9,]+xp from that completion streak!")
            && !lower.matches("\\* successfully converted [0-9,]+⛂ gold into stat form!")
            && !lower.matches("\\* total: [0-9,]+⛂ gold")
            && !Pattern.compile("\\* \\+[0-9,]+⛂").matcher(lower).find();
    }
}

