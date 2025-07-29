package weebify.dptb2utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.scoreboard.*;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weebify.dptb2utils.gui.screen.ButtonTimerConfigScreen;
import weebify.dptb2utils.gui.widget.NotificationToast;
import weebify.dptb2utils.gui.screen.ModMenuScreen;
import weebify.dptb2utils.utils.ButtonTimerManager;
import weebify.dptb2utils.utils.DelayedTask;
import weebify.dptb2utils.utils.DiscordWebSocketClient;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class DPTB2Utils implements ClientModInitializer {
	public static final String MOD_ID = "dptb2-utils";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public ModConfigs config;
	private File saveFile;
	private long lastSaved;

	private boolean displayScreen = false;
	public boolean isInDPTB2 = false;
	public boolean isRamper = false;

	public List<DelayedTask> scheduledTasks = new ArrayList<>();

	private static final MinecraftClient mc = MinecraftClient.getInstance();
	private static DPTB2Utils instance;
	public static final Gson GSON = new Gson();

//	public static String HOST = "79.99.40.71";
//	public static int PORT = 6426;
	public static DiscordWebSocketClient websocketClient;

	public List<Text> bootsList = new ArrayList<>();

	public static DPTB2Utils getInstance() {
		return instance;
	}

	@Override
	public void onInitializeClient() {
		instance = this;
		this.config = new ModConfigs();
		this.saveFile = new File(mc.runDirectory + "/config", "weebify_dptb2utils.json");
		try {
			if (this.saveFile.createNewFile()) {
				try (FileWriter fw = new FileWriter(this.saveFile)) {
					GSON.toJson(this.config, fw);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		this.lastSaved = saveFile.lastModified();
		this.loadSettings();

		initializeCommands();
		initializeEvents();
	}

	public void scheduleTask(int ticks, Runnable task) {
		this.scheduledTasks.add(new DelayedTask(ticks, task));
	}

	public void buttonTimerReset() {
		ButtonTimerManager.buttonTimer = -1;
		ButtonTimerManager.isMayhem = false;
		ButtonTimerManager.isChaos = false;
		ButtonTimerManager.isDisabled = false;
		ButtonTimerManager.chaosCounter = 0;
	}

	private void initializeEvents() {
		ClientTickEvents.START_CLIENT_TICK.register(this::onClientTick);
		ClientTickEvents.END_CLIENT_TICK.register((var) -> {
			scheduledTasks.removeIf(DelayedTask::tick);
		});
		// detecting whether the player is in DPTB2
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			this.buttonTimerReset();

			ServerInfo serverEntry = client.getCurrentServerEntry();
			if (serverEntry == null) {
				this.isInDPTB2 = false;
				return;
			}
			if (!serverEntry.address.toLowerCase().contains("hypixel.net")) {
				this.isInDPTB2 = false;
				return;
			}

			this.scheduleTask(20, () -> {
				if (client.world == null) return;

				Scoreboard scoreboard = client.world.getScoreboard();
				ScoreboardObjective objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);

				if (objective != null) {
					String title = objective.getDisplayName().getString().toLowerCase();
					Text[] sidebarEntries =scoreboard.getScoreboardEntries(objective)
							.stream()
							.filter(score -> !score.hidden())
							.sorted(Comparator.comparing(ScoreboardEntry::value).reversed().thenComparing(ScoreboardEntry::owner, String.CASE_INSENSITIVE_ORDER))
							.map(scoreboardEntry -> {
								Team team = scoreboard.getScoreHolderTeam(scoreboardEntry.owner());
								Text textx = scoreboardEntry.name();
                                return (Text) Team.decorateName(team, textx);
							})
							.toArray(Text[]::new);

					StringBuilder s = new StringBuilder();
					for (Text entry : sidebarEntries) {
						s.append(entry.getString());
					}

					String content = s.toString().toLowerCase().replaceAll("§\\w", "");

					this.isInDPTB2 = title.contains("housing") && content.contains("don't press the button 2");
//					LOGGER.info("isInDPTB2 = {}", this.isInDPTB2);
					if (this.isInDPTB2) client.getToastManager().add(new NotificationToast("DPTB2 Utils", "You are in Don't Press The Button 2!", 0xD2FFC8, SoundEvents.ENTITY_PLAYER_LEVELUP	));
					this.refreshRamperStatus();
				}
			});
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			this.buttonTimerReset();
			if (websocketClient != null && websocketClient.isOpen()) {
				websocketClient.close();
			}
		});

		// button timer hud
		HudRenderCallback.EVENT.register(((drawContext, renderTickCounter) -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			if (this.isInDPTB2 && this.getButtonTimerEnabled() && !(mc.currentScreen instanceof ButtonTimerConfigScreen)) {
				int width = mc.getWindow().getScaledWidth();
				int height = mc.getWindow().getScaledHeight();
				Text text = ButtonTimerManager.tickToTime(ButtonTimerManager.buttonTimer);
				int textWidth = mc.textRenderer.getWidth(text);
				if (this.getButtonTimerRenderBG()) {
					drawContext.fill(
							(int) (width*this.getButtonTimerConfigs("posX", Float.class)),
							(int) (height*this.getButtonTimerConfigs("posY", Float.class)),
							(int) (width*this.getButtonTimerConfigs("posX", Float.class)) + textWidth + 8,
							(int) (height*this.getButtonTimerConfigs("posY", Float.class)) + 15,
							0x63000000 // ballin it, worked ig
					);
				}

				drawContext.drawText(
						mc.textRenderer, text,
						(int) (width*this.getButtonTimerConfigs("posX", Float.class)) + 4,
						(int) (height*this.getButtonTimerConfigs("posY", Float.class)) + 4,
						Colors.WHITE,
						this.getButtonTimerTextShadow()
				);
			}
		}));
	}

	private void initializeCommands() {
		ClientCommandRegistrationCallback.EVENT.register(this::commandModMenu);
//		ClientCommandRegistrationCallback.EVENT.register(this::commandBroadcast);
	}

	private void onClientTick(MinecraftClient var) {
		if (this.saveFile.lastModified() > this.lastSaved) {
			this.loadSettings();
			this.lastSaved = this.saveFile.lastModified();
		}

		if (this.displayScreen) {
			this.displayScreen = false;
			mc.setScreen(new ModMenuScreen(this));
		}

		if (this.isInDPTB2) {
			if (ButtonTimerManager.buttonTimer >= 0) {
				ButtonTimerManager.buttonTimer += 1;
			}
		}
	}

	private void commandModMenu(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> c = dispatcher.register(
				ClientCommandManager.literal("dptb2")
						.executes(context -> {
							this.displayScreen = true; // necessary to open the config screen 1 tick late, stupid shit idk why
							return 1;
						})
		);
	}

//	private void commandBroadcast(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
//		LiteralCommandNode<FabricClientCommandSource> c = dispatcher.register(
//				ClientCommandManager.literal("broadcast")
//						.then(ClientCommandManager.argument("message", StringArgumentType.greedyString()))
//						.executes(context -> {
//							String msg = StringArgumentType.getString(context, "message");
//								websocketClient.sendModMessage(String.format("%s broadcast", mc.player != null ? mc.player.getGameProfile().getName() : "Unknown"), msg);
//							return 1;
//						})
//		);
//	}

	public void refreshRamperStatus() {
		String host = this.getDPTBotHost();
		int port = this.getDPTBotPort();
		if (this.isInDPTB2 && this.getDiscordRamper()) {
			LOGGER.info("Attempting Websocket connection to ws://{}:{}", host, port);
			websocketClient = new DiscordWebSocketClient(String.format("ws://%s:%s", host, port));
			websocketClient.connect();
		} else {
			this.isRamper = false;
			if (websocketClient != null && websocketClient.isOpen()) {
				LOGGER.info("Closing Websocket connection to ws://{}:{}", host, port);
				websocketClient.close();
			}
		}
	}

	public void saveSettings() {
		try (FileWriter fw = new FileWriter(this.saveFile)) {
			GSON.toJson(this.config, fw);
			LOGGER.info("Settings saved!");
		} catch (IOException e) {
			LOGGER.error("Failed to save settings!", e);
			throw new RuntimeException(e);
		}
	}

	public void loadSettings() {
		try (FileReader fr = new FileReader(this.saveFile)) {
			this.config = GSON.fromJson(fr, ModConfigs.class);
			LOGGER.info("Settings loaded!");
		} catch (IOException e) {
			LOGGER.error("Failed to load settings!", e);
			throw new RuntimeException(e);
		}
	}

	public <T> T getConfig(Map<String, JsonElement> map, Map<String, JsonElement> defaultMap, String key, Class<T> clazz) {
		if (!map.containsKey(key)) {
			map.put(key, defaultMap.get(key));
		}
		return GSON.fromJson(map.get(key), clazz);
	}
	public <T> T setConfig(Map<String, JsonElement> map, String key, T value, Class<T> clazz) {
		JsonElement jsonValue = GSON.toJsonTree(value, clazz);
		JsonElement oldValue = map.put(key, jsonValue);
		return GSON.fromJson(oldValue, clazz);
	}

	public <T> T getNotifs(String key, Class<T> clazz) {
		return this.getConfig(this.config.notifsMap, ModConfigs.notifsDefaultMap, key, clazz);
	}
	public <T> T getButtonTimerConfigs(String key, Class<T> clazz) {
		return this.getConfig(this.config.buttonTimerMap, ModConfigs.buttonTimerDefaultMap, key, clazz);
	}
	public boolean getAutoCheer() {
		return this.getConfig(this.config.othersMap, ModConfigs.othersDefaultMap, "autoCheer", Boolean.class);
	}
	public boolean getDiscordRamper() {
		return this.getConfig(this.config.othersMap, ModConfigs.othersDefaultMap, "discordRamper", Boolean.class);
	}
	public String getDPTBotHost() {
		return this.getConfig(this.config.othersMap, ModConfigs.othersDefaultMap, "dptbotHost", String.class);
	}
	public int getDPTBotPort() {
		return this.getConfig(this.config.othersMap, ModConfigs.othersDefaultMap, "dptbotPort", Integer.class);
	}

	public boolean getBoolNotifs(String key) {
		return this.getNotifs(key, Boolean.class);
	}
	public boolean getButtonTimerEnabled() {
		return this.getButtonTimerConfigs("enabled", Boolean.class);
	}
	public boolean getButtonTimerTextShadow() {
		return this.getButtonTimerConfigs("textShadow", Boolean.class);
	}
	public boolean getButtonTimerRenderBG() {
		return this.getButtonTimerConfigs("renderBackground", Boolean.class);
	}

	public boolean setAutoCheer(boolean value) {
		return this.setConfig(this.config.othersMap, "autoCheer", value, Boolean.class);
	}
	public boolean setDiscordRamper(boolean value) {
		return this.setConfig(this.config.othersMap, "discordRamper", value, Boolean.class);
	}
	public String setDPTBotHost(String value) {
		return this.setConfig(this.config.othersMap, "dptbotHost", value, String.class);
	}
	public int setDPTBotPort(int value) {
		return this.setConfig(this.config.othersMap, "dptbotPort", value, Integer.class);
	}
	public <T> T setNotifs(String key, T value, Class<T> clazz) {
		return this.setConfig(this.config.notifsMap, key, value, clazz);
	}
	public <T> T setButtonTimerConfigs(String key, T value, Class<T> clazz) {
		return this.setConfig(this.config.buttonTimerMap, key, value, clazz);
	}
	public boolean setBoolNotifs(String key, boolean value) {
		return this.setNotifs(key, value, Boolean.class);
	}
	public boolean setButtonTimerEnabled(boolean value) {
		return this.setButtonTimerConfigs("enabled", value, Boolean.class);
	}
	public boolean setButtonTimerTextShadow(boolean value) {
		return this.setButtonTimerConfigs("textShadow", value, Boolean.class);
	}
	public boolean setButtonTimerRenderBG(boolean value) {
		return this.setButtonTimerConfigs("renderBackground", value, Boolean.class);
	}
}