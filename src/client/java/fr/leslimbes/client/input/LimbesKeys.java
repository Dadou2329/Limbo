package fr.leslimbes.client.input;

import fr.leslimbes.client.screen.JournalScreen;
import fr.leslimbes.client.screen.RadialScreen;
import fr.leslimbes.network.LimbesPayloads;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/** Touches reconfigurables (catégorie « Les Limbes ») : quatre emplacements de pouvoir, la sélection radiale et le journal. */
public final class LimbesKeys {
	public static final String CATEGORIE = "key.categories.les_limbes";
	public static final KeyBinding POUVOIR_1 = register("pouvoir_1", GLFW.GLFW_KEY_R);
	public static final KeyBinding POUVOIR_2 = register("pouvoir_2", GLFW.GLFW_KEY_V);
	public static final KeyBinding POUVOIR_3 = register("pouvoir_3", GLFW.GLFW_KEY_G);
	public static final KeyBinding POUVOIR_4 = register("pouvoir_4", GLFW.GLFW_KEY_B);
	public static final KeyBinding RADIAL = register("radial", GLFW.GLFW_KEY_X);
	public static final KeyBinding JOURNAL = register("journal", GLFW.GLFW_KEY_J);
	public static final KeyBinding[] POUVOIRS = {POUVOIR_1, POUVOIR_2, POUVOIR_3, POUVOIR_4};

	private static KeyBinding register(String nom, int touche) {
		return KeyBindingHelper.registerKeyBinding(new KeyBinding("key.les_limbes." + nom, InputUtil.Type.KEYSYM, touche, CATEGORIE));
	}

	public static void init() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) {
				return;
			}
			for (int i = 0; i < POUVOIRS.length; i++) {
				while (POUVOIRS[i].wasPressed()) {
					ClientPlayNetworking.send(new LimbesPayloads.UtiliserPouvoir(i));
				}
			}
			while (RADIAL.wasPressed()) {
				if (client.currentScreen == null) {
					client.setScreen(new RadialScreen());
				} else if (client.currentScreen instanceof RadialScreen) {
					client.setScreen(null);
				}
			}
			while (JOURNAL.wasPressed()) {
				if (client.currentScreen == null) {
					client.setScreen(new JournalScreen());
				}
			}
		});
	}

	/** Nom court de la touche liée à un emplacement (pour l'interface). */
	public static String nomTouche(int slot) {
		return POUVOIRS[slot].getBoundKeyLocalizedText().getString();
	}

	private LimbesKeys() {
	}
}
