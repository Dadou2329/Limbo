package fr.leslimbes.registry;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

/** Données attachées aux entités (ici : la progression du joueur). */
public final class LimbesAttachments {
	public static final AttachmentType<PlayerLimbesData> JOUEUR = AttachmentRegistry.create(LesLimbes.id("joueur"),
			builder -> builder.persistent(PlayerLimbesData.CODEC).copyOnDeath().initializer(PlayerLimbesData::new));

	/**
	 * État public d'un joueur, synchronisé vers tous les clients qui le voient : niveau de couronne
	 * (0 = rien, 1 = fragment, 2 = couronne assemblée, 3 = Avènement). Dérivé des données privées côté serveur.
	 */
	public static final AttachmentType<Integer> ETAT_PUBLIC = AttachmentRegistry.create(LesLimbes.id("etat_public"),
			builder -> builder.initializer(() -> 0).copyOnDeath()
					.syncWith(net.minecraft.network.codec.PacketCodecs.VAR_INT, net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate.all()));

	/** Niveau de couronne public d'un joueur (lecture client ou serveur). */
	public static int niveauCouronne(net.minecraft.entity.player.PlayerEntity player) {
		Integer v = player.getAttached(ETAT_PUBLIC);
		return v == null ? 0 : v;
	}

	/** Recalcule et publie l'état public d'un joueur à partir de sa progression (serveur). */
	public static void publier(net.minecraft.server.network.ServerPlayerEntity player) {
		PlayerLimbesData data = PlayerLimbesData.of(player);
		int niveau = fr.leslimbes.power.EtatPouvoirs.avenementActif(player) ? 3 : data.aDrapeau("couronne_assemblee") ? 2 : data.pouvoirs.isEmpty() ? 0 : 1;
		if (niveauCouronne(player) != niveau) {
			player.setAttached(ETAT_PUBLIC, niveau);
		}
	}

	public static void init() {
	}

	private LimbesAttachments() {
	}
}
