package fr.leslimbes.client.render.entity;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.npc.HabitantEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

import java.util.Map;

/** Rendu des habitants : modèle de joueur et skin propre à chaque rôle. */
public class HabitantRenderer extends MobEntityRenderer<HabitantEntity, PlayerEntityModel<HabitantEntity>> {
	private static final Map<String, Identifier> CACHE = new java.util.concurrent.ConcurrentHashMap<>();

	public HabitantRenderer(EntityRendererFactory.Context ctx) {
		super(ctx, new PlayerEntityModel<>(ctx.getPart(EntityModelLayers.PLAYER), false), 0.5F);
	}

	@Override
	public Identifier getTexture(HabitantEntity entity) {
		String role = entity.getRole();
		// Chaque rôle a son apparence si sa texture existe dans les ressources ; sinon l'apparence commune.
		return CACHE.computeIfAbsent(role, r -> {
			Identifier propre = LesLimbes.id("textures/entity/habitant/" + r + ".png");
			boolean existe = net.minecraft.client.MinecraftClient.getInstance().getResourceManager().getResource(propre).isPresent();
			return existe ? propre : LesLimbes.id("textures/entity/habitant/habitant_1.png");
		});
	}

	@Override
	protected boolean hasLabel(HabitantEntity entity) {
		return this.dispatcher.getSquaredDistanceToCamera(entity) < 24.0 * 24.0;
	}
}
