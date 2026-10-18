package fr.leslimbes.client.dev;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.client.screen.DialogueScreen;
import fr.leslimbes.client.screen.JournalScreen;
import fr.leslimbes.npc.HabitantEntity;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.util.Hand;

import java.util.Comparator;
import java.util.Locale;

/**
 * Outil de développement : captures d'écran automatiques (activé par {@code -Dles_limbes.capture=true},
 * tâche Gradle {@code runCapture}). Permet de vérifier le rendu (ciel, structures, interface, dialogue)
 * sans affichage interactif, par exemple sous Xvfb. Le client se ferme à la fin du scénario.
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
			case 30 -> commande(client, "gamerule sendCommandFeedback false");
			case 40 -> commande(client, "limbes pouvoir tout");
			// Le scénario traverse le relais des Veilleurs : le joueur est protégé, pas invulnérable.
			case 50 -> commande(client, "effect give @s minecraft:resistance 100000 4 true");
			case 55 -> commande(client, "effect give @s minecraft:regeneration 100000 2 true");
			case 57 -> commande(client, "effect give @s minecraft:slow_falling 100000 0 true");
			case 60 -> commande(client, "limbes tp sanctuaire");
			case 80 -> commande(client, "time set day");
			case 260 -> regarder(client, 180.0F, 2.0F);
			case 300 -> capture(client, "01_sanctuaire_arche");
			case 320 -> regarder(client, 0.0F, 6.0F);
			case 360 -> capture(client, "02_sanctuaire_pont");
			// Traversée réelle de l'arche de retour : marche vers le nord jusqu'au voile, le serveur
			// téléporte dans l'Overworld (point de retour ou apparition), puis retour au Sanctuaire.
			case 380 -> {
				regarder(client, 180.0F, 0.0F);
				avancer(client, true);
			}
			case 440 -> avancer(client, false);
			case 560 -> capture(client, "03_retour_overworld");
			case 580 -> commande(client, "limbes tp sanctuaire");
			case 700 -> rejoindrePnj(client);
			case 730 -> parlerAuPnj(client);
			case 770 -> capture(client, "04_dialogue");
			case 790 -> fermerDialogue(client);
			// Vue dégagée vers le nord-ouest depuis au-dessus du pied du pont (chute lente active).
			case 810 -> commande(client, "tp @s 0.5 96 56.5");
			case 830 -> regarder(client, 135.0F, -8.0F);
			case 850 -> capture(client, "05_ciel_palais");
			case 870 -> client.setScreen(new JournalScreen());
			case 900 -> capture(client, "06_journal_quetes");
			case 910 -> client.setScreen(JournalScreen.pouvoirs());
			case 940 -> capture(client, "07_journal_pouvoirs");
			case 950 -> client.setScreen(null);
			case 970 -> commande(client, "limbes tp clairval");
			case 1110 -> regarder(client, 160.0F, 4.0F);
			case 1150 -> capture(client, "08_clairval_place");
			case 1170 -> regarder(client, -20.0F, 2.0F);
			case 1210 -> capture(client, "09_clairval_fontaine");
			case 1230 -> commande(client, "tp @s -120 67 174");
			case 1370 -> regarder(client, 180.0F, -2.0F);
			case 1410 -> capture(client, "10_manoir");
			case 1430 -> commande(client, "tp @s 150 66 82");
			case 1570 -> regarder(client, 180.0F, 0.0F);
			case 1610 -> capture(client, "11_relais");
			case 1630 -> commande(client, "time set 18000");
			case 1650 -> commande(client, "limbes tp clairval");
			case 1790 -> regarder(client, 135.0F, -30.0F);
			case 1830 -> capture(client, "12_nuit_constellations");
			// Étape 3 : raie céleste apprivoisée, moulin qui tourne, observatoire, lac au crépuscule, maison égarée.
			case 1850 -> commande(client, "time set day");
			case 1860 -> commande(client, "tp @s -20 67 130");
			case 1900 -> commande(client, "summon les_limbes:raie_celeste -24 69 126 {Tame:1b}");
			case 1980 -> regarder(client, 50.0F, -8.0F);
			case 2020 -> capture(client, "13_raie_celeste");
			case 2040 -> commande(client, "limbes quete demarrer le_moulin_immobilise");
			case 2050 -> commande(client, "limbes quete etape le_moulin_immobilise 3");
			case 2080 -> commande(client, "tp @s -28 67 136");
			case 2160 -> regarder(client, 180.0F, -25.0F);
			case 2200 -> capture(client, "14_moulin_tourne");
			case 2220 -> commande(client, "tp @s 30 67 124");
			case 2300 -> regarder(client, 180.0F, -20.0F);
			case 2340 -> capture(client, "15_observatoire");
			case 2360 -> commande(client, "tp @s -84 67 22");
			case 2380 -> commande(client, "time set 12800");
			case 2500 -> regarder(client, -90.0F, 8.0F);
			case 2540 -> capture(client, "16_lac_crepuscule");
			case 2560 -> commande(client, "time set day");
			case 2570 -> commande(client, "limbes arrivee declencher maison_egaree");
			case 2580 -> commande(client, "tp @s 70 67 226");
			case 2700 -> regarder(client, 180.0F, -4.0F);
			case 2740 -> capture(client, "17_maison_egaree");
			case 2780 -> {
				LesLimbes.LOGGER.info("CAPTURE : scénario terminé.");
				client.scheduleStop();
			}
			default -> {
			}
		}
	}

	private static void avancer(MinecraftClient client, boolean actif) {
		client.options.forwardKey.setPressed(actif);
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

	private static HabitantEntity pnjLePlusProche(MinecraftClient client, double rayon) {
		return client.world.getEntitiesByClass(HabitantEntity.class, client.player.getBoundingBox().expand(rayon), e -> true).stream()
				.min(Comparator.comparingDouble(e -> e.squaredDistanceTo(client.player)))
				.orElse(null);
	}

	/** Se place à deux blocs du personnage le plus proche et se tourne vers lui. */
	private static void rejoindrePnj(MinecraftClient client) {
		HabitantEntity pnj = pnjLePlusProche(client, 24.0);
		if (pnj == null) {
			LesLimbes.LOGGER.warn("CAPTURE : aucun personnage à proximité pour le dialogue.");
			return;
		}
		double dx = client.player.getX() - pnj.getX();
		double dz = client.player.getZ() - pnj.getZ();
		double d = Math.max(1.0E-3, Math.sqrt(dx * dx + dz * dz));
		double x = pnj.getX() + dx / d * 2.0;
		double z = pnj.getZ() + dz / d * 2.0;
		commande(client, String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f", x, pnj.getY(), z));
		regarder(client, (float) Math.toDegrees(Math.atan2(dx, -dz)), 4.0F);
	}

	private static void parlerAuPnj(MinecraftClient client) {
		HabitantEntity pnj = pnjLePlusProche(client, 4.0);
		if (pnj == null || client.interactionManager == null) {
			LesLimbes.LOGGER.warn("CAPTURE : personnage hors de portée pour le dialogue.");
			return;
		}
		double dx = pnj.getX() - client.player.getX();
		double dz = pnj.getZ() - client.player.getZ();
		regarder(client, (float) Math.toDegrees(Math.atan2(-dx, dz)), 4.0F);
		client.interactionManager.interactEntity(client.player, pnj, Hand.MAIN_HAND);
	}

	private static void fermerDialogue(MinecraftClient client) {
		if (client.currentScreen instanceof DialogueScreen dialogue) {
			dialogue.close();
		}
	}

	private static void capture(MinecraftClient client, String nom) {
		ScreenshotRecorder.saveScreenshot(client.runDirectory, nom + ".png", client.getFramebuffer(), t -> LesLimbes.LOGGER.info("CAPTURE : {} -> {}", nom, t.getString()));
	}

	private CaptureAutomatique() {
	}
}
