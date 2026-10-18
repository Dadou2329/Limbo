package fr.leslimbes.world.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.leslimbes.quest.QuestDefinition;
import fr.leslimbes.quest.QuestLoader;
import fr.leslimbes.quest.QuestProgress;
import fr.leslimbes.registry.LimbesAttachments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Progression personnelle d'un joueur dans les Limbes : réserve de majesté, pouvoirs, quêtes,
 * drapeaux narratifs et points de retour. Persistée avec le joueur (attachement Fabric), conservée
 * à la mort, synchronisée vers le client pour l'affichage uniquement.
 */
public final class PlayerLimbesData {
	public static final int VERSION = 1;
	public static final int SLOTS = 3;
	public static final float MAJESTE_INITIALE = 60.0F;

	public static final Codec<PlayerLimbesData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("version").forGetter(d -> d.version),
			Codec.FLOAT.fieldOf("majeste").forGetter(d -> d.majeste),
			Codec.FLOAT.fieldOf("majeste_max").forGetter(d -> d.majesteMax),
			Codec.STRING.listOf().fieldOf("pouvoirs").forGetter(d -> List.copyOf(d.pouvoirs)),
			Codec.STRING.listOf().fieldOf("equipes").forGetter(d -> d.equipes),
			QuestProgress.CODEC.listOf().fieldOf("quetes").forGetter(d -> List.copyOf(d.quetes.values())),
			Codec.STRING.listOf().fieldOf("drapeaux").forGetter(d -> List.copyOf(d.drapeaux)),
			Codec.STRING.optionalFieldOf("quete_suivie", "").forGetter(d -> d.queteSuivie),
			GlobalPos.CODEC.optionalFieldOf("retour_overworld").forGetter(d -> d.retourOverworld),
			GlobalPos.CODEC.optionalFieldOf("lieu_sur").forGetter(d -> d.lieuSur),
			BlockPos.CODEC.optionalFieldOf("vestige_connu").forGetter(d -> d.vestigeConnu),
			Codec.BOOL.optionalFieldOf("carnet_recu", false).forGetter(d -> d.carnetRecu),
			Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("compteurs", Map.of()).forGetter(d -> d.compteurs),
			Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("montures", Map.of()).forGetter(d -> d.montures)
	).apply(instance, PlayerLimbesData::new));

	public int version;
	public float majeste;
	public float majesteMax;
	public final Set<String> pouvoirs;
	public final List<String> equipes;
	public final Map<String, QuestProgress> quetes;
	public final Set<String> drapeaux;
	public String queteSuivie;
	public Optional<GlobalPos> retourOverworld;
	public Optional<GlobalPos> lieuSur;
	public Optional<BlockPos> vestigeConnu;
	public boolean carnetRecu;
	public final Map<String, Integer> compteurs;
	/** Montures liées par pacte : clé de monture -> UUID de l'entité (recréée au rappel si introuvable). */
	public final Map<String, String> montures;

	public PlayerLimbesData() {
		this(VERSION, MAJESTE_INITIALE, MAJESTE_INITIALE, List.of(), List.of("", "", ""), List.of(), List.of(), "",
				Optional.empty(), Optional.empty(), Optional.empty(), false, Map.of(), Map.of());
	}

	public PlayerLimbesData(int version, float majeste, float majesteMax, List<String> pouvoirs, List<String> equipes,
							List<QuestProgress> quetes, List<String> drapeaux, String queteSuivie, Optional<GlobalPos> retourOverworld,
							Optional<GlobalPos> lieuSur, Optional<BlockPos> vestigeConnu, boolean carnetRecu, Map<String, Integer> compteurs,
							Map<String, String> montures) {
		this.version = version;
		this.majeste = majeste;
		this.majesteMax = majesteMax;
		this.pouvoirs = new HashSet<>(pouvoirs);
		this.equipes = new ArrayList<>(equipes);
		while (this.equipes.size() < SLOTS) {
			this.equipes.add("");
		}
		this.quetes = new HashMap<>();
		for (QuestProgress q : quetes) {
			this.quetes.put(q.questId, q);
		}
		this.drapeaux = new HashSet<>(drapeaux);
		this.queteSuivie = queteSuivie;
		this.retourOverworld = retourOverworld;
		this.lieuSur = lieuSur;
		this.vestigeConnu = vestigeConnu;
		this.carnetRecu = carnetRecu;
		this.compteurs = new HashMap<>(compteurs);
		this.montures = new HashMap<>(montures);
	}

	/** Accès aux données d'un joueur (serveur ou client) ; créées à la demande. */
	public static PlayerLimbesData of(PlayerEntity player) {
		return player.getAttachedOrCreate(LimbesAttachments.JOUEUR);
	}

	// --- Majesté ---------------------------------------------------------------------------
	public boolean peutPayer(float cout) {
		return majeste >= cout;
	}

	public boolean payer(float cout) {
		if (!peutPayer(cout)) {
			return false;
		}
		majeste -= cout;
		return true;
	}

	public void regenerer(float quantite) {
		majeste = Math.min(majesteMax, majeste + quantite);
	}

	public void augmenterMajesteMax(float delta) {
		majesteMax += delta;
		majeste = Math.min(majesteMax, majeste + delta);
	}

	// --- Pouvoirs --------------------------------------------------------------------------
	public boolean possedePouvoir(String id) {
		return pouvoirs.contains(id);
	}

	/** Débloque un pouvoir et l'équipe dans le premier emplacement libre. @return true si nouveau. */
	public boolean debloquerPouvoir(String id) {
		if (!pouvoirs.add(id)) {
			return false;
		}
		if (!equipes.contains(id)) {
			for (int i = 0; i < SLOTS; i++) {
				if (equipes.get(i).isEmpty()) {
					equipes.set(i, id);
					break;
				}
			}
		}
		return true;
	}

	public boolean equiper(int slot, String id) {
		if (slot < 0 || slot >= SLOTS || (!id.isEmpty() && !pouvoirs.contains(id))) {
			return false;
		}
		for (int i = 0; i < SLOTS; i++) {
			if (equipes.get(i).equals(id) && !id.isEmpty()) {
				equipes.set(i, "");
			}
		}
		equipes.set(slot, id);
		return true;
	}

	// --- Drapeaux et compteurs ---------------------------------------------------------------
	public boolean aDrapeau(String drapeau) {
		return drapeaux.contains(drapeau);
	}

	public boolean poserDrapeau(String drapeau) {
		return drapeaux.add(drapeau);
	}

	public int compteur(String cle) {
		return compteurs.getOrDefault(cle, 0);
	}

	public int incrementerCompteur(String cle) {
		int v = compteur(cle) + 1;
		compteurs.put(cle, v);
		return v;
	}

	// --- Quêtes ------------------------------------------------------------------------------
	public Optional<QuestProgress> quete(String id) {
		return Optional.ofNullable(quetes.get(id));
	}

	public boolean queteActive(String id) {
		QuestProgress p = quetes.get(id);
		return p != null && !p.terminee;
	}

	public boolean queteTerminee(String id) {
		QuestProgress p = quetes.get(id);
		return p != null && p.terminee;
	}

	public Optional<QuestProgress> queteSuivie() {
		if (queteSuivie.isEmpty()) {
			return Optional.empty();
		}
		return quete(queteSuivie).filter(p -> !p.terminee);
	}

	/** Lieu indiqué par l'étape courante de la quête suivie, s'il existe. */
	public Optional<QuestDefinition.Lieu> lieuQueteSuivie() {
		return queteSuivie().flatMap(progress -> {
			QuestDefinition def = QuestLoader.get(progress.questId);
			if (def == null) {
				return Optional.empty();
			}
			return def.lieuPour(progress);
		});
	}

	public Text afficherMajeste() {
		return Text.literal((int) majeste + " / " + (int) majesteMax);
	}
}
