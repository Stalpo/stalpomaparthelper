package net.stalpo.stalpomaparthelper;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

import java.io.File;

import static net.stalpo.stalpomaparthelper.ClearDownloadedMapsCommand.registerClearDownloadedMaps;
import static net.stalpo.stalpomaparthelper.ClearDownloadedSMIsCommand.registerClearDownloadedSMIs;
import static net.stalpo.stalpomaparthelper.NameMapCommand.registerNameMap;
import static net.stalpo.stalpomaparthelper.SetMaxWrongPixelsCommand.registerSetMaxWrongPixels;

public class StalpoMapartHelper implements ClientModInitializer {
	public static final String MOD_ID = "stalpomaparthelper";
	private static final String category = "Stalpo Mapart Helper";
	private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static KeyBinding keyDownloadMaps;
	public static KeyBinding keyFindDuplicates;
	public static KeyBinding keyFindNotLocked;
	public static KeyBinding keyToggleMapCopier;
	public static boolean mapCopierToggled;
	public static KeyBinding keyToggleSMIDownloadMode;
	public static boolean SMIDownloadModeToggled;
	public static KeyBinding keyToggleSMINamerMode;
	public static boolean SMINamerModeToggled;
	public static KeyBinding keyToggleMapLocker;
	public static boolean mapLockerToggled;
	public static KeyBinding keyGetSMI;
	public static KeyBinding keyToggleMapNamer;
	public static boolean mapNamerToggled;

	public static File modFolder;

	@Override
	public void onInitializeClient() {
		LOG(MOD_ID+": Hello World!");

		keyDownloadMaps = registerKey("Download Maps", GLFW.GLFW_KEY_KP_1);
		keyFindDuplicates = registerKey("Find Duplicates", GLFW.GLFW_KEY_KP_2);
		keyFindNotLocked = registerKey("Find Not Locked", GLFW.GLFW_KEY_KP_3);
		keyGetSMI = registerKey("Get SMI", GLFW.GLFW_KEY_KP_4);
		keyToggleMapCopier = registerKey("Toggle Map Copier", GLFW.GLFW_KEY_KP_5);
		keyToggleMapLocker = registerKey("Toggle Map Locker", GLFW.GLFW_KEY_KP_6);
		keyToggleMapNamer = registerKey("Toggle Map Namer", GLFW.GLFW_KEY_KP_7);
		keyToggleSMIDownloadMode = registerKey("Toggle SMI Download Mode", GLFW.GLFW_KEY_KP_8);
		keyToggleSMINamerMode = registerKey("Toggle SMI Namer Mode", GLFW.GLFW_KEY_KP_9);

		MapartShulker.setNextMap();
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> registerNameMap(dispatcher));
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> registerClearDownloadedMaps(dispatcher));
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> registerClearDownloadedSMIs(dispatcher));
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> registerSetMaxWrongPixels(dispatcher));

		modFolder = new File(MinecraftClient.getInstance().runDirectory, "stalpomaparthelper");
		if(!modFolder.exists() && !modFolder.mkdir()) {
			StalpoMapartHelper.ERROR("Could not create directory " + modFolder.getAbsolutePath() + " cannot continue!");
			return;
		}

		for(String s : new String[]{"maparts", "maparts_dump", "maparts_smi"}){
			File screensDir = new File(modFolder, s);
			if(!screensDir.exists() && !screensDir.mkdir()) {
				StalpoMapartHelper.ERROR("Could not create directory " + screensDir.getAbsolutePath() + " cannot continue!");
				return;
			}
		}

		Util.getIoWorkerExecutor().execute(ImageHelper::initializeImages);
		Util.getIoWorkerExecutor().execute(ImageHelper::initializeImagesSMI);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (keyToggleMapCopier.wasPressed()) {
				mapCopierToggled = !mapCopierToggled;
				if(mapCopierToggled){
					LOGCHAT("Map copier enabled!");
					disableOtherToggles(1);
				}else{
					LOGCHAT("Map copier disabled!");
				}
			}
			while (keyToggleSMIDownloadMode.wasPressed()) {
				SMIDownloadModeToggled = !SMIDownloadModeToggled;
				if(SMIDownloadModeToggled){
					LOGCHAT("SMI download mode enabled!");
					CHAT("INFO: This mode is just for Stalpo");
				}else{
					LOGCHAT("SMI download mode disabled!");
				}
			}
			while (keyToggleMapLocker.wasPressed()) {
				mapLockerToggled = !mapLockerToggled;
				if(mapLockerToggled){
					LOGCHAT("Map locker enabled!");
					disableOtherToggles(2);
				}else{
					LOGCHAT("Map locker disabled!");
				}
			}
			while (keyToggleMapNamer.wasPressed()) {
				mapNamerToggled = !mapNamerToggled;
				if(mapNamerToggled){
					LOGCHAT("Map namer enabled!");
					disableOtherToggles(3);
				}else{
					LOGCHAT("Map namer disabled!");
				}
			}
			while (keyToggleSMINamerMode.wasPressed()) {
				SMINamerModeToggled = !SMINamerModeToggled;
				if(SMINamerModeToggled){
					LOGCHAT("SMI map namer mode enabled!");
				}else{
					LOGCHAT("SMI map namer mode disabled!");
				}
			}
		});
	}

	private void disableOtherToggles(int t){
		switch(t){
			case 1:
				if(mapLockerToggled){
					mapLockerToggled = false;
					LOGCHAT("Map locker disabled!");
				}
				if(mapNamerToggled){
					mapNamerToggled = false;
					LOGCHAT("Map namer disabled!");
				}
				break;
			case 2:
				if(mapCopierToggled){
					mapCopierToggled = false;
					LOGCHAT("Map copier disabled!");
				}
				if(mapNamerToggled){
					mapNamerToggled = false;
					LOGCHAT("Map namer disabled!");
				}
				break;
			case 3:
				if(mapCopierToggled){
					mapCopierToggled = false;
					LOGCHAT("Map copier disabled!");
				}
				if(mapLockerToggled){
					mapLockerToggled = false;
					LOGCHAT("Map locker disabled!");
				}
				break;
		}
	}

	private KeyBinding registerKey(String key, int code) {
		KeyBinding result = new KeyBinding(key, code, category);
		KeyBindingHelper.registerKeyBinding(result);
		return result;
	}

	public static void LOG(String s){
		LOGGER.info(s);
	}

	public static void ERROR(String s){
		LOGGER.error(s);
	}

	public static void CHAT(String s){
		assert MinecraftClient.getInstance().player != null;
		MinecraftClient.getInstance().player.sendMessage(Text.literal(s), false);
	}

	public static void LOGCHAT(String s){
		LOGGER.info(s);
		assert MinecraftClient.getInstance().player != null;
		MinecraftClient.getInstance().player.sendMessage(Text.literal(s), false);
	}
}