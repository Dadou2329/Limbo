package fr.leslimbes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Réglages du serveur ({@code config/les_limbes.json}) : budgets explicites qui bornent ce que le mod ajoute au monde.
 * Le fichier est créé avec les valeurs par défaut au premier lancement ; toute valeur est bornée à la lecture.
 */
public final class LimbesConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static LimbesConfig instance;

	/** Arrivées posées en même temps dans le monde (1 à 6). */
	public int arriveesSimultanees = 2;
	/** Gardes astrales par souverain (1 à 6). */
	public int gardesParJoueur = 3;
	/** Durée d'une garde astrale en ticks (600 à 6000). */
	public int dureeGardeTicks = 1200;
	/** Éclats (projectiles des tireurs et des archers) présents au plus dans un monde (16 à 256). */
	public int eclatsMaxParMonde = 48;
	/** Rayon (blocs) autour des joueurs dans lequel les personnages essentiels sont surveillés et réapparaissent (64 à 256). */
	public int rayonSurveillancePnj = 128;

	public static LimbesConfig get() {
		if (instance == null) {
			instance = charger();
		}
		return instance;
	}

	private static LimbesConfig charger() {
		Path chemin = FabricLoader.getInstance().getConfigDir().resolve("les_limbes.json");
		LimbesConfig c = new LimbesConfig();
		try {
			if (Files.exists(chemin)) {
				LimbesConfig lu = GSON.fromJson(Files.readString(chemin, StandardCharsets.UTF_8), LimbesConfig.class);
				if (lu != null) {
					c = lu;
				}
			}
			c.borner();
			Files.createDirectories(chemin.getParent());
			Files.writeString(chemin, GSON.toJson(c), StandardCharsets.UTF_8);
		} catch (IOException | RuntimeException e) {
			LesLimbes.LOGGER.warn("Configuration les_limbes.json illisible, valeurs par défaut utilisées : {}", e.toString());
			c = new LimbesConfig();
		}
		LesLimbes.LOGGER.info("Budgets : {} Arrivées simultanées, {} gardes par joueur ({} ticks), {} éclats par monde, surveillance des personnages à {} blocs",
				c.arriveesSimultanees, c.gardesParJoueur, c.dureeGardeTicks, c.eclatsMaxParMonde, c.rayonSurveillancePnj);
		return c;
	}

	private void borner() {
		arriveesSimultanees = Math.max(1, Math.min(6, arriveesSimultanees));
		gardesParJoueur = Math.max(1, Math.min(6, gardesParJoueur));
		dureeGardeTicks = Math.max(600, Math.min(6000, dureeGardeTicks));
		eclatsMaxParMonde = Math.max(16, Math.min(256, eclatsMaxParMonde));
		rayonSurveillancePnj = Math.max(64, Math.min(256, rayonSurveillancePnj));
	}

	/** Un nouvel éclat peut-il être lancé dans ce monde sans dépasser le budget ? */
	public static boolean peutLancerEclat(net.minecraft.server.world.ServerWorld world) {
		int n = 0;
		int max = get().eclatsMaxParMonde;
		for (net.minecraft.entity.Entity e : world.iterateEntities()) {
			if (e instanceof fr.leslimbes.entity.region.EclatProjectileEntity && ++n >= max) {
				return false;
			}
		}
		return true;
	}

	private LimbesConfig() {
	}
}
