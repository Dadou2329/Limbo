package fr.leslimbes.registry;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.world.state.PlayerLimbesData;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

/** Données attachées aux entités (ici : la progression du joueur). */
public final class LimbesAttachments {
	public static final AttachmentType<PlayerLimbesData> JOUEUR = AttachmentRegistry.create(LesLimbes.id("joueur"),
			builder -> builder.persistent(PlayerLimbesData.CODEC).copyOnDeath().initializer(PlayerLimbesData::new));

	public static void init() {
	}

	private LimbesAttachments() {
	}
}
