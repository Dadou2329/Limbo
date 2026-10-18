package fr.leslimbes.entity;

/**
 * Entité dont le modèle piloté par données peut lire un état d'animation (préparation d'attaque,
 * phase, repos...) et une variante de texture. Implémentée côté commun, lue côté client.
 */
public interface EntiteAnimee {
	/** État d'animation courant (0 = normal) ; les animations « etat » se déclenchent sur une valeur précise. */
	default int etatAnimation() {
		return 0;
	}

	/** Suffixe de texture (« » = texture par défaut). */
	default String varianteTexture() {
		return "";
	}
}
