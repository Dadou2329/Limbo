package fr.leslimbes.domaine;

import fr.leslimbes.quest.QuestManager;
import fr.leslimbes.quest.QuestSignal;
import fr.leslimbes.registry.LimbesSounds;
import fr.leslimbes.world.state.PlayerDataSync;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Découverte des lieux d'intérêt : un joueur qui entre dans le rayon d'un lieu le voit inscrit à son
 * journal et sur sa carte (drapeau {@code lieu:<id>}). Les relais portés par un lieu sont découverts avec lui.
 */
public final class LieuxManager {
	/** Appelé une fois par seconde et par joueur. */
	public static void tick(ServerPlayerEntity player) {
		if (DomaineLoader.lieux().isEmpty()) {
			return;
		}
		PlayerLimbesData data = PlayerLimbesData.of(player);
		String dim = player.getServerWorld().getRegistryKey().getValue().toString();
		for (LieuDefinition lieu : DomaineLoader.lieux().values()) {
			if (!lieu.dimension().equals(dim) || data.aDrapeau(lieu.drapeau())) {
				continue;
			}
			double dx = player.getX() - (lieu.pos().getX() + 0.5);
			double dz = player.getZ() - (lieu.pos().getZ() + 0.5);
			double dy = Math.abs(player.getY() - lieu.pos().getY());
			if (dx * dx + dz * dz <= (double) lieu.rayon() * lieu.rayon() && dy <= Math.max(24, lieu.rayon())) {
				decouvrir(player, lieu, true);
			}
		}
	}

	/** Inscrit un lieu au journal du joueur. @return true si c'était une découverte. */
	public static boolean decouvrir(ServerPlayerEntity player, LieuDefinition lieu, boolean annoncer) {
		PlayerLimbesData data = PlayerLimbesData.of(player);
		if (!data.poserDrapeau(lieu.drapeau())) {
			return false;
		}
		if (annoncer) {
			player.sendMessage(Text.translatable("message.les_limbes.lieu.decouvert", lieu.nom()).formatted(Formatting.GOLD), true);
			player.playSoundToPlayer(LimbesSounds.QUETE_ETAPE, SoundCategory.PLAYERS, 0.7F, 1.1F);
		}
		QuestManager.signaler(player, QuestSignal.drapeau(lieu.drapeau()));
		if (!lieu.relais().isEmpty()) {
			RelaisManager.decouvrir(player, lieu.relais(), annoncer);
		}
		PlayerDataSync.envoyer(player);
		return true;
	}

	private LieuxManager() {
	}
}
