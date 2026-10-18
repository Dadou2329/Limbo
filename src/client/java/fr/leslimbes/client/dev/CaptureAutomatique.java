package fr.leslimbes.client.dev;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.client.screen.JournalScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ScreenshotRecorder;

/**
 * Outil de développement : captures d'écran automatiques (activé par {@code -Dles_limbes.capture=true},
 * tâche Gradle {@code runCapture}). Permet de vérifier le rendu (ciel, structures, interface) sans
 * affichage interactif, par exemple sous Xvfb. Le client se ferme à la fin du scénario.
 */
public final class CaptureAutomatique {
	private static final boolean ACTIF = Boolean.getBoolean("les_limbes.capture");
	private static int ticks;

	public static void init() {
		if (!ACTIF) {
			return;
		}
		LesLimbes.LOGGER.info("CAPTURE : scénario de captures automatiques activé.");
		ClientTickEvents.END_CLIENT_TICK.register(CaptureAutomatique::tick);
	}

	private static void tick(MinecraftClient client) {
		if (client.world == null || client.player == null) {
			return;
		}
		ticks++;
		switch (ticks) {
			case 40 -> commande(client, "limbes pouvoir tout");
			case 60 -> commande(client, "limbes tp sanctuaire");
			case 80 -> commande(client, "time set day");
			case 260 -> regarder(client, 180.0F, 2.0F);
			case 300 -> capture(client, "01_sanctuaire_arche");
			case 320 -> regarder(client, 0.0F, 6.0F);
			case 360 -> capture(client, "02_sanctuaire_pont");
			case 380 -> regarder(client, 135.0F, -12.0F);
			case 420 -> capture(client, "03_ciel_palais");
			case 440 -> client.setScreen(new JournalScreen());
			case 470 -> capture(client, "04_journal");
			case 480 -> client.setScreen(null);
			case 500 -> commande(client, "limbes tp clairval");
			case 640 -> regarder(client, 160.0F, 4.0F);
			case 680 -> capture(client, "05_clairval_place");
			case 700 -> regarder(client, -20.0F, 2.0F);
			case 740 -> capture(client, "06_clairval_sud");
			case 760 -> commande(client, "limbes tp manoir");
			case 900 -> regarder(client, 180.0F, 0.0F);
			case 940 -> capture(client, "07_manoir");
			case 960 -> commande(client, "limbes tp relais");
			case 1100 -> regarder(client, 90.0F, 0.0F);
			case 1140 -> capture(client, "08_relais");
			case 1160 -> commande(client, "time set 13500");
			case 1180 -> commande(client, "limbes tp sanctuaire");
			case 1320 -> regarder(client, 135.0F, -15.0F);
			case 1360 -> capture(client, "09_nuit_constellations");
			case 1400 -> {
				LesLimbes.LOGGER.info("CAPTURE : scénario terminé.");
				client.scheduleStop();
			}
			default -> {
			}
		}
	}

	private static void commande(MinecraftClient client, String commande) {
		if (client.player != null) {
			client.player.networkHandler.sendChatCommand(commande);
		}
	}

	private static void regarder(MinecraftClient client, float yaw, float pitch) {
		if (client.player != null) {
			client.player.setYaw(yaw);
			client.player.setPitch(pitch);
		}
	}

	private static void capture(MinecraftClient client, String nom) {
		ScreenshotRecorder.saveScreenshot(client.runDirectory, nom + ".png", client.getFramebuffer(), t -> LesLimbes.LOGGER.info("CAPTURE : {} -> {}", nom, t.getString()));
	}

	private CaptureAutomatique() {
	}
}
