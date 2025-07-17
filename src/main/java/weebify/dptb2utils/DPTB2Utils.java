package weebify.dptb2utils;

import com.google.gson.Gson;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.scoreboard.*;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weebify.dptb2utils.gui.NotificationToast;
import weebify.dptb2utils.gui.screen.ModMenuScreen;
import weebify.dptb2utils.utils.DelayedTask;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class DPTB2Utils implements ClientModInitializer {
	public static final String MOD_ID = "dptb2-utils";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public ModConfigs config;
	private File saveFile;
	private long lastSaved;

	private boolean displayScreen = false;
	public boolean isInDPTB2 = false;
	public int buttonTimer = -1;
	public List<DelayedTask> scheduledTasks = new ArrayList<>();

	private static final MinecraftClient mc = MinecraftClient.getInstance();
	public static final Gson GSON = new Gson();
	private static DPTB2Utils instance;

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

	private void initializeEvents() {
		ClientTickEvents.START_CLIENT_TICK.register(this::onClientTick);
		ClientTickEvents.END_CLIENT_TICK.register((var) -> {
			scheduledTasks.removeIf(DelayedTask::tick);
		});
		// detecting whether the player is in DPTB2
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			ServerInfo serverEntry = client.getCurrentServerEntry();
			if (serverEntry == null) {
				this.isInDPTB2 = false;
				return;
			}
			if (!serverEntry.address.contains("hypixel.net")) {
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
					LOGGER.info("Scoreboard title: {}", title);
					LOGGER.info("Scoreboard content: {}", content);

					this.isInDPTB2 = title.contains("housing") && content.contains("don't press the button 2");
					LOGGER.info("isInDPTB2: {}", this.isInDPTB2);
					if (this.isInDPTB2) {
						client.getToastManager().add(new NotificationToast("DPTB2 Utils", "You are in Don't Press The Button 2!", 0xD2FFC8, SoundEvents.ENTITY_PLAYER_LEVELUP	));
					}
				}
			});

		});
	}

	private void initializeCommands() {
		ClientCommandRegistrationCallback.EVENT.register(this::commandModMenu);
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

	public boolean getAutoCheer() {
		if (!this.config.othersMap.containsKey("autoCheer")) {
			this.config.othersMap.put("autoCheer", ModConfigs.othersDefaultMap.get("autoCheer"));
		}
		return this.config.othersMap.get("autoCheer").getAsBoolean();
	}
	public boolean getNotifs(String key) {
		if (!this.config.notifsMap.containsKey(key)) {
			this.config.notifsMap.put(key, ModConfigs.notifsDefaultMap.get(key));
		}
		return this.config.notifsMap.get(key).getAsBoolean();
	}

	public boolean setAutoCheer(boolean value) {
		return Objects.requireNonNull(this.config.othersMap.put("autoCheer", GSON.toJsonTree(value))).getAsBoolean();
	}
	public boolean setNotifs(String key, boolean value) {
		return Objects.requireNonNull(this.config.notifsMap.put(key, GSON.toJsonTree(value))).getAsBoolean();
	}

}