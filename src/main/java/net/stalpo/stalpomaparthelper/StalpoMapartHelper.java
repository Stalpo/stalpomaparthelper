package net.stalpo.stalpomaparthelper;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

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
	public static KeyBinding keyToggleMapLocker;
	public static boolean mapLockerToggled;
	public static KeyBinding keyGetSMI;

	@Override
	public void onInitializeClient() {
		LOG(MOD_ID+": Hello World!");

		keyDownloadMaps = registerKey("Download Maps", GLFW.GLFW_KEY_KP_1);
		keyFindDuplicates = registerKey("Find Duplicates", GLFW.GLFW_KEY_KP_2);
		keyFindNotLocked = registerKey("Find Not Locked", GLFW.GLFW_KEY_KP_3);
		keyGetSMI = registerKey("Get SMI", GLFW.GLFW_KEY_KP_4);
		keyToggleMapCopier = registerKey("Toggle Map Copier", GLFW.GLFW_KEY_KP_5);
		keyToggleMapLocker = registerKey("Toggle Map Locker", GLFW.GLFW_KEY_KP_6);
		keyToggleSMIDownloadMode = registerKey("Toggle SMI Download Mode", GLFW.GLFW_KEY_KP_7);

		MapartShulker.setNextMap();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (keyToggleMapCopier.wasPressed()) {
				mapCopierToggled = !mapCopierToggled;
				if(mapCopierToggled){
					LOGCHAT("Map copier enabled!");
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
				}else{
					LOGCHAT("Map locker disabled!");
				}
			}
		});
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
		MinecraftClient.getInstance().player.sendMessage(Text.literal(s), false);
	}

	public static void LOGCHAT(String s){
		LOGGER.info(s);
		MinecraftClient.getInstance().player.sendMessage(Text.literal(s), false);
	}
}