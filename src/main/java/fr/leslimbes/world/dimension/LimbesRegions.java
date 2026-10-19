package fr.leslimbes.world.dimension;

import net.minecraft.util.math.MathHelper;

/**
 * Les six régions des Limbes et leur disposition dans le plan : chaque région est un disque (centre,
 * rayon) ; un point appartient à la région dont la distance normalisée {@code d / rayon} est la plus
 * petite (Voronoï pondéré), avec un mélange doux des reliefs à la frontière. Source unique pour la
 * source de biomes, la fonction de densité régionale, le remplissage de la Mer suspendue et les tests.
 *
 * <p>Les Prairies de l'Aube restent au centre : tout le contenu des étapes 1 à 4 (x −180..260, z −40..240)
 * est à moins de 270 blocs de leur centre, bien à l'intérieur de leur disque.</p>
 */
public final class LimbesRegions {
	public enum Region {
		PRAIRIES("prairies", 0, 110, 480),
		MER("mer", 950, -750, 560),
		JARDINS("jardins", 1050, 350, 520),
		DESERT("desert", 150, 1250, 520),
		MONTAGNES("montagnes", -1150, 500, 540),
		COURONNE("couronne", -900, -900, 560);

		public final String id;
		public final int cx;
		public final int cz;
		public final int rayon;

		Region(String id, int cx, int cz, int rayon) {
			this.id = id;
			this.cx = cx;
			this.cz = cz;
			this.rayon = rayon;
		}

		/** Distance normalisée au centre (0 au centre, 1 au bord nominal du disque). */
		public double normalisee(double x, double z) {
			double dx = x - cx;
			double dz = z - cz;
			return Math.sqrt(dx * dx + dz * dz) / rayon;
		}

		public static Region parId(String id) {
			for (Region r : values()) {
				if (r.id.equals(id)) {
					return r;
				}
			}
			return null;
		}
	}

	/** Résultat d'une évaluation : région principale, sa distance normalisée, voisine et poids du mélange. */
	public record Point(Region region, double n, Region voisine, double nVoisine, double poids) {
	}

	/** Largeur (en distance normalisée) de la zone de transition entre deux régions. */
	public static final double TRANSITION = 0.12;
	/** Niveau de la surface de la Mer suspendue (dernier bloc d'eau). */
	public static final int NIVEAU_MER = 63;
	/** Distance normalisée au-delà de laquelle la Mer n'est plus remplie (le rebord de falaises commence avant). */
	public static final double MER_REMPLISSAGE_MAX = 0.86;

	public static Point evaluer(double x, double z) {
		Region meilleure = null, seconde = null;
		double n1 = Double.MAX_VALUE, n2 = Double.MAX_VALUE;
		for (Region r : Region.values()) {
			double n = r.normalisee(x, z);
			if (n < n1) {
				seconde = meilleure;
				n2 = n1;
				meilleure = r;
				n1 = n;
			} else if (n < n2) {
				seconde = r;
				n2 = n;
			}
		}
		double t = MathHelper.clamp((n2 - n1) / TRANSITION, 0.0, 1.0);
		// poids de la région principale : 0,5 exactement sur la frontière, 1 dès qu'on s'en éloigne.
		double poids = 0.5 + 0.5 * t;
		return new Point(meilleure, n1, seconde, n2, poids);
	}

	public static Region region(double x, double z) {
		return evaluer(x, z).region();
	}

	/** Vrai si la colonne doit être remplie d'eau jusqu'au niveau de la Mer suspendue. */
	public static boolean dansLaMer(double x, double z) {
		Point p = evaluer(x, z);
		return p.region() == Region.MER && p.n() <= MER_REMPLISSAGE_MAX;
	}

	private static double lisse(double t) {
		t = MathHelper.clamp(t, 0.0, 1.0);
		return t * t * (3.0 - 2.0 * t);
	}

	/**
	 * Altitude de la surface (le premier bloc d'air est à {@code floor(h)} ) pour une région, à partir des bruits
	 * {@code collines} et {@code iles} (dans [-1, 1]) et de la distance normalisée. Déterministe et sans
	 * état : utilisée par la fonction de densité, les routes et les tests.
	 */
	public static double hauteur(Region r, double n, double collines, double iles, double temperature, double humidite) {
		double c = MathHelper.clamp(collines, -1.0, 1.0);
		double i = MathHelper.clamp(iles, -1.0, 1.0);
		switch (r) {
			case PRAIRIES -> {
				return 64.5 + 13.44 * c;
			}
			case MER -> {
				int sous = sousBiome(r, n, temperature, humidite);
				double fond = 41.0 + 5.0 * c - (sous == 0 ? 6.0 : 0.0);
				double seuil = sous == 0 ? 0.56 : sous == 1 ? 0.36 : 0.42;
				double ile = Math.max(0.0, i * 0.9 - seuil) * 70.0;
				// Rebord de falaises : à n ≥ 0,86 (fin du remplissage) il dépasse toujours le niveau de l'eau,
				// même avec le fond le plus bas (c = −1 : 36 + 44 × 0,7 ≈ 67).
				double rebord = 44.0 * lisse((n - 0.74) / 0.11) * (0.85 + 0.15 * c);
				return fond + ile + rebord;
			}
			case JARDINS -> {
				double base = 72.5 + 19.0 * c + 6.0 * Math.max(0.0, i);
				if (sousBiome(r, n, temperature, humidite) == 2) {
					base -= 7.0;
				}
				return base;
			}
			case DESERT -> {
				double dunes = 9.0 * (1.0 - Math.abs(c)) + 5.0 * (0.5 + 0.5 * i);
				if (sousBiome(r, n, temperature, humidite) == 2) {
					// Terrasses anciennes : paliers de quatre blocs.
					return 62.5 + 4.0 * Math.round((dunes + 6.0 * i) / 4.0);
				}
				return 60.5 + dunes;
			}
			case MONTAGNES -> {
				int sous = sousBiome(r, n, temperature, humidite);
				double aiguille = Math.max(0.0, 0.55 - Math.abs(i)) / 0.55;
				double base = 94.5 + 45.0 * c + (sous == 0 ? 28.0 : 10.0) * aiguille * aiguille;
				if (sous == 1) {
					base -= 20.0;
				}
				return base;
			}
			case COURONNE -> {
				// Anneaux de paysages suspendus : quatre terrasses concentriques reliées par des pentes douces.
				double anneau = n * 4.0;
				int index = MathHelper.clamp((int) Math.floor(anneau), 0, 3);
				double[] bases = {86.5, 78.5, 82.5, 74.5};
				double frac = anneau - index;
				double h = bases[index];
				if (frac > 0.8 && index < 3) {
					h = MathHelper.lerp(lisse((frac - 0.8) / 0.2), bases[index], bases[index + 1]);
				}
				return h + 3.0 * c;
			}
			default -> {
				return 64.5;
			}
		}
	}

	/** Pente de densité (blocs⁻¹) : plus elle est forte, plus les reliefs sont abrupts. */
	public static double pente(Region r) {
		return switch (r) {
			case MER -> 1.0 / 24.0;
			case MONTAGNES -> 1.0 / 14.0;
			case DESERT -> 1.0 / 28.0;
			default -> 1.0 / 32.0;
		};
	}

	/**
	 * Sous-biome (0, 1 ou 2) d'une région, dans l'ordre déclaré par la source de biomes. Les Prairies
	 * gardent la règle d'origine (température puis humidité) pour ne pas changer le monde existant.
	 */
	public static int sousBiome(Region r, double n, double temperature, double humidite) {
		return switch (r) {
			case PRAIRIES -> temperature < 0.1 ? 0 : humidite < 0.0 ? 1 : 2;
			case MER -> {
				double nn = n + 0.08 * temperature;
				yield nn < 0.45 ? 0 : nn < 0.78 ? 1 : 2;
			}
			case JARDINS -> humidite > 0.2 ? 1 : temperature < -0.1 ? 2 : 0;
			case DESERT -> (n < 0.3 || humidite > 0.3) ? 1 : temperature > 0.15 ? 0 : 2;
			case MONTAGNES -> n < 0.4 ? 0 : humidite > 0.15 ? 1 : 2;
			case COURONNE -> n < 0.3 ? 0 : n < 0.7 ? 1 : 2;
		};
	}

	/** Hauteur mélangée entre la région principale et sa voisine (frontières douces). */
	public static double hauteurMelangee(double x, double z, double collines, double iles, double temperature, double humidite) {
		Point p = evaluer(x, z);
		double h = hauteur(p.region(), p.n(), collines, iles, temperature, humidite);
		if (p.poids() >= 1.0 || p.voisine() == null) {
			return h;
		}
		double hv = hauteur(p.voisine(), p.nVoisine(), collines, iles, temperature, humidite);
		return MathHelper.lerp(p.poids(), hv, h);
	}

	public static double penteMelangee(double x, double z) {
		Point p = evaluer(x, z);
		double pe = pente(p.region());
		if (p.poids() >= 1.0 || p.voisine() == null) {
			return pe;
		}
		return MathHelper.lerp(p.poids(), pente(p.voisine()), pe);
	}

	private LimbesRegions() {
	}
}
