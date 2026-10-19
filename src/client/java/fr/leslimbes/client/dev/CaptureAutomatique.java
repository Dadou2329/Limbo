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
	/** Capture différée : le scénario se met en pause jusqu'à ce que le terrain soit rendu (au plus 600 ticks). */
	private static String captureEnAttente;
	private static int patience;

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
		if (captureEnAttente != null) {
			if (terrainPret(client) || --patience <= 0) {
				if (patience <= 0) {
					LesLimbes.LOGGER.warn("CAPTURE : terrain encore incomplet pour {}, capture quand même.", captureEnAttente);
				}
				prendre(client, captureEnAttente);
				captureEnAttente = null;
			}
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
			// Raie céleste sur fond de ciel, depuis le plateau du Sanctuaire (le bourg de Clairval la cacherait).
			case 1860 -> commande(client, "limbes tp sanctuaire");
			case 1900 -> commande(client, "summon les_limbes:raie_celeste 0.5 86 8 {NoAI:1b}");
			case 1980 -> regarder(client, 0.0F, -14.0F);
			case 2020 -> capture(client, "13_raie_celeste");
			case 2040 -> commande(client, "limbes quete demarrer le_moulin_immobilise");
			case 2050 -> commande(client, "limbes quete etape le_moulin_immobilise 3");
			case 2080 -> commande(client, "tp @s -28 67 136");
			case 2160 -> regarder(client, 180.0F, -25.0F);
			case 2200 -> capture(client, "14_moulin_tourne");
			case 2220 -> commande(client, "tp @s 30 67 124");
			case 2300 -> regarder(client, 180.0F, -20.0F);
			case 2340 -> capture(client, "15_observatoire");
			case 2350 -> commande(client, "limbes arrivee declencher maison_egaree prairies_1");
			case 2360 -> commande(client, "tp @s -84 67 22");
			case 2380 -> commande(client, "time set 12800");
			case 2500 -> regarder(client, -90.0F, 8.0F);
			case 2540 -> capture(client, "16_lac_crepuscule");
			case 2560 -> commande(client, "time set day");
			case 2580 -> commande(client, "tp @s 70 67 226");
			case 2700 -> regarder(client, 180.0F, -4.0F);
			case 2740 -> capture(client, "17_maison_egaree");
			// Étapes 5 à 8 : régions, capitale, boss, garde astrale, sélection radiale, Avènement, règne, Arrivée, décor.
			case 2780 -> commande(client, "tp @s 950 70 -720");
			case 2920 -> regarder(client, 180.0F, 6.0F);
			case 2960 -> capture(client, "18_port_nacre");
			case 2980 -> commande(client, "tp @s 1050 74 380");
			case 3120 -> regarder(client, 180.0F, -20.0F);
			case 3160 -> capture(client, "19_sylvecime");
			case 3180 -> commande(client, "tp @s 150 67 1280");
			case 3320 -> regarder(client, 180.0F, 2.0F);
			case 3360 -> capture(client, "20_meridiane");
			case 3380 -> commande(client, "tp @s -1150 97 500");
			case 3520 -> regarder(client, 180.0F, -8.0F);
			case 3560 -> capture(client, "21_verreole");
			case 3580 -> commande(client, "tp @s -610 83 -580");
			case 3720 -> regarder(client, 180.0F, -2.0F);
			case 3760 -> capture(client, "22_haute_garde");
			case 3780 -> commande(client, "tp @s -900 87 -880");
			case 3920 -> regarder(client, 180.0F, -6.0F);
			case 3960 -> capture(client, "23_capitale_palais");
			case 3980 -> commande(client, "tp @s 1150 73 316");
			case 4000 -> commande(client, "summon les_limbes:aelis_duel 1150 72 304");
			case 4120 -> regarder(client, 180.0F, 0.0F);
			case 4160 -> capture(client, "24_boss_aelis");
			case 4180 -> commande(client, "kill @e[type=les_limbes:aelis_duel]");
			case 4190 -> commande(client, "limbes tp clairval");
			case 4195 -> commande(client, "limbes arrivee declencher cite_brumeval");
			case 4330 -> pouvoir(client, "chevalier_astral");
			case 4340 -> pouvoir(client, "archer_astral");
			case 4350 -> pouvoir(client, "protecteur_astral");
			case 4420 -> regarder(client, 20.0F, 4.0F);
			case 4460 -> capture(client, "25_garde_astrale");
			// Pont de lumière : il ne se pose que sur le vide ou l'eau, donc au-dessus du Lac du Crépuscule.
			case 4475 -> commande(client, "tp @s -84 67 22");
			case 4500 -> regarder(client, -90.0F, 6.0F);
			case 4510 -> pouvoir(client, "decret_que_le_chemin_s_ouvre");
			// Un pas en arrière pour voir le pont s'avancer sur le lac.
			case 4518 -> commande(client, "tp @s -80 67 22");
			case 4525 -> regarder(client, -90.0F, 22.0F);
			case 4540 -> capture(client, "26_pont_de_lumiere");
			case 4560 -> client.setScreen(new fr.leslimbes.client.screen.RadialScreen());
			case 4600 -> capture(client, "27_selection_radiale");
			case 4610 -> client.setScreen(null);
			case 4620 -> commande(client, "limbes drapeau poser couronne");
			case 4625 -> commande(client, "limbes drapeau poser couronne_assemblee");
			case 4640 -> pouvoir(client, "avenement");
			case 4660 -> client.options.setPerspective(net.minecraft.client.option.Perspective.THIRD_PERSON_FRONT);
			case 4720 -> capture(client, "28_avenement");
			case 4740 -> client.options.setPerspective(net.minecraft.client.option.Perspective.FIRST_PERSON);
			case 4770 -> commande(client, "tp @s -1060 88 -676");
			case 4910 -> regarder(client, 180.0F, -4.0F);
			case 4950 -> capture(client, "29_brumeval");
			case 4970 -> commande(client, "tp @s 1030 66 -770");
			// La structure se pose une fois la position chargée (le joueur est arrivé).
			case 5040 -> commande(client, "limbes structure les_limbes:regne/navire_royal 1021 55 -820");
			case 5120 -> regarder(client, 180.0F, -10.0F);
			case 5160 -> capture(client, "30_navire_royal");
			case 5180 -> commande(client, "tp @s 1300 74 -980");
			case 5320 -> regarder(client, 180.0F, -14.0F);
			case 5360 -> capture(client, "31_ile_des_ruines");
			case 5370 -> commande(client, "limbes arrivee declencher bibliotheque_perdue couronne_1");
			case 5380 -> commande(client, "tp @s -900 87 -1362");
			case 5520 -> regarder(client, 180.0F, -6.0F);
			case 5560 -> capture(client, "32_lisiere_des_brumes");
			case 5590 -> commande(client, "tp @s -720 83 -747");
			case 5730 -> regarder(client, 180.0F, -8.0F);
			case 5770 -> capture(client, "33_bibliotheque_perdue");
			case 5780 -> commande(client, "limbes arrivee declencher radeau_des_naufrages mer_1");
			case 5790 -> commande(client, "limbes tp clairval");
			case 5930 -> commande(client, "setblock ~ ~ ~2 les_limbes:vase_de_nacre");
			case 5935 -> commande(client, "setblock ~1 ~ ~2 les_limbes:tapis_de_nacre");
			case 5940 -> commande(client, "setblock ~-1 ~2 ~2 les_limbes:guirlande_de_fete");
			case 5980 -> regarder(client, 0.0F, 20.0F);
			case 6020 -> capture(client, "34_decor");
			// Radeau des naufragés sur l'îlot de la Mer (Arrivée unique : déjà posée si la sauvegarde vient de l'auto-test).
			case 6050 -> commande(client, "tp @s 900 67 -639");
			case 6190 -> regarder(client, 180.0F, -6.0F);
			case 6230 -> capture(client, "35_radeau_des_naufrages");
			case 6250 -> client.setScreen(JournalScreen.options());
			case 6290 -> capture(client, "36_options");
			case 6300 -> client.setScreen(null);
			case 6340 -> {
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

	/** Utilise une technique par son identifiant (même paquet que la sélection radiale). */
	private static void pouvoir(MinecraftClient client, String id) {
		net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new fr.leslimbes.network.LimbesPayloads.UtiliserPouvoirId(id));
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

	/** Capture immédiate si le terrain est rendu, sinon différée (téléportation lointaine sous rendu logiciel). */
	private static void capture(MinecraftClient client, String nom) {
		if (terrainPret(client)) {
			prendre(client, nom);
		} else {
			captureEnAttente = nom;
			patience = 600;
		}
	}

	private static boolean terrainPret(MinecraftClient client) {
		return client.worldRenderer.isTerrainRenderComplete()
				&& client.world.getChunkManager().isChunkLoaded(client.player.getChunkPos().x, client.player.getChunkPos().z);
	}

	private static void prendre(MinecraftClient client, String nom) {
		ScreenshotRecorder.saveScreenshot(client.runDirectory, nom + ".png", client.getFramebuffer(), t -> LesLimbes.LOGGER.info("CAPTURE : {} -> {}", nom, t.getString()));
	}

	private CaptureAutomatique() {
	}
}
