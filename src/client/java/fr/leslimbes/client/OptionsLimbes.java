package fr.leslimbes.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.leslimbes.LesLimbes;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Options client des Limbes (config/les_limbes.json) : densité des effets, secousses de caméra, flashes,
 * marqueurs dans le monde, indications discrètes, indices de tutoriel déjà vus. Purement cosmétique :
 * rien ici n'influence le serveur.
 */
public final class OptionsLimbes {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static OptionsLimbes instance;

	public int densiteEffets = 100;
	/** Profil graphique : 0 bas (particules réduites, pas de secousses), 1 normal, 2 élevé (particules doublées). */
	public int profil = 1;
	public boolean secousses = true;
	public boolean flashes = true;
	public boolean marqueurs = true;
	public boolean discret = false;
	public Set<String> indicesVus = new LinkedHashSet<>();

	public static OptionsLimbes get() {
		if (instance == null) {
			instance = charger();
		}
		return instance;
	}

	private static Path fichier() {
		return FabricLoader.getInstance().getConfigDir().resolve("les_limbes.json");
	}

	private static OptionsLimbes charger() {
		Path p = fichier();
		if (Files.exists(p)) {
			try {
				OptionsLimbes o = GSON.fromJson(Files.readString(p), OptionsLimbes.class);
				if (o != null) {
					if (o.indicesVus == null) {
						o.indicesVus = new LinkedHashSet<>();
					}
					o.densiteEffets = Math.max(25, Math.min(100, o.densiteEffets));
					o.profil = Math.max(0, Math.min(2, o.profil));
					return o;
				}
			} catch (IOException | RuntimeException e) {
				LesLimbes.LOGGER.warn("Options des Limbes illisibles, valeurs par défaut : {}", e.toString());
			}
		}
		return new OptionsLimbes();
	}

	public void sauvegarder() {
		try {
			Files.createDirectories(fichier().getParent());
			Files.writeString(fichier(), GSON.toJson(this));
		} catch (IOException e) {
			LesLimbes.LOGGER.warn("Impossible d'enregistrer les options des Limbes : {}", e.toString());
		}
	}

	/** Densité suivante dans le cycle 25 → 50 → 75 → 100. */
	public void cyclerDensite() {
		densiteEffets = densiteEffets >= 100 ? 25 : densiteEffets + 25;
		sauvegarder();
	}

	/** Nombre de particules à émettre pour une quantité de référence. */
	public int quantite(int base) {
		int facteur = profil == 0 ? 50 : profil == 2 ? 200 : 100;
		return Math.max(1, base * densiteEffets * facteur / 10000);
	}

	public void cyclerProfil() {
		profil = (profil + 1) % 3;
		if (profil == 0) {
			secousses = false;
		} else if (profil == 1) {
			secousses = true;
		}
		sauvegarder();
	}

	public String nomProfil() {
		return net.minecraft.text.Text.translatable("ecran.les_limbes.options.profil." + (profil == 0 ? "bas" : profil == 2 ? "eleve" : "normal")).getString();
	}

	public boolean indiceVu(String id) {
		return indicesVus.contains(id);
	}

	public void marquerIndiceVu(String id) {
		if (indicesVus.add(id)) {
			sauvegarder();
		}
	}

	public static String ouiNon(boolean b) {
		return net.minecraft.text.Text.translatable(b ? "ecran.les_limbes.options.oui" : "ecran.les_limbes.options.non").getString();
	}
}
