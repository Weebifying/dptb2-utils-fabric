package weebify.dptb2utils.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import weebify.dptb2utils.DPTB2Utils;
import weebify.dptb2utils.gui.NotificationToast;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@Inject(method = "onGameMessage", at=@At("HEAD"))
	private void onGameMessageInject(GameMessageS2CPacket packet, CallbackInfo ci) {
		if (!packet.overlay()) {
			DPTB2Utils mod = DPTB2Utils.getInstance();
			MinecraftClient mc = MinecraftClient.getInstance();
			ToastManager toastManager = mc.getToastManager();

			String org = packet.content().getLiteralString();
            if (org == null) {
				org = packet.content().getString();
			}
            String content = org.replaceAll("§[0-9a-fk-or]", "");

			if (mod.action) {
				if (mod.getNotifs("shopUpdate") && content.startsWith("* SHOP! New items available at the Rotating Shop!")) {
					toastManager.add(new NotificationToast("Shop Update!", "New items available at the Rotating Shop!", 0xFFFF55FF));
					mod.action = false;
				} else if (mod.getNotifs("buttonMayhem") && content.startsWith("* [!] MAYHEM! The BUTTON has no cooldown for 10s!")) {
					toastManager.add(new NotificationToast("Button Mayhem!", "The BUTTON has no cooldown for 10s!", 0xFFFF0000));
					mod.action = false;
				} else if (mod.getNotifs("buttonDisable") && content.startsWith("* [!] The BUTTON has been disabled for 5s!")) {
					toastManager.add(new NotificationToast("Button Disabled!", "The BUTTON has been disabled for 5s!", 0xFF00FF00));
					mod.action = false;
				} else if (mod.getNotifs("bootsCollected") && content.startsWith("* WOAH!")) {
					toastManager.add(new NotificationToast("Boots Acquisition!", "Someone just collected a rare boots!", 0xFFFFFF55));
					mod.action = false;

					mod.bootsList.add(packet.content());
				}
				else if (mod.getNotifs("doorSwitch") && content.startsWith("* [!] The DOOR has cycled! Which one is it now?")) {
					toastManager.add(new NotificationToast("Door Switch!", "The DOOR has cycled! Which one is it now?", 0xFFFFAA00));
					mod.action = false;
				}

				if (mod.getAutoCheer() && content.startsWith("* COMMUNITY GOAL!")) {
					if (mc.getNetworkHandler() != null) {
						mc.getNetworkHandler().sendChatCommand("cheer");
						mod.action = false;
					}
				}
			}

		}
	}
}