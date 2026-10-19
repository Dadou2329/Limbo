package fr.leslimbes.power;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * État transitoire des techniques d'un joueur (fenêtres de parade, charge en cours, cible marquée, lames
 * satellites, Marche du Palais, Avènement, étoiles du Jugement). Rien ici n'est sauvegardé : une reconnexion
 * referme toutes les fenêtres, ce qui est sans conséquence pour la progression.
 */
public final class EtatPouvoirs {
	private static final Map<UUID, EtatPouvoirs> ETATS = new HashMap<>();

	/** Une étoile du Jugement des constellations : tombe au tick indiqué sur la cible. */
	public record Etoile(long tick, UUID cible) {
	}

	public long paradeJusqua;
	public long chargeJusqua;
	public final Set<UUID> chargeTouches = new HashSet<>();
	@Nullable
	public UUID marque;
	public long marqueJusqua;
	public long changementJusqua;
	public long satellitesJusqua;
	public double satellitesAngle;
	public long marcheJusqua;
	public long avenementJusqua;
	public boolean avenementPublie;
	public final List<Etoile> etoiles = new ArrayList<>();

	public static EtatPouvoirs de(ServerPlayerEntity player) {
		return ETATS.computeIfAbsent(player.getUuid(), u -> new EtatPouvoirs());
	}

	@Nullable
	public static EtatPouvoirs existant(UUID uuid) {
		return ETATS.get(uuid);
	}

	public static void retirer(UUID uuid) {
		ETATS.remove(uuid);
	}

	public boolean paradeActive(long now) {
		return now < paradeJusqua;
	}

	public boolean chargeActive(long now) {
		return now < chargeJusqua;
	}

	public boolean satellitesActifs(long now) {
		return now < satellitesJusqua;
	}

	public boolean marcheActive(long now) {
		return now < marcheJusqua;
	}

	public boolean avenementActif(long now) {
		return now < avenementJusqua;
	}

	/** L'Avènement est-il en cours pour ce joueur (lecture depuis l'état public) ? */
	public static boolean avenementActif(ServerPlayerEntity player) {
		EtatPouvoirs e = ETATS.get(player.getUuid());
		return e != null && e.avenementActif(player.getServerWorld().getTime());
	}

	private EtatPouvoirs() {
	}
}
