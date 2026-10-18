package fr.leslimbes.quest;

import fr.leslimbes.quest.QuestDefinition.TypeObjectif;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

/** Un événement de jeu susceptible de faire avancer un objectif. */
public record QuestSignal(TypeObjectif type, String cible, Optional<BlockPos> pos) {
	public static QuestSignal parler(String role) {
		return new QuestSignal(TypeObjectif.PARLER, role, Optional.empty());
	}

	public static QuestSignal vaincre(String entityId) {
		return new QuestSignal(TypeObjectif.VAINCRE, entityId, Optional.empty());
	}

	public static QuestSignal interagir(String blockId, BlockPos pos) {
		return new QuestSignal(TypeObjectif.INTERAGIR, blockId, Optional.of(pos));
	}

	public static QuestSignal pouvoir(String abilityId) {
		return new QuestSignal(TypeObjectif.POUVOIR, abilityId, Optional.empty());
	}

	public static QuestSignal drapeau(String drapeau) {
		return new QuestSignal(TypeObjectif.DRAPEAU, drapeau, Optional.empty());
	}
}
