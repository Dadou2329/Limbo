package fr.leslimbes.client.render.entity;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.entity.VeilleurEgareEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class VeilleurEgareRenderer extends MobEntityRenderer<VeilleurEgareEntity, VeilleurEgareModel> {
	public static final EntityModelLayer LAYER = new EntityModelLayer(LesLimbes.id("veilleur_egare"), "main");
	private static final Identifier TEXTURE = LesLimbes.id("textures/entity/veilleur_egare.png");

	public VeilleurEgareRenderer(EntityRendererFactory.Context ctx) {
		super(ctx, new VeilleurEgareModel(ctx.getPart(LAYER)), 0.7F);
	}

	@Override
	public Identifier getTexture(VeilleurEgareEntity entity) {
		return TEXTURE;
	}

	@Override
	protected void scale(VeilleurEgareEntity entity, MatrixStack matrices, float amount) {
		matrices.scale(1.6F, 1.6F, 1.6F);
	}
}
