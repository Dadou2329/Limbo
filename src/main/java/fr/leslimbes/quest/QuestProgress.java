package fr.leslimbes.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Progression d'un joueur dans une quête. Données pures (sans référence au monde) : sérialisées
 * avec le joueur, testables sans Minecraft.
 */
public final class QuestProgress {
	public static final Codec<QuestProgress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("quete").forGetter(p -> p.questId),
			Codec.INT.fieldOf("etape").forGetter(p -> p.etape),
			Codec.BOOL.fieldOf("terminee").forGetter(p -> p.terminee),
			Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("compteurs").forGetter(p -> p.compteurs),
			Codec.STRING.listOf().fieldOf("faits").forGetter(p -> List.copyOf(p.faits)),
			Codec.unboundedMap(Codec.STRING, BlockPos.CODEC).fieldOf("lieux").forGetter(p -> p.lieux)
	).apply(instance, QuestProgress::new));

	public final String questId;
	public int etape;
	public boolean terminee;
	public final Map<String, Integer> compteurs;
	public final Set<String> faits;
	public final Map<String, BlockPos> lieux;

	public QuestProgress(String questId) {
		this(questId, 0, false, Map.of(), List.of(), Map.of());
	}

	public QuestProgress(String questId, int etape, boolean terminee, Map<String, Integer> compteurs, List<String> faits, Map<String, BlockPos> lieux) {
		this.questId = questId;
		this.etape = etape;
		this.terminee = terminee;
		this.compteurs = new HashMap<>(compteurs);
		this.faits = new HashSet<>(faits);
		this.lieux = new HashMap<>(lieux);
	}

	public int compteur(String objectifId) {
		return compteurs.getOrDefault(objectifId, 0);
	}

	public int incrementer(String objectifId, int delta) {
		int v = compteur(objectifId) + delta;
		compteurs.put(objectifId, v);
		return v;
	}

	public boolean estFait(String objectifId) {
		return faits.contains(objectifId);
	}

	/** Réinitialise les marqueurs d'objectifs lors du passage à une nouvelle étape. */
	public void nouvelleEtape(int index) {
		this.etape = index;
		this.faits.clear();
		this.compteurs.clear();
	}

	public QuestProgress copier() {
		return new QuestProgress(questId, etape, terminee, compteurs, List.copyOf(faits), lieux);
	}
}
