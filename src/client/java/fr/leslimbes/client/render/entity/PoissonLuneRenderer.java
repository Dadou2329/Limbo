package fr.leslimbes.client.render.entity;

import fr.leslimbes.LesLimbes;
import fr.leslimbes.entity.PoissonLuneEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class PoissonLuneRenderer extends MobEntityRenderer<PoissonLuneEntity, PoissonLuneModel> {
	public static final EntityModelLayer LAYER = new EntityModelLayer(LesLimbes.id("poisson_lune"), "main");
	private static final Identifier TEXTURE = LesLimbes.id("textures/entity/poisson_lune.png");

	public PoissonLuneRenderer(EntityRendererFactory.Context ctx) {
		super(ctx, new PoissonLuneModel(ctx.getPart(LAYER)), 0.2F);
	}

	@Override
	public Identifier getTexture(PoissonLuneEntity entity) {
		return TEXTURE;
	}

	@Override
	protected RenderLayer getRenderLayer(PoissonLuneEntity entity, boolean showBody, boolean translucent, boolean showOutline) {
		// Le poisson est lumineux : rendu émissif.
		return RenderLayer.getEntityTranslucentEmissive(TEXTURE);
	}
}
